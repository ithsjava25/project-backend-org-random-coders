package org.example.vet1177.services;

import org.example.vet1177.entities.Clinic;
import org.example.vet1177.repository.ClinicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository){
        this.clinicRepository = clinicRepository;
    }

    public Clinic createClinic(String name, String address, String phoneNumber){
        Clinic clinic = new Clinic(name, address, phoneNumber);
        return clinicRepository.save(clinic);
    }

    public List<Clinic> getAllClinics(){
        return clinicRepository.findAll();
    }

    public Clinic getById(UUID id){
        return clinicRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Clinic not found"));
    }

}
