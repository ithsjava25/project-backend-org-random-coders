package org.example.vet1177.repository;

import org.example.vet1177.entities.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClinicRepository extends JpaRepository<Clinic, UUID> {
}
