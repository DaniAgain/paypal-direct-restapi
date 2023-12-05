package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.services.PayPalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/paypal")
public class PayPalController {
    private static final Logger log = LoggerFactory.getLogger(PayPalController.class);

    @Autowired
    private PayPalService payPalService;

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



    @GetMapping ("/{orderId}")
    public ResponseEntity<String> getOrderDetails(@PathVariable String orderId) {
        String orderCreationResult = payPalService.createOrder();
        log.info("Resultat av order skapande: {}", orderCreationResult);
        // Fortsätt med att hämta orderdetaljer
        String orderDetails = payPalService.getOrderDetails(orderId);

        return ResponseEntity.ok(orderDetails);
    }

    // Om jag fortfarande vill ha en global exception handler kan jag ha den här
    /*@ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleIOException(IOException e) {
        // Logga felet för intern felsökning
        log.error("IOException vid hantering av orderdetaljer", e);
        // Returnera ett meddelande i responskroppen
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fel vid hämtning av orderdetaljer");
    }*/

}
