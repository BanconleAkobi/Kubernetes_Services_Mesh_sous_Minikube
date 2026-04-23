package com.insa.tp_archi_step_2.service;

import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

@Service
public class Service3ServiceImpl implements Service3Service {

    private final Random random = new Random();

    public void validateSampleResult(SampleResultDto sampleResult) throws ResponseStatusException {
        if (!isValidResult()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sample result.");
        }

        applyBiologicSignature(sampleResult);


    }

    private boolean isValidResult() {
        return random.nextDouble() > 0.1;
    }

    private void applyBiologicSignature(SampleResultDto sampleResult) {
        //TODO: Implement biologic signature application logic here
    }
}
