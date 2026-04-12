package org.example.vet1177.integration.clinic;

import org.example.vet1177.config.AwsS3Properties;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class ClinicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClinicRepository clinicRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private AwsS3Properties awsS3Properties;

    @BeforeEach
    void setUp() {
        clinicRepository.deleteAll();
    }

    @Test
    void should_create_clinic() throws Exception {
        String body = """
                {
                  "name": "Clinic A",
                  "address": "Address A",
                  "phoneNumber": "0701234567"
                }
                """;

        mockMvc.perform(post("/api/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Clinic A"))
                .andExpect(jsonPath("$.address").value("Address A"))
                .andExpect(jsonPath("$.phoneNumber").value("0701234567"));

        assert clinicRepository.findAll().size() == 1;
    }

    @Test
    void should_return_all_clinics() throws Exception {
        clinicRepository.save(new Clinic("C1", "A1", "111"));
        clinicRepository.save(new Clinic("C2", "A2", "222"));

        mockMvc.perform(get("/api/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void should_return_clinic_by_id() throws Exception {
        Clinic clinic = clinicRepository.save(new Clinic("Test", "Addr", "999"));

        mockMvc.perform(get("/api/clinics/" + clinic.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clinic.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.address").value("Addr"))
                .andExpect(jsonPath("$.phoneNumber").value("999"));
    }

    @Test
    void should_update_clinic() throws Exception {
        Clinic clinic = clinicRepository.save(new Clinic("Old", "OldAddr", "111"));

        String body = """
                {
                  "name": "New",
                  "address": "NewAddr",
                  "phoneNumber": "222"
                }
                """;

        mockMvc.perform(put("/api/clinics/" + clinic.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.address").value("NewAddr"))
                .andExpect(jsonPath("$.phoneNumber").value("222"));

        Clinic updated = clinicRepository.findById(clinic.getId()).orElseThrow();
        assert updated.getName().equals("New");
        assert updated.getAddress().equals("NewAddr");
        assert updated.getPhoneNumber().equals("222");
    }

    @Test
    void should_delete_clinic() throws Exception {
        Clinic clinic = clinicRepository.save(new Clinic("Delete", "Addr", "123"));

        mockMvc.perform(delete("/api/clinics/" + clinic.getId()))
                .andExpect(status().isNoContent());

        assert clinicRepository.findById(clinic.getId()).isEmpty();
    }

    @Test
    void should_return_400_on_invalid_create() throws Exception {
        String body = """
                {
                  "name": "",
                  "address": "",
                  "phoneNumber": ""
                }
                """;

        mockMvc.perform(post("/api/clinics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_404_when_clinic_not_found() throws Exception {
        mockMvc.perform(get("/api/clinics/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}