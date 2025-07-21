package com.auth.services;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

@Service
public class ContractorConnect {

    private final RestTemplate restTemplate;

    public ContractorConnect(RestTemplateBuilder restTemplate) {
        this.restTemplate = restTemplate
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
    }

    public String connectContractor(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);
        HttpEntity<?> user = new HttpEntity<>(httpHeaders);

        return restTemplate.exchange(
            "http://localhost:8083/ui//contractor/deals",
            HttpMethod.GET,
            user,
            String.class
        ).getBody();
    }

}
