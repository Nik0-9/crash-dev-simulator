package it.telematics.esercizio_telematics.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import it.telematics.esercizio_telematics.event.service.EmergencyDispatchService;
import it.telematics.esercizio_telematics.event.service.EventLoggingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventLoggingService loggingService;

    @MockBean
    private EmergencyDispatchService dispatchService;

    // [NUOVO] Dobbiamo mockare anche questa dipendenza!
    @MockBean
    private DatabaseSequenceService sequenceService;

    @Test
    void handleCrashReport_shouldProcessEventAndReturnAccepted() throws Exception {
        // --- ARRANGE ---

        // 1. Dati di input
        CrashReportPayload payload = new CrashReportPayload();
        payload.setDeviceId("TEST-DEV-123");
        payload.setSeverity("HIGH");

        // 2. Comportamento dei Mocks (La parte aggiornata)

        // Quando il sequenceService viene chiamato, deve restituire il nostro ID di test (es. 101L)
        when(sequenceService.getNextLogId()).thenReturn(101L);

        // Il metodo di salvataggio ora è 'void'. Non restituisce nulla.
        // Dobbiamo dire a Mockito di "non fare nulla"// All'interno di EventControllerTest.java

// ...

// 2. Comportamento dei Mocks
// Quando il loggingService riceve QUALSIASI oggetto, deve restituire l'ID 101L
// [MODIFICA] Usa il nome del metodo corretto: logRawEvent
        when(loggingService.logRawEvent(any(Object.class))).thenReturn(101L);
        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/events/crash-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted()) // Verifica che lo status HTTP sia 202
                .andExpect(jsonPath("$.eventId").value(101)) // Verifica che nel JSON di risposta l'ID sia 101
                .andExpect(jsonPath("$.status").value("PROCESSED_AND_LOGGED"));
    }
}