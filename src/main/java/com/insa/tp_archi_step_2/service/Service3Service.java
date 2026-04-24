package com.insa.tp_archi_step_2.service;

import java.util.List;

import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

public interface Service3Service {

    void validateSampleResult(SampleResultDto sampleResult);

    SampleResultDto getSampleResultById(String id);

    List<SampleResultDto> getAllSampleResults();
}