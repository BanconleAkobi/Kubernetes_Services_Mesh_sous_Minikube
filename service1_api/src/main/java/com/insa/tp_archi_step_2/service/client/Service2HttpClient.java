package com.insa.tp_archi_step_2.service.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class Service2HttpClient {

    private static final String SERVICE_2_URL = "http://service2:9001/api";
    
    public ResponseEntity<String> sendToAnalysisService(RegisteredSampleDto sample) throws ResponseStatusException {
        RestClient restClient = RestClient.builder()
            .baseUrl(SERVICE_2_URL)
            .build();

        ResponseEntity<String> response = restClient.post()
            .uri("/analyze")
            .contentType(MediaType.APPLICATION_JSON)
            .body(sample)
            .retrieve()
            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                throw new ResponseStatusException(res.getStatusCode(), "An error occurred while sending the sample for analysis.");
            })
            .toEntity(String.class);

        log.info("Sample sent for analysis: {}", sample.getId());
        return response;
    }
}
