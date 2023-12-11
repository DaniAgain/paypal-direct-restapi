package com.example.paypaldirectrestapi.controllers;

import com.example.paypaldirectrestapi.services.PayPalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;


@Controller
@RequiredArgsConstructor
@Slf4j
public class PayPalController{

    private final PayPalService payPalService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") String amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    ) {
        try {
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";
            Payment payment = payPalService.createPayment(
                    Double.valueOf(amount),
                    currency,
                    method,
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );

            for (Links links: payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return new RedirectView(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            log.error("Error occurred:: ", e);
        }
        return new RedirectView("/payment/error");
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId
    ) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                return "paymentSuccess";
            }
        } catch (PayPalRESTException e) {
            log.error("Error occurred:: ", e);
        }
        return "paymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "paymentCancel";
    }

    @GetMapping("/payment/error")
    public String paymentError() {
        return "paymentError";
    }
} //The views "paymentSuccess," "paymentCancel," and "paymentError" are likely associated with Thymeleaf templates for rendering HTML pages.



/*private final PaymentRepository orderRepository;

    private final Payment order = new Payment();

    @Autowired
    public PayPalController(PayPalService payPalService, PaymentRepository orderRepository) {
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





    @PostMapping("/payment/create")
    public RedirectView createPayment() {
        try {
            String cancelUrl = "http://localhost:8080/paypal/payment/cancel";
            String successUrl = "http://localhost:8080/paypal/payment/cancel";
            Payment payment = payPalService

        } catch (PayPalRESTException e) {
            log.error("Error occurred: ", e);
        }
        // Invoke the PayPal service to create an order
        String response = payPalService.createOrder();
        System.out.println(response);

        return new RedirectView("https://www.sandbox.paypal.com/checkoutnow?token=" + payPalService.getLatestOrderId());


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
    }*/
