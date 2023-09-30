package com.example.smsphishing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SMSFetchActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 101;
    private FirebaseAuth auth;
    private TextView smsTextView;
    private Spinner smsSpinner;
    private SMSItem selectedSMSItem;

    public class SMSItem {
        private String address;
        private String body;
        private String date;

        public SMSItem(String address, String body, String date) {
            this.address = address;
            this.body = body;
            this.date = date;
        }

        public String getAddress() {
            return address;
        }

        public String getBody() {
            return body;
        }

        public String getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "Address:" + address + "\nBody: " + body + "\nDate: " + date;
        }
    }

    private class CustomSpinnerAdapter extends ArrayAdapter<SMSItem> {

        public CustomSpinnerAdapter(@NonNull Context context, @NonNull List<SMSItem> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createItemView(position, convertView, parent);
        }

        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createItemView(position, convertView, parent);
        }

        private View createItemView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_spinner_item, parent, false);
            }

            SMSItem smsItem = getItem(position);
            if (smsItem != null) {
                TextView addressText = convertView.findViewById(R.id.address_text);
                TextView bodyText = convertView.findViewById(R.id.body_text);
                TextView dateText = convertView.findViewById(R.id.date_text);

                addressText.setText("Address: " + smsItem.getAddress());
                bodyText.setText("Body: " + smsItem.getBody());
                dateText.setText("Date: " + smsItem.getDate());
            }
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_fetch);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        auth = FirebaseAuth.getInstance();

        smsSpinner = findViewById(R.id.spinner_fetch_sms);

        smsTextView = findViewById(R.id.tv_fetch_sms);
        Button smsFetchButton = findViewById(R.id.btn_fetch_sms);
        Button signOutButton = findViewById(R.id.btn_sign_out);
        Button submitButton = findViewById(R.id.btn_submit_sms);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSMS();
            }
        });

        smsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedSMSItem = (SMSItem) parentView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        smsFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(SMSFetchActivity.this,
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SMSFetchActivity.this, new String[]
                            {Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
                } else {
                    fetchSMSMessages();
                }
            }
        });
    }

    private void submitSMS() {
        if (selectedSMSItem != null) {
            String selectedSMSBody = selectedSMSItem.getBody();
            String selectedSMSAddress = selectedSMSItem.getAddress();

            TextView addressTextView = findViewById(R.id.tv_address);
            TextView bodyTextView = findViewById(R.id.tv_body);

            addressTextView.setText("Selected SMS Address: " + selectedSMSAddress);
            bodyTextView.setText("Selected SMS Body: " + selectedSMSBody);

        } else {
            Toast.makeText(this, "No SMS selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        auth.signOut();
        startActivity(new Intent(SMSFetchActivity.this, SignInActivity.class));
        finish();
    }

    private void fetchSMSMessages() {

        long currentTime = System.currentTimeMillis();
        long fourDaysAgo = currentTime - (96 * 60 * 60 * 1000);

        Uri inboxUri = Uri.parse("content://sms/inbox");
        String[] projection = {"_id", "address", "body", "date"};

        String selection = "date >= ?";
        String[] selectionArgs = {String.valueOf(fourDaysAgo)};

        Cursor cursor = getContentResolver().query(inboxUri, projection, selection, selectionArgs, "date DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                List<SMSItem> smsItemList = new ArrayList<>();

                do {
                    @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                    @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex("body"));
                    @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex("date"));
                    @SuppressLint("Range") String formattedDate = dateFormat.format(new Date(date));

                    SMSItem smsItem = new SMSItem(address, body, formattedDate);
                    smsItemList.add(smsItem);
                } while (cursor.moveToNext());

                CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this, smsItemList);
                smsSpinner.setAdapter(adapter);
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch SMS messages
                fetchSMSMessages();
            } else {
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}





























