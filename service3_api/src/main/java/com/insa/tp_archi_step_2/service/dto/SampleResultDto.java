package com.insa.tp_archi_step_2.service.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SampleResultDto extends RegisteredSampleDto {
    
    private String value;

    private String unit;
    
    private String interpretation;
}