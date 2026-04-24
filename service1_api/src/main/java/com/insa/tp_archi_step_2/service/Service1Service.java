package com.insa.tp_archi_step_2.service;

import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;
import com.insa.tp_archi_step_2.service.dto.SampleDto;

public interface Service1Service {
    RegisteredSampleDto registerSample(SampleDto entity);
}