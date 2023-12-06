package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.models.Order;
import com.example.paypaldirectrestapi.services.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    private Order order;

    @Autowired
    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @GetMapping("/create-order")
    public String createOrder() {
        // Invoke the PayPal service to create an order
        String response = payPalService.createOrder();
        return "Order Created: " + response;
    }

    @GetMapping("/show-order-details/{orderId}")
    public String showOrderDetails(Model model) {
        String orderId = order.getOrderId();
        String accessToken = payPalService.getAccessToken();

        // Get order details using the service
        String orderDetails = payPalService.getOrderDetails(orderId, accessToken);

        model.addAttribute("orderDetails", orderDetails);
        return "your-template-name"; // Replace with the actual template name
    }


}