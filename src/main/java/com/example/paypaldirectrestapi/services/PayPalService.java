package com.example.paypaldirectrestapi.services;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class PayPalService {

    private final APIContext apiContext;

    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.forLanguageTag(currency), "%.2f", total)); // 9.99$ - 9,99€

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(
            String paymentId,
            String payerId
    ) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        return payment.execute(apiContext, paymentExecution);
    }
}

    /*private static final Logger log = LoggerFactory.getLogger(PayPalController.class);

    @Value("${paypal.api.url}")
    private String paypalApiUrl;

    @Value("${paypal.api.client-id}")
    private String paypalClientId;

    @Value("${paypal.api.secret}")
    private String paypalSecret;

    private final RestTemplate restTemplate;


    @Autowired
    private PaymentRepository orderRepository;

    private final Payment order = new Payment();

    public PayPalService(RestTemplate restTemplate, PaymentRepository orderRepository) {
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
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

            System.out.println(getAccessToken());
            // Handle the response
            String response = handleResponse(httpConn);
            String orderId = extractOrderId(response);
            extractOrderId(response);
            saveOrderId(orderId);
            System.out.println(orderId);

            Payment order = new Payment();
            order.setOrderId(orderId);
            orderRepository.save(order);

            return response;


        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return "Failed to create order";
        }


    }

    public String getLatestOrderId() {
        // Retrieve the latest orderId from the database
        Payment latestOrder = orderRepository.findTopByOrderByIdDesc();
        if (latestOrder != null) {
            return latestOrder.getLatestOrderId();
        } else {
            return "No order ID found";
        }
    }

    private void saveOrderId(String orderId) {
        // Save orderId to the database
        Payment order = new Payment();
        order.setOrderId(orderId);
        orderRepository.save(order);
    }

    private String extractOrderId(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("id");
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Return null if orderId extraction fails
        }
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

        // Handle the response as needed, you might want to parse JSON or do other processing
        // For now, just printing the response
        System.out.println(response);

        return response;
    }

    public String getApprovalUrl() {
        try {
            String latestOrderId = getLatestOrderId(); // Antag att du har en metod för att hämta det senaste order-ID
            String accessToken = getAccessToken();

            // Skapa URL för att hämta orderdetaljer från PayPal
            URL url = new URL(paypalApiUrl + "/v2/checkout/orders/" + latestOrderId);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            httpConn.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Hantera PayPal API-responsen
            String response = handleResponse(httpConn);

            // Analysera responsen för att hämta godkännandelänken
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String approvalUrl = jsonResponse.getAsJsonObject("links")
                    .getAsJsonArray("rel")
                    .getAsString();

            return approvalUrl;
        } catch (IOException e) {
            e.printStackTrace(); // Hantera felet lämpligt
            return "Failed to get approval URL";
        }
    }


    public String confirmPaymentSource(String orderId) {
        try {
            String accessToken = getAccessToken();

            // Skapa URL för att bekräfta betalningskällan för en specifik order
            URL confirmUrl = new URL("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/confirm-payment");
            HttpURLConnection confirmConnection = (HttpURLConnection) confirmUrl.openConnection();
            confirmConnection.setRequestMethod("POST");
            confirmConnection.setRequestProperty("Authorization", "Bearer " + accessToken);

            // Sätt upp JSON-payload för bekräftelse av betalningskällan
            String confirmPayload = "{ \"payment_source\": { \"paypal\": { \"name\": { \"given_name\": \"John\", \"surname\": \"Doe\" }, \"email_address\": \"customer@example.com\", \"experience_context\": { \"payment_method_preference\": \"IMMEDIATE_PAYMENT_REQUIRED\", \"brand_name\": \"EXAMPLE INC\", \"locale\": \"en-US\", \"landing_page\": \"LOGIN\", \"shipping_preference\": \"SET_PROVIDED_ADDRESS\", \"user_action\": \"PAY_NOW\", \"return_url\": \"https://example.com/returnUrl\", \"cancel_url\": \"https://example.com/cancelUrl\" } } } }";

            // Skicka JSON-payload till PayPal
            confirmConnection.setDoOutput(true);
            try (OutputStreamWriter confirmWriter = new OutputStreamWriter(confirmConnection.getOutputStream())) {
                confirmWriter.write(confirmPayload);
            }

            // Läs och returnera PayPal API-responsen för bekräftelse
            InputStream confirmResponseStream = confirmConnection.getResponseCode() / 100 == 2
                    ? confirmConnection.getInputStream()
                    : confirmConnection.getErrorStream();
            String confirmResponse = new Scanner(confirmResponseStream).useDelimiter("\\A").next();

            // Om bekräftelsen är lyckad, hämta godkännandelänken
            if (confirmConnection.getResponseCode() / 100 == 2) {
                String approvalUrl = getApprovalUrl(orderId);
                return "Payment source confirmed. Approval URL: " + approvalUrl;
            } else {
                return "Failed to confirm payment source. PayPal API Response: " + confirmResponse;
            }
        } catch (IOException e) {
            e.printStackTrace(); // Hantera felet lämpligt
            return "Failed to confirm payment source";
        }
    }




    private String handleResponse(HttpURLConnection httpConn) throws IOException {
        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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


    }*/
