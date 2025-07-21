package com.auth.services;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * class for connect deal method
 */
@Service
public class DealConnect {

    private final RestTemplate restTemplate;

    public DealConnect(RestTemplateBuilder restTemplate) {
        this.restTemplate = restTemplate
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .build();
    }

    /**
     * connect deal service and send access token
     * @param token access token
     * @return header response
     */
    public String connectDeal(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);
        HttpEntity<?> user = new HttpEntity<>(httpHeaders);

        return restTemplate.exchange(
            "http://localhost:8081/ui/deals", HttpMethod.GET,
            user,
            String.class
        ).getBody();
    }

}
