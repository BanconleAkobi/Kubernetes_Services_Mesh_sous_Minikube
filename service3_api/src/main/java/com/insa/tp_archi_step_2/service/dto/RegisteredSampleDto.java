package com.insa.tp_archi_step_2.service.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.experimental.SuperBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SuperBuilder
@Getter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RegisteredSampleDto extends SampleDto {

    private UUID id;
}