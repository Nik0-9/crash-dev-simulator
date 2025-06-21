package it.telematics.esercizio_telematics.event.service;

import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class EmergencyDispatchService {
    /**
     * Determina le azioni di emergenza da intraprendere e le restituisce
     * come una lista di oggetti mappa, pronta per essere serializzata in JSON.
     * @param event Il payload dell'evento di crash.
     * @return Una lista di azioni intraprese.
     */
    public List<Map<String, Object>> determineAndExecuteActions(CrashReportPayload event) {
        List<Map<String, Object>> actionsTaken = new ArrayList<>();
        String severity = Objects.toString(event.getSeverity(), "UNKNOWN").toUpperCase();

        switch (severity) {
            case "LOW":
                actionsTaken.add(callVoipToCustomer(event));
                break;
            case "MEDIUM":
                actionsTaken.add(callVoipToCustomer(event));
                actionsTaken.add(dispatchAmbulance(event));
                break;
            case "HIGH":
            case "CRITICAL":
                actionsTaken.add(callVoipToCustomer(event));
                actionsTaken.add(dispatchAmbulance(event));
                actionsTaken.add(dispatchTowTruck(event));
                break;
            default:
                log.warn("Nessuna azione automatica intrapresa per severità: {}", severity);
                break;
        }
        return actionsTaken;
    }
    private Map<String, Object> callVoipToCustomer(CrashReportPayload event) {
        String logMessage = String.format("Simulazione chiamata VOIP al cliente associato al veicolo %s.", event.getVehicleLicensePlate());
        log.info("[AZIONE] {}", logMessage);

        // Usiamo LinkedHashMap per mantenere l'ordine delle chiavi
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("actionType", "VOIP_CALL");
        action.put("target", "customer");
        action.put("timestamp", Instant.now().toString());
        action.put("details", logMessage);
        return action;
    }

    private Map<String, Object> dispatchAmbulance(CrashReportPayload event) {
        String logMessage = String.format("Simulazione invio AMBULANZA alle coordinate: Lat %s, Lon %s",
                event.getLocation().get("latitude"),
                event.getLocation().get("longitude"));
        log.warn("[AZIONE] {}", logMessage);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("actionType", "DISPATCH_AMBULANCE");
        action.put("target", "event_location");
        action.put("timestamp", Instant.now().toString());
        action.put("details", logMessage);
        return action;
    }

    private Map<String, Object> dispatchTowTruck(CrashReportPayload event) {
        String logMessage = String.format("Simulazione invio CARROATTREZZI per il veicolo %s", event.getVehicleLicensePlate());
        log.warn("[AZIONE] {}", logMessage);

        Map<String, Object> action = new LinkedHashMap<>();
        action.put("actionType", "DISPATCH_TOW_TRUCK");
        action.put("target", "event_location");
        action.put("timestamp", Instant.now().toString());
        action.put("details", logMessage);
        return action;
    }

}
