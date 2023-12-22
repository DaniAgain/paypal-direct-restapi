package com.example.paypaldirectrestapi.services;

import com.example.paypaldirectrestapi.models.Order;
import com.example.paypaldirectrestapi.repositories.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    @Autowired
    private OrderRepository orderRepository;

    private final Order order = new Order();

    public PayPalService(RestTemplate restTemplate, OrderRepository orderRepository) {
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
    }

    public String createOrder() {
        try {
            String accessToken = getAccessToken();

            URL url = new URL(paypalApiUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");




            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);


            String orderPayload = "{ \"intent\": \"CAPTURE\", \"purchase_units\": [ { \"amount\": { \"currency_code\": \"USD\", \"value\": \"100.00\" } } ] }";
            httpConn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write(orderPayload);
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();




            System.out.println(getAccessToken());

            String response = handleResponse(httpConn);
            String orderId = extractOrderId(response);
            extractOrderId(response);
            saveOrderId(orderId);
            System.out.println(orderId);

            Order order = new Order();
            order.setOrderId(orderId);
            orderRepository.save(order);

            return response;


        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to create order";
        }


    }

    public String getLatestOrderId() {
        Order latestOrder = orderRepository.findTopByOrderByIdDesc();
        if (latestOrder != null) {
            return latestOrder.getLatestOrderId();
        } else {
            return "No order ID found";
        }
    }

    private void saveOrderId(String orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        orderRepository.save(order);
    }

    private String extractOrderId(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




    private String handleResponse(HttpURLConnection httpConn) throws IOException {
        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    public String getAccessToken() {
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((paypalClientId + ":" + paypalSecret).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", authHeader);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        String url = "https://api-m.sandbox.paypal.com/v1/oauth2/token";
        AccessTokenResponse response = restTemplate.postForObject(url, request, AccessTokenResponse.class);

        // Return the access token
        return response != null ? response.getAccess_token() : null;
    }


    public String getOrderDetails() throws IOException {
        URL url = new URL("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + getLatestOrderId());
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        httpConn.setRequestProperty("Authorization", "Bearer " + getAccessToken());

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";

        System.out.println(response);

        return response;
    }





    public String confirmPaymentSource(String orderId) {
        try {

            String confirmUrl = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + getLatestOrderId() + "/confirm-payment-source";


            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + getAccessToken());
            headers.set("Content-Type", "application/json");

            String requestBody = "{ \"payment_source\": { \"paypal\": { \"name\": { \"given_name\": \"John\", \"surname\": \"Doe\" }, \"email_address\": \"customer@example.com\", \"experience_context\": { \"payment_method_preference\": \"IMMEDIATE_PAYMENT_REQUIRED\", \"brand_name\": \"EXAMPLE INC\", \"locale\": \"en-US\", \"landing_page\": \"LOGIN\", \"shipping_preference\": \"SET_PROVIDED_ADDRESS\", \"user_action\": \"PAY_NOW\", \"return_url\": \"https://example.com/returnUrl\", \"cancel_url\": \"https://example.com/cancelUrl\" } } } }";

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(confirmUrl, entity, String.class);

            System.out.println("Confirm Order Response: " + response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {

            System.err.println("Error Status Code: " + e.getRawStatusCode());
            System.err.println("Error Response: " + e.getResponseBodyAsString());
            return "Error confirming payment source";
        } catch (Exception e) {

            e.printStackTrace();
            return "Error confirming payment source";
        }
    }




    private static class AccessTokenResponse {
        private String scope;
        private String access_token;
        private String token_type;
        private String app_id;
        private long expires_in;
        private String nonce;

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public String getApp_id() {
            return app_id;
        }

        public void setApp_id(String app_id) {
            this.app_id = app_id;
        }

        public long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(long expires_in) {
            this.expires_in = expires_in;
        }

        public String getNonce() {
            return nonce;
        }

        public void setNonce(String nonce) {
            this.nonce = nonce;
        }


    }

}
