package com.example.paypaldirectrestapi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {



    @GetMapping("/start")
    public String showStartPage(Model model) {
        // Add any necessary attributes to the model
        model.addAttribute("approveLink", null); // Set a default value or as needed
        return "createPayment.html"; // Return the name of your Thymeleaf template
    }

}
