package com.insa.tp_archi_step_2.data.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insa.tp_archi_step_2.data.entity.Sample;

@Repository
public interface SampleRepository extends JpaRepository<Sample, UUID> {
}
