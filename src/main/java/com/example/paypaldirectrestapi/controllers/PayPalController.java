package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.models.Order;
import com.example.paypaldirectrestapi.repositories.OrderRepository;
import com.example.paypaldirectrestapi.services.PayPalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

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
    public RedirectView createOrder() {
        // Invoke the PayPal service to create an order
        String response = payPalService.createOrder();
        return new RedirectView("https://www.sandbox.paypal.com/checkoutnow?token=" + payPalService.getLatestOrderId());
    }

/*

    @GetMapping("/create-order")
    public String createOrder() {

        String response = payPalService.createOrder();
        return response;
    }

    */



    @GetMapping("/get-order-details")
    public String getOrderDetails() throws IOException {
        String response = payPalService.getOrderDetails();
        return "Order Created: " + response;
    }




    /*
    @PostMapping("/confirm-payment-source")
    public String confirmPaymentSource() {
        String orderId = payPalService.getLatestOrderId();
        return payPalService.confirmPaymentSource();
    }
*/


}