package it.telematics.esercizio_telematics.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.telematics.esercizio_telematics.event.dto.CrashReportPayload;
import it.telematics.esercizio_telematics.event.dto.EventLogRecord;
import it.telematics.esercizio_telematics.event.service.EmergencyDispatchService;
import it.telematics.esercizio_telematics.event.service.EventLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventLoggingService loggingService;
    private final EmergencyDispatchService dispatchService;
    private final ObjectMapper objectMapper; // Inietta l'ObjectMapper per la conversione

    @PostMapping("/crash-report")
    public ResponseEntity<Map<String, Object>> handleCrashReport(@RequestBody CrashReportPayload payload) {
        log.info("Inizio elaborazione evento per dispositivo: {}", payload.getDeviceId());

        // 1. Dati di Audit
        Map<String, Object> auditData = new LinkedHashMap<>();
        auditData.put("receivedAt", Instant.now().toString());

        // 2. Dati di Elaborazione (Processing)
        List<Map<String, Object>> actions = dispatchService.determineAndExecuteActions(payload);
        Map<String, Object> processingData = new LinkedHashMap<>();
        processingData.put("status", "COMPLETED");
        processingData.put("completedAt", Instant.now().toString());
        processingData.put("actions", actions);

        // 3. Assembla l'oggetto contenitore finale
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("audit", auditData);
        logEntry.put("originalPayload", payload); // Il payload originale, intatto!
        logEntry.put("processing", processingData);


        // 4. Salva l'intero oggetto contenitore nel database
        long eventId = loggingService.logRawEvent(logEntry);
        log.info("Log completo registrato con ID: {}", eventId);

        // 5. Rispondi al client
        Map<String, Object> response = Map.of(
                "eventId", eventId,
                "status", "PROCESSED_AND_LOGGED"
        );
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/critical-reports")
    public ResponseEntity<List<EventLogRecord>> getCriticalReports() {
        List<EventLogRecord> criticalEvents = loggingService.findCriticalSeverityEvents();
        return ResponseEntity.ok(criticalEvents);
    }

    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, Object>> deleteUserLogs(@RequestParam("user") String user) {
        if (user == null || user.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Il parametro 'user' non può essere vuoto."));
        }

        int deletedCount = loggingService.deleteLogsByUser(user);

        Map<String, Object> response = Map.of(
                "user", user,
                "deletedCount", deletedCount
        );

        return ResponseEntity.ok(response);
    }
}
