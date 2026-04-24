package com.insa.tp_archi_step_2.service;

import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.insa.tp_archi_step_2.service.client.Service2HttpClient;
import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;
import com.insa.tp_archi_step_2.service.dto.SampleDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Service1ServiceImpl implements Service1Service {

    private final Random random = new Random();

    private final Service2HttpClient service2HttpClient;

    @Override
    public void registerSample(SampleDto sample) {
        RegisteredSampleDto registeredSample = setSampleId(sample);
        if (!checkBasicData(registeredSample)) {
            log.error("Basic data check failed for sample: {}", registeredSample.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided data is incorrect (not true, this is a check with a random chance of failure");
        }
        sendToAnalysis(registeredSample);
    }

    private RegisteredSampleDto setSampleId(SampleDto sample) {
        log.info("Registering sample for patient: {}", sample.getPatient());
        return RegisteredSampleDto.builder()
                .id(java.util.UUID.randomUUID())
                .patient(sample.getPatient())
                .testType(sample.getTestType())
                .sampleType(sample.getSampleType())
                .build();
    }

    private boolean checkBasicData(RegisteredSampleDto sample) {
        log.info("Check basic data for sample ID: {}", sample.getId());
        return random.nextDouble() > 0.1; // Simulate a 10% chance of failure in basic data check
    }

    private void sendToAnalysis(RegisteredSampleDto sample) {
        log.info("Sending sample for analysis: {}", sample.getId());
        service2HttpClient.sendToAnalysisService(sample);
    }

}