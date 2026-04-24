package com.insa.tp_archi_step_2.data.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "samples")
@Data
public class Sample {

    @Id
    private UUID id;
    
    private String patient;

    private String testType;

    private String sampleType;

    private String value;

    private String unit;
    
    private String interpretation;

    private String signature;
}
