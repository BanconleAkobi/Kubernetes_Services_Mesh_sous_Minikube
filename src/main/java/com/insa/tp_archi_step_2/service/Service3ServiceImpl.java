package com.insa.tp_archi_step_2.service;

import java.util.Random;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.insa.tp_archi_step_2.data.entity.Sample;
import com.insa.tp_archi_step_2.data.repository.SampleRepository;
import com.insa.tp_archi_step_2.service.dto.SampleResultDto;
import com.insa.tp_archi_step_2.service.dto.mapper.SampleDtoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Service3ServiceImpl implements Service3Service {

    private final Random random = new Random();

    private final SampleRepository sampleRepository;

    private final SampleDtoMapper sampleDtoMapper;

    public void validateSampleResult(SampleResultDto sampleResult) throws ResponseStatusException {
        if (!isValidResult()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sample result.");
        }

        Sample sample = applyBiologicSignature(sampleResult);

        sampleRepository.save(sample);
    }

    private boolean isValidResult() {
        return random.nextDouble() > 0.1;
    }

    private Sample applyBiologicSignature(SampleResultDto sampleResult) {
        Sample sample = sampleDtoMapper.sampleDtoToSample(sampleResult);
        sample.setSignature(random.ints(48, 122)
                .filter(i -> (i >= 48 && i <= 57) || (i >= 65 && i <= 90) || (i >= 97 && i <= 122))
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString());
        return sample;
    }

    public SampleResultDto getSampleResultById(String id) throws ResponseStatusException {
        return sampleRepository.findById(UUID.fromString(id))
                .map(sampleDtoMapper::sampleToSampleDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample result not found."));
    }
}
