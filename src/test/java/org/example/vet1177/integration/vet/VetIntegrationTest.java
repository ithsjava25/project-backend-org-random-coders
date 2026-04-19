package org.example.vet1177.integration.vet;

import org.example.vet1177.config.AwsS3Properties;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.entities.Vet;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.integration.TestDataFactory;
import org.example.vet1177.policy.AdminPolicy;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.UserRepository;
import org.example.vet1177.repository.VetRepository;
import org.example.vet1177.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class VetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VetRepository vetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private AwsS3Properties awsS3Properties;

    @MockitoBean
    private AdminPolicy adminPolicy;

    @BeforeEach
    void setUp() {
        vetRepository.deleteAll();
        userRepository.deleteAll();
        clinicRepository.deleteAll();
    }

    @BeforeEach
    void resetMocks() {
        reset(adminPolicy);
    }

    private User createAdmin(Clinic clinic) {
        User admin = new User(
                "Admin",
                UUID.randomUUID() + "@test.com",
                "password123",
                Role.ADMIN,
                clinic
        );
        return userRepository.save(admin);
    }

    private User createVetUser(Clinic clinic) {
        User vetUser = new User(
                "Vet User",
                UUID.randomUUID() + "@test.com",
                "password123",
                Role.VET,
                clinic
        );
        return userRepository.save(vetUser);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Test
    void VT_P0_01_admin_can_create_vet() throws Exception {
        doNothing().when(adminPolicy).requireAdmin(any());
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User admin = createAdmin(clinic);
        User targetUser = TestDataFactory.createOwner(userRepository, clinic);

        String body = """
                {
                  "userId": "%s",
                  "licenseId": "LIC-1001",
                  "specialization": "Surgery",
                  "bookingInfo": "Weekdays only"
                }
                """.formatted(targetUser.getId());

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(targetUser.getId().toString()))
                .andExpect(jsonPath("$.licenseId").value("LIC-1001"))
                .andExpect(jsonPath("$.specialization").value("Surgery"))
                .andExpect(jsonPath("$.bookingInfo").value("Weekdays only"));

        assertEquals(1, vetRepository.count());

        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.VET, updatedUser.getRole());

        verify(adminPolicy).requireAdmin(admin);
    }
    @Test
    void VT_P0_02_non_admin_cannot_create_vet_as_vet() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User nonAdminVet = createVetUser(clinic);
        User targetUser = TestDataFactory.createOwner(userRepository, clinic);

        String body = """
                {
                  "userId": "%s",
                  "licenseId": "LIC-1003",
                  "specialization": "Cardiology",
                  "bookingInfo": "Tue-Thu"
                }
                """.formatted(targetUser.getId());

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(nonAdminVet)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(adminPolicy, never()).requireAdmin(any());
        assertEquals(0, vetRepository.count());
    }

    @Test
    void VT_P0_03_duplicate_license_id_rejected() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User admin = createAdmin(clinic);

        User firstUser = TestDataFactory.createOwner(userRepository, clinic);
        User secondUser = TestDataFactory.createOwner(userRepository, clinic);

        Vet existingVet = new Vet(firstUser, "LIC-DUP-1", "Surgery", "Morning");
        vetRepository.save(existingVet);

        String body = """
                {
                  "userId": "%s",
                  "licenseId": "LIC-DUP-1",
                  "specialization": "Dentistry",
                  "bookingInfo": "Afternoon"
                }
                """.formatted(secondUser.getId());

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity());


        assertEquals(1, vetRepository.count());
        assertTrue(vetRepository.existsByLicenseId("LIC-DUP-1"));
    }

    @Test
    void VT_P0_04_missing_target_user_rejected() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User admin = createAdmin(clinic);

        String body = """
                {
                  "userId": "%s",
                  "licenseId": "LIC-404",
                  "specialization": "Neurology",
                  "bookingInfo": "By appointment"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        assertEquals(0, vetRepository.count());
    }

    @Test
    void VT_P0_05_get_all_vets_includes_clinic_name() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User user = createVetUser(clinic);

        Vet vet = new Vet(user, "LIC-GET-ALL", "Orthopedics", "Mon-Wed");
        vetRepository.save(vet);

        mockMvc.perform(get("/api/vets")
                        .with(authentication(auth(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(user.getId().toString()))
                .andExpect(jsonPath("$[0].licenseId").value("LIC-GET-ALL"))
                .andExpect(jsonPath("$[0].clinicName").value(clinic.getName()));
    }

    @Test
    void VT_P0_06_get_vet_by_id_returns_expected_vet() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User user = createVetUser(clinic);

        Vet vet = new Vet(user, "LIC-BY-ID", "Internal medicine", "Fri");
        vetRepository.save(vet);

        mockMvc.perform(get("/api/vets/" + user.getId())
                        .with(authentication(auth(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.licenseId").value("LIC-BY-ID"))
                .andExpect(jsonPath("$.clinicName").value(clinic.getName()));
    }

    @Test
    void VT_P0_06_get_vet_by_id_returns_404_for_unknown_id() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User user = createVetUser(clinic);

        mockMvc.perform(get("/api/vets/" + UUID.randomUUID())
                        .with(authentication(auth(user))))
                .andExpect(status().isNotFound());
    }

    @Test
    void VT_P1_01_validation_error_for_invalid_vet_request() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User admin = createAdmin(clinic);

        String body = """
                {
                  "userId": null,
                  "licenseId": "",
                  "specialization": "Valid specialization",
                  "bookingInfo": "Valid booking info"
                }
                """;

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertEquals(0, vetRepository.count());
    }

    @Test
    void VT_P1_02_existing_vet_role_remains_stable() throws Exception {
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User admin = createAdmin(clinic);
        User targetUser = createVetUser(clinic); // redan VET, men ännu ingen vet_details-rad

        String body = """
                {
                  "userId": "%s",
                  "licenseId": "LIC-STABLE",
                  "specialization": "Exotics",
                  "bookingInfo": "Weekends"
                }
                """.formatted(targetUser.getId());

        mockMvc.perform(post("/api/vets")
                        .with(authentication(auth(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(targetUser.getId().toString()))
                .andExpect(jsonPath("$.licenseId").value("LIC-STABLE"));

        User reloadedUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals(Role.VET, reloadedUser.getRole());
        assertEquals(1, vetRepository.count());
    }
}