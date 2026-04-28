package com.insa.tp_archi_step_2.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import com.insa.tp_archi_step_2.service.client.Service3HttpClient;
import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;
import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class Service2ServiceImpl implements Service2Service {

    private final Random random = new Random(); 

    private final Service3HttpClient service3HttpClient;
    
    public void processSample(RegisteredSampleDto sample) {
        simulateProcessingTime(sample, 2000);
        SampleResultDto result = generateResult(sample);
        sendResultForValidation(result);
    }

    private void simulateProcessingTime(RegisteredSampleDto sample, int ms) {
        log.info("Processing sample ID: {} for {} ms", sample.getId(), ms);
        try {
            Thread.sleep(ms); // Simulate processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private SampleResultDto generateResult(RegisteredSampleDto sample) {
        log.info("Generating result for sample ID: {}", sample.getId());
        return SampleResultDto.builder()
                .id(sample.getId())
                .patient(sample.getPatient())
                .testType(sample.getTestType())
                .sampleType(sample.getSampleType())
                .value(String.format("%.2f", random.nextDouble(0, 100)))
                .unit(getRandomUnit())
                .interpretation(getRandomInterpretation())
                .build();
    }

    private String getRandomInterpretation() {
        String[] interpretations = {"Normal", "High", "Low", "Positive", "Negative"};
        return interpretations[random.nextInt(interpretations.length)];
    }

    private String getRandomUnit() {
        String[] units = {"mg/dL", "mmol/L", "g/L"};
        return units[random.nextInt(units.length)];
    }

    private void sendResultForValidation(SampleResultDto result) {
        log.info("Sending result for validation: {}", result.getId());
        service3HttpClient.sendToValidationService(result);
    }
}