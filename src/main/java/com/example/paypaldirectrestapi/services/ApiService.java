package com.example.paypaldirectrestapi.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final String apiUrl = "https://api-m.sandbox.paypal.com";

    private final RestTemplate restTemplate;

    public ApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchData() {

        return restTemplate.getForObject(apiUrl + "/endpoint", String.class);
    }


}
