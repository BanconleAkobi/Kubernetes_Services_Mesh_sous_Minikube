package com.insa.tp_archi_step_2.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
public class SampleResultDto extends RegisteredSampleDto {
    
    private String value;

    private String unit;
    
    private String interpretation;
}