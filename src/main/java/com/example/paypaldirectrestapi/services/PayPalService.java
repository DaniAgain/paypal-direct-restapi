package com.example.paypaldirectrestapi.services;// PayPalService.java

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.Scanner;

@Service
public class PayPalService {

    @Value("${paypal.api.url}")
    private String paypalApiUrl;

    @Value("${paypal.api.client-id}")
    private String paypalClientId;

    @Value("${paypal.api.secret}")
    private String paypalSecret;

    private final RestTemplate restTemplate;

    public PayPalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createOrder() {
        try {
            String accessToken = getAccessToken();

            URL url = new URL(paypalApiUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");



            // Set request headers
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Set request body - JSON payload for creating an order
            String orderPayload = "{ \"intent\": \"CAPTURE\", \"purchase_units\": [ { \"amount\": { \"currency_code\": \"USD\", \"value\": \"100.00\" } } ] }";
            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write(orderPayload);
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

            // Handle the response
            String response = handleResponse(httpConn);
            return "Order Created: " + response;
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return "Failed to create order";
        }

    }
    private String handleResponse(HttpURLConnection httpConn) throws IOException {
        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    private String getAccessToken() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((paypalClientId + ":" + paypalSecret).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeader);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        String url = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
        AccessTokenResponse response = restTemplate.postForObject(url, request, AccessTokenResponse.class);

        // Return the access token
        return response != null ? response.getAccessToken() : null;
    }

    private static class AccessTokenResponse {
        private String scope;
        private String access_token;
        private String token_type;
        private String app_id;
        private long expires_in;
        private String nonce;

        // Getter methods

        public String getAccessToken() {
            return access_token;
        }


    }

}