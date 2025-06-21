package it.telematics.esercizio_telematics.event.service;

import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyDispatchServiceTest {

    private EmergencyDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        // Creiamo una nuova istanza pulita prima di ogni test
        dispatchService = new EmergencyDispatchService();
    }

    @Test
    @DisplayName("Dovrebbe restituire 3 azioni per un evento CRITICAL")
    void shouldReturnThreeActionsForCriticalEvent() {
        // 1. ARRANGE (Prepara i dati di input)
        CrashReportPayload criticalEvent = new CrashReportPayload();
        criticalEvent.setSeverity("CRITICAL");
        criticalEvent.setVehicleLicensePlate("TEST-001");
        criticalEvent.setLocation(Map.of("latitude", 10.0, "longitude", 20.0));

        // 2. ACT (Esegui il metodo da testare)
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(criticalEvent);

        // 3. ASSERT (Verifica i risultati)
        assertNotNull(actions);
        assertEquals(3, actions.size(), "Ci dovrebbero essere 3 azioni per un evento critico");

        // Verifica che i tipi di azione siano corretti
        assertTrue(actions.stream().anyMatch(action -> "VOIP_CALL".equals(action.get("actionType"))));
        assertTrue(actions.stream().anyMatch(action -> "DISPATCH_AMBULANCE".equals(action.get("actionType"))));
        assertTrue(actions.stream().anyMatch(action -> "DISPATCH_TOW_TRUCK".equals(action.get("actionType"))));
    }

    @Test
    @DisplayName("Dovrebbe restituire 1 azione per un evento LOW")
    void shouldReturnOneActionForLowEvent() {
        // ARRANGE
        CrashReportPayload lowEvent = new CrashReportPayload();
        lowEvent.setSeverity("LOW");
        lowEvent.setVehicleLicensePlate("TEST-002");

        // ACT
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(lowEvent);

        // ASSERT
        assertNotNull(actions);
        assertEquals(1, actions.size());
        assertEquals("VOIP_CALL", actions.get(0).get("actionType"));
    }
}