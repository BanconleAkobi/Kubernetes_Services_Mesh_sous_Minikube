package com.insa.tp_archi_step_2.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
public class SampleDto {

    @JsonProperty
    private String patient;

    @JsonProperty
    private String testType;

    @JsonProperty
    private String sampleType;
}