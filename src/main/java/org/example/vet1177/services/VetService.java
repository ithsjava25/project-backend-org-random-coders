package org.example.vet1177.services;

import org.example.vet1177.dto.request.vet.VetRequest;
import org.example.vet1177.dto.response.vet.VetResponse;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.entities.Vet;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.UserRepository;
import org.example.vet1177.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VetService {

    private final VetRepository vetRepository;
    private final UserRepository userRepository;

    public VetService(VetRepository vetRepository, UserRepository userRepository) {
        this.vetRepository = vetRepository;
        this.userRepository = userRepository;
    }

    public VetResponse createVet(VetRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

                if (vetRepository.existsByLicenseId(request.licenseId())) {
                    throw new BusinessRuleException("Licens-ID " + request.licenseId() + " används redan");
                }

                if(vetRepository.existsById(request.userId())){
                    throw new BusinessRuleException("Användaren är redan registrerad som veterinär");
                }

                if(user.getRole() != Role.VET) {
                    user.setRole(Role.VET);
                    userRepository.save(user);
                }

                Vet vet = new Vet(
                        user,
                        request.licenseId(),
                        request.specialization(),
                        request.bookingInfo()
                );

                Vet savedVet = vetRepository.save(vet);

                return  VetResponse.from(savedVet);
    }

@Transactional(readOnly = true)
    public List<VetResponse> getAllVets() {
        return vetRepository.findAll().stream().map(VetResponse::from).toList();

}

@Transactional(readOnly = true)
    public VetResponse getVetById(UUID userId) {
        return vetRepository.findById(userId).map(VetResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Vet", userId));
}
}
