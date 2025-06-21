package it.telematics.esercizio_telematics.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import it.telematics.esercizio_telematics.event.service.EmergencyDispatchService;
import it.telematics.esercizio_telematics.event.service.EventLoggingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Carica solo lo strato Web per EventController e le sue dipendenze (che verranno mockate)
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc; // Per simulare chiamate HTTP

    @Autowired
    private ObjectMapper objectMapper; // Per convertire oggetti in JSON

    @MockBean // Crea una versione "finta" di questo servizio
    private EventLoggingService loggingService;

    @MockBean // Crea una versione "finta" di questo servizio
    private EmergencyDispatchService dispatchService;

    @Test
    @DisplayName("POST /crash-report dovrebbe elaborare e salvare l'evento e restituire 202")
    void handleCrashReport_shouldProcessAndLogEvent_andReturnAccepted() throws Exception {
        // ARRANGE
        CrashReportPayload payload = new CrashReportPayload();
        payload.setDeviceId("TEST-123");
        payload.setSeverity("HIGH");

        // Definisci il comportamento dei "finti" servizi
        // 1. Quando il dispatchService viene chiamato, fagli restituire una lista vuota
        when(dispatchService.determineAndExecuteActions(any(CrashReportPayload.class)))
                .thenReturn(Collections.emptyList());

        // 2. Quando il loggingService viene chiamato per salvare, fagli restituire l'ID 101
        when(loggingService.logRawEvent(any(Map.class))).thenReturn(101L);

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/events/crash-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isAccepted()) // Verifica status HTTP 202
                .andExpect(jsonPath("$.eventId").value(101)) // Verifica il corpo della risposta
                .andExpect(jsonPath("$.status").value("PROCESSED_AND_LOGGED"));

        // VERIFY (Opzionale ma utile): Verifica che i metodi dei mock siano stati chiamati
        verify(dispatchService).determineAndExecuteActions(any(CrashReportPayload.class));
        verify(loggingService).logRawEvent(any(Map.class));
    }

    @Test
    @DisplayName("DELETE /logs dovrebbe eliminare i log e restituire 200 OK")
    void deleteUserLogs_shouldCallServiceAndReturnOk() throws Exception {
        // ARRANGE
        String testUser = "test_user";
        int deletedRows = 5;

        // Quando il loggingService viene chiamato per eliminare, fagli restituire il numero 5
        when(loggingService.deleteLogsByUser(testUser)).thenReturn(deletedRows);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/v1/events/logs")
                        .param("user", testUser)) // Aggiunge il parametro ?user=test_user
                .andExpect(status().isOk()) // Verifica status HTTP 200
                .andExpect(jsonPath("$.user").value(testUser))
                .andExpect(jsonPath("$.deletedCount").value(deletedRows));

        // VERIFY
        verify(loggingService).deleteLogsByUser(testUser);
    }
}