package com.insa.tp_archi_step_2.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insa.tp_archi_step_2.service.Service3Service;
import com.insa.tp_archi_step_2.service.dto.SampleResultDto;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/service3")
@RequiredArgsConstructor
public class Service3Controller {

    private final Service3Service service3Service;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String validateSampleResult(@RequestBody SampleResultDto sampleResult) {
        service3Service.validateSampleResult(sampleResult);
        return "Sample result validated successfully.";
    }

    @GetMapping
    public SampleResultDto getSampleResult(@RequestParam String id) {
        return service3Service.getSampleResultById(id);
    }
    
}
