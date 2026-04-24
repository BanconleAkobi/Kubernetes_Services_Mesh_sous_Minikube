package com.insa.tp_archi_step_2.service.dto.mapper;

import org.mapstruct.Mapper;

import com.insa.tp_archi_step_2.data.entity.Sample;
import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

@Mapper(componentModel = "spring")
public interface SampleDtoMapper {

    Sample sampleDtoToSample(SampleResultDto sampleDto);

    SampleResultDto sampleToSampleDto(Sample sample);
}
