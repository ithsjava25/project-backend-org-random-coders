package org.example.vet1177.controller;

import org.example.vet1177.entities.Clinic;
import org.example.vet1177.services.ClinicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClinicController.class)
@AutoConfigureMockMvc(addFilters = false) //  stänger av security
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClinicService clinicService;

    @Test
    void shouldCreateClinic() throws Exception {
        // Arrange
        Clinic clinic = new Clinic("Vet", "Street", "123");

        when(clinicService.create(any(), any(), any())).thenReturn(clinic);

        String json = """
        {
            "name": "Vet",
            "address": "Street",
            "phoneNumber": "123"
        }
        """;

        // Act + Assert
        mockMvc.perform(post("/api/clinics")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vet"))
                .andExpect(jsonPath("$.address").value("Street"))
                .andExpect(jsonPath("$.phoneNumber").value("123"));
    }
}