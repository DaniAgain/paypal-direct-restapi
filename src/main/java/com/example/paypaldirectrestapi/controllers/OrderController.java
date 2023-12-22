package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.services.PayPalService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/paypal")
public class OrderController {

    private final PayPalService payPalService;

    public OrderController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/confirm-payment-source")
    public String confirmPaymentSource() {
        String orderId = payPalService.getLatestOrderId();
        return payPalService.confirmPaymentSource(orderId);
    }


    @PostMapping("/authorize-order")
    public ResponseEntity<String> authorizeOrder() {

        String orderId = payPalService.getLatestOrderId();
        String authorizeUrl = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/authorize";


        return getStringResponseEntity(authorizeUrl);
    }

    private ResponseEntity<String> getStringResponseEntity(String authorizeUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + payPalService.getAccessToken());


        HttpEntity<String> requestEntity = new HttpEntity<>(headers);


        RestTemplate restTemplate = new RestTemplate();


        ResponseEntity<String> responseEntity = restTemplate.exchange(
                authorizeUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );


        return responseEntity;
    }

    @PostMapping("/capture-order")
    public ResponseEntity<String> captureOrder() {

        String orderId = payPalService.getLatestOrderId();
        String captureUrl = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture";


        return getStringResponseEntity(captureUrl);
    }



}

