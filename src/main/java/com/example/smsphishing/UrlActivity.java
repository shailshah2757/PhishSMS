package com.example.smsphishing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlActivity extends AppCompatActivity {

    private EditText urlEditText;
    private TextView resultTextView;
    private Button checkButton;
    private ProgressBar progressBar;
    private boolean resultObtained = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);

        urlEditText = findViewById(R.id.et_url);
        resultTextView = findViewById(R.id.tv_url);
        checkButton = findViewById(R.id.btn_url);
        progressBar = findViewById(R.id.progress_bar);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlEditText.getText().toString();
                if (!url.isEmpty()) {
                    new CheckUrlTask().execute(url);
                } else {
                    Toast.makeText(UrlActivity.this, "Please enter a Url", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class CheckUrlTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "https://www.virustotal.com/api/v3/urls";
            String apiKey = "034521d2f423341d64a6dcaf2c049dd121c80bef1144edc0a92179beab228c05";
            String urlToCheck = params[0];
            String result;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("x-apikey", apiKey);
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded");

                String postData = "url=" + urlToCheck;
                connection.setDoOutput(true);
                connection.getOutputStream().write(postData.getBytes("UTF-8"));

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject data = jsonResponse.optJSONObject("data");

                    if (data != null) {
                        String analysisId = data.optString("id");
                        String analysisUrl = "https://www.virustotal.com/api/v3/analyses/" + analysisId;

                        while (true) {
                            HttpURLConnection analysisConnection = (HttpURLConnection) new URL(analysisUrl).openConnection();
                            analysisConnection.setRequestProperty("x-apikey", apiKey);
                            int analysisResponseCode = analysisConnection.getResponseCode();

                            if (analysisResponseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader analysisReader = new BufferedReader(new InputStreamReader(analysisConnection.getInputStream()));
                                StringBuilder analysisResponse = new StringBuilder();
                                String analysisLine;

                                while ((analysisLine = analysisReader.readLine()) != null) {
                                    analysisResponse.append(analysisLine);
                                }

                                analysisReader.close();

                                JSONObject analysisJsonResponse = new JSONObject(analysisResponse.toString());
                                JSONObject analysisData = analysisJsonResponse.optJSONObject("data");
                                JSONObject analysisAttributes = analysisData.optJSONObject("attributes");

                                String analysisStatus = analysisAttributes.optString("status");

                                if ("completed".equals(analysisStatus)) {
                                    int verdict = analysisAttributes.optJSONObject("stats").optInt("malicious", 0);

                                    if (verdict > 0) {
                                        result = "The URL is malicious.";
                                    } else {
                                        result = "The URL is not malicious.";
                                    }

                                    break;
                                } else if ("queued".equals(analysisStatus) || "inprogress".equals(analysisStatus)) {
                                    result = "Analysis is still in progress. Checking again in a moment...";
                                } else {
                                    result = "Analysis status: " + analysisStatus;
                                    break;
                                }
                            } else {
                                result = "Failed to retrieve analysis results. Status code: " + analysisResponseCode;
                                break;
                            }
                        }
                    } else {
                        result = "Unable to retrieve analysis data.";
                    }
                } else {
                    result = "Failed to retrieve analysis results. Status code: " + responseCode;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                result = "Error: " + e.getMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            resultTextView.setText(result);
            progressBar.setVisibility(View.GONE);
            resultObtained = true;
        }
    }
}