package com.insa.tp_archi_step_2.service.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SampleDto {

    private String patient;

    private String testType;

    private String sampleType;
}
