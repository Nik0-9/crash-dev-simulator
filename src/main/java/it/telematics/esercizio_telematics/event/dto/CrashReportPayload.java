package it.telematics.esercizio_telematics.event.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class CrashReportPayload {
    @NotBlank(message = "Il campo 'deviceId' non può essere vuoto")
    private String deviceId;

    private String vehicleLicensePlate; // Opzionale

    @NotNull(message = "Il campo 'eventTimestamp' non può essere nullo")
    private OffsetDateTime eventTimestamp;

    @NotNull(message = "Il campo 'location' non può essere nullo")
    private Map<String, Double> location;

    @NotBlank(message = "Il campo 'severity' non può essere vuoto")
    private String severity;

    @JsonProperty("gforce")
    @JsonAlias("gForce")
    private Double gForce; // Opzionale
}
