package com.insa.tp_archi_step_2.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insa.tp_archi_step_2.service.Service2Service;
import com.insa.tp_archi_step_2.service.dto.RegisteredSampleDto;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/service2")
@RequiredArgsConstructor
public class Service2Controller {

    private final Service2Service service2Service;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public String receiveSampleForAnalysis(@RequestBody RegisteredSampleDto sample) {
        service2Service.processSample(sample);
        return "Sample received and analyzed.";
    }
}
