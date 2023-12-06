package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.models.Order;
import com.example.paypaldirectrestapi.repositories.OrderRepository;
import com.example.paypaldirectrestapi.services.PayPalService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/paypal")
public class PayPalController{

    private final PayPalService payPalService;

    private final OrderRepository orderRepository;

    private final Order order = new Order();

    @Autowired
    public PayPalController(PayPalService payPalService, OrderRepository orderRepository) {
        this.payPalService = payPalService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/create-order")
    public String createOrder() {
        // Invoke the PayPal service to create an order
        String response = payPalService.createOrder();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedJson = gson.toJson(response);
        return "Order Created: " + formattedJson;
    }



    @GetMapping("/get-order-details")
    public String getOrderDetails() throws IOException {
        String response = payPalService.getOrderDetails();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String formattedJson = gson.toJson(response);
        return "Order Created: " + formattedJson;
    }


    @GetMapping("/confirm-payment/{orderId}")
    public String confirmPaymentSource(@PathVariable String orderId) {
            return payPalService.confirmPaymentSource(orderId);
    }



}