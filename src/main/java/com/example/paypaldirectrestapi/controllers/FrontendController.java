package com.example.paypaldirectrestapi.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {



    @GetMapping("/start")
    public String showStartPage(Model model) {

        model.addAttribute("approveLink", null);
        return "createOrder.html";
    }

}
