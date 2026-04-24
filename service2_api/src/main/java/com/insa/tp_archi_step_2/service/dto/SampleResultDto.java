package com.insa.tp_archi_step_2.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
public class SampleResultDto extends RegisteredSampleDto {
    
    @JsonProperty
    private String value;

    @JsonProperty
    private String unit;
    
    @JsonProperty
    private String interpretation;
}