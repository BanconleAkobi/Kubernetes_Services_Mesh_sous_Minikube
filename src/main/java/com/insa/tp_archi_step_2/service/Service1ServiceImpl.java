package com.insa.tp_archi_step_2.service;

import org.springframework.stereotype.Service;

import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;
import com.insa.tp_archi_step_2.service.dto.SampleDto;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class Service1ServiceImpl implements Service1Service {

    @Override
    public void registerSample(SampleDto sample) {
        RegisteredSampleDto registeredSample = setSampleId(sample);
        checkBasicData(registeredSample);
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

    private void checkBasicData(RegisteredSampleDto sample) {
        log.info("Check basic data for sample ID: {}", sample.getId());
        // TODO: Logic to check the basic data of the sample
    }

    private void sendToAnalysis(RegisteredSampleDto sample) {
        log.info("Sending sample for analysis: {}", sample.getId());
        // TODO: Logic to send the sample for analysis
    }

}
