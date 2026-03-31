package org.example.vet1177.repository;

import org.example.vet1177.entities.Vet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VetRepository extends JpaRepository<Vet, UUID> {

    //  Hittar en veterinär baserat på deras unika licens-ID.
    Optional<Vet> findByLicenseId(String licenseId);

    // Hittar alla veterinärer med en viss specialisering.
    List<Vet> findBySpecializationContainingIgnoreCase(String specialization);

    // Hittar en veterinär genom att söka på användarens e-post.
    Optional<Vet> findByUserEmail(String email);

    // Kontrollerar om ett licens-ID redan finns i systemet.
    boolean existsByLicenseId(String licenseId);

    @Override
    @EntityGraph(attributePaths = {"user", "user.clinic"})
    List<Vet> findAll();
}
