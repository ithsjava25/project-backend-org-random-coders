package org.example.vet1177.services;

import org.example.vet1177.entities.Clinic;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.ClinicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository){
        this.clinicRepository = clinicRepository;
    }

    //Skapa
    public Clinic create(String name, String address, String phoneNumber){
        Clinic clinic = new Clinic(name, address, phoneNumber);
        return clinicRepository.save(clinic);
    }

    // Läsa
    @Transactional(readOnly = true)
    public List<Clinic> getAll(){
        return clinicRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Clinic getById(UUID id){
        return clinicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", id));
    }

    // Uppdatera
    public Clinic update(UUID id, String name, String address, String phoneNumber){
        Clinic clinic = getById(id);

        clinic.setName(name);
        clinic.setAddress(address);
        clinic.setPhoneNumber(phoneNumber);

        return clinicRepository.save(clinic);
    }

    //Ta bort
    public void delete(UUID id){
        Clinic clinic = getById(id);
        clinicRepository.delete(clinic);
    }
}