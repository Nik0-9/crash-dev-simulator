package it.telematics.esercizio_telematics.event.service;

import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyDispatchServiceTest {

    private EmergencyDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        // Viene eseguito prima di ogni test, garantendo che ogni test sia indipendente
        dispatchService = new EmergencyDispatchService();
    }

    @Test
    @DisplayName("Dato LOW, deve restituire una sola azione: VOIP_CALL")
    void givenLowSeverity_whenDetermineActions_thenReturnsOneAction() {
        // ARRANGE: Prepara i dati di input
        CrashReportPayload event = new CrashReportPayload();
        event.setSeverity("LOW");
        event.setVehicleLicensePlate("TEST-LOW");

        // ACT: Esegui il metodo da testare
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(event);

        // ASSERT: Verifica i risultati
        assertEquals(1, actions.size(), "Dovrebbe esserci una sola azione per severità LOW");
        assertEquals("VOIP_CALL", actions.get(0).get("actionType"));
    }

    @Test
    @DisplayName("Dato MEDIUM, deve restituire due azioni: VOIP e Ambulanza")
    void givenMediumSeverity_whenDetermineActions_thenReturnsTwoActions() {
        // ARRANGE
        CrashReportPayload event = new CrashReportPayload();
        event.setSeverity("MEDIUM");
        event.setVehicleLicensePlate("TEST-MEDIUM");
        event.setLocation(Map.of("latitude", 1.0, "longitude", 1.0)); // Necessario per l'ambulanza

        // ACT
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(event);

        // ASSERT
        assertEquals(2, actions.size(), "Dovrebbero esserci due azioni per severità MEDIUM");
        assertTrue(actions.stream().anyMatch(a -> "VOIP_CALL".equals(a.get("actionType"))));
        assertTrue(actions.stream().anyMatch(a -> "DISPATCH_AMBULANCE".equals(a.get("actionType"))));
    }

    // Usiamo un test parametrizzato per testare HIGH e CRITICAL con lo stesso codice
    @ParameterizedTest
    @ValueSource(strings = {"HIGH", "CRITICAL"})
    @DisplayName("Dato HIGH o CRITICAL, deve restituire tre azioni")
    void givenHighOrCriticalSeverity_whenDetermineActions_thenReturnsThreeActions(String severity) {
        // ARRANGE
        CrashReportPayload event = new CrashReportPayload();
        event.setSeverity(severity);
        event.setVehicleLicensePlate("TEST-HIGH");
        event.setLocation(Map.of("latitude", 1.0, "longitude", 1.0));

        // ACT
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(event);

        // ASSERT
        assertEquals(3, actions.size(), "Dovrebbero esserci tre azioni per severità " + severity);
        assertTrue(actions.stream().anyMatch(a -> "VOIP_CALL".equals(a.get("actionType"))));
        assertTrue(actions.stream().anyMatch(a -> "DISPATCH_AMBULANCE".equals(a.get("actionType"))));
        assertTrue(actions.stream().anyMatch(a -> "DISPATCH_TOW_TRUCK".equals(a.get("actionType"))));
    }
}