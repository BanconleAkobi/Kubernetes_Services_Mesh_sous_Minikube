package com.insa.tp_archi_step_2.web;

import org.springframework.web.bind.annotation.RestController;

import com.insa.tp_archi_step_2.service.Service1Service;
import com.insa.tp_archi_step_2.service.dto.SampleDto;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class Service1Controller {

    private final Service1Service service1Service;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String registerSample(@RequestBody SampleDto sample) {
        service1Service.registerSample(sample);
        return "Sample registered successfully and sent for analysis.";
    }
}