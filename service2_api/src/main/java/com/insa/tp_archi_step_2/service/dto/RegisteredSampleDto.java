package com.insa.tp_archi_step_2.service.dto;

import java.util.UUID;

import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SuperBuilder
@Getter
@NoArgsConstructor
public class RegisteredSampleDto extends SampleDto {
    
    private UUID id;
}