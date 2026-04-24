package com.insa.tp_archi_step_2.service;

import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

public interface Service3Service {

    void validateSampleResult(SampleResultDto sampleResult);

    SampleResultDto getSampleResultById(String id);
}
