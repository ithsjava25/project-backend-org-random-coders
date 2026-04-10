package org.example.vet1177.integration.activitylog;

import org.checkerframework.checker.units.qual.C;
import org.example.vet1177.config.AwsS3Properties;
import org.example.vet1177.entities.*;
import org.example.vet1177.integration.TestDataFactory;
import org.example.vet1177.repository.*;
import org.example.vet1177.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
public class ActivityLogIntegrationTest {

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PetRepository petRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private AwsS3Properties awsS3Properties;


//    @Test
//    void should_return_logs_for_owner_only() throws Exception{
//        //Arrange
//        //1. Skapa klinik
//        Clinic clinic = new Clinic();
//        clinic.setName("Test Clinic");
//        clinic = clinicRepository.save(clinic);
//
//        //2. Skapa owner user
//        User owner = new User(
//                "Owner Test",
//                "owner@test.com",
//                "password123",
//                Role.OWNER,
//                clinic
//        );
//        owner = userRepository.save(owner);
//
//        Pet pet = new Pet(
//                owner,
//                "Doggo",
//                "Dog",
//                "Labrador",
//                LocalDate.of(2020, 1, 1),
//                new BigDecimal("20.5")
//        );
//
//        pet = petRepository.save(pet);
//
//
//        // 3. Skapa medical record
//        MedicalRecord record = new MedicalRecord();
//        record.setTitle("Test Record");
//        record.setOwner(owner);
//        record.setClinic(clinic);
//        record.setCreatedBy(owner);
//        record.setPet(pet);
//        record = medicalRecordRepository.save(record);
//
//        // 4. skapa activity logs
//        ActivityLog log1 = new ActivityLog(
//                ActivityType.CASE_CREATED,
//                "First log",
//                owner,
//                record
//        );
//
//        ActivityLog log2 = new ActivityLog(
//                ActivityType.UPDATED,
//                "Second log",
//                owner,
//                record
//        );
//
//        activityLogRepository.save(log1);
//        activityLogRepository.save(log2);
//
//        // Act och Assert
//        mockMvc.perform(get("/api/activity-logs/record/" + record.getId())
//                        .header("userId", owner.getId().toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2));
//    }
    @Test
    void should_return_logs_for_owner_only() throws Exception {

        // Arrange
        Clinic clinic = TestDataFactory.createClinic(clinicRepository);
        User owner = TestDataFactory.createOwner(userRepository, clinic);
        Pet pet = TestDataFactory.createPet(petRepository, owner);
        MedicalRecord record = TestDataFactory.createRecord(
                medicalRecordRepository, owner, clinic, pet
        );

        TestDataFactory.createLog(activityLogRepository, owner, record,
                ActivityType.CASE_CREATED, "First log");

        TestDataFactory.createLog(activityLogRepository, owner, record,
                ActivityType.UPDATED, "Second log");

        // Act & Assert
        mockMvc.perform(get("/api/activity-logs/record/" + record.getId())
                        .header("userId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void should_allow_vet_in_same_clinic_to_see_logs() throws Exception {

        Clinic clinic = TestDataFactory.createClinic(clinicRepository);

        User owner = TestDataFactory.createOwner(userRepository, clinic);
        User vet = new User(
                "Vet",
                "vet@test.com",
                "password",
                Role.VET,
                clinic
        );
        vet = userRepository.save(vet);

        Pet pet = TestDataFactory.createPet(petRepository, owner);
        MedicalRecord record = TestDataFactory.createRecord(
                medicalRecordRepository, owner, clinic, pet
        );

        TestDataFactory.createLog(activityLogRepository, owner, record,
                ActivityType.CASE_CREATED, "log");

        mockMvc.perform(get("/api/activity-logs/record/" + record.getId())
                        .header("userId", vet.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void should_filter_out_logs_for_vet_in_other_clinic() throws Exception {

        Clinic clinicA = TestDataFactory.createClinic(clinicRepository);
        Clinic clinicB = TestDataFactory.createClinic(clinicRepository);

        User owner = TestDataFactory.createOwner(userRepository, clinicA);

        User vetOtherClinic = new User(
                "Vet",
                "vet2@test.com",
                "password",
                Role.VET,
                clinicB
        );
        vetOtherClinic = userRepository.save(vetOtherClinic);

        Pet pet = TestDataFactory.createPet(petRepository, owner);
        MedicalRecord record = TestDataFactory.createRecord(
                medicalRecordRepository, owner, clinicA, pet
        );

        TestDataFactory.createLog(activityLogRepository, owner, record,
                ActivityType.CASE_CREATED, "log");

        mockMvc.perform(get("/api/activity-logs/record/" + record.getId())
                        .header("userId", vetOtherClinic.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void should_return_400_if_userId_missing() throws Exception {

        mockMvc.perform(get("/api/activity-logs/record/" + UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }
}
