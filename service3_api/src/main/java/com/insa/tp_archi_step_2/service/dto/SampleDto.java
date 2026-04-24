package com.insa.tp_archi_step_2.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
public class SampleDto {

    private String patient;

    private String testType;

    private String sampleType;
}