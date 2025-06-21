package it.telematics.esercizio_telematics.event.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CrashReportPayload {
    private String deviceId;
    private String vehicleLicensePlate;
    private OffsetDateTime eventTimestamp;
    private Map<String, Double> location;
    private String severity;
    @JsonProperty("gforce")
    @JsonAlias("gForce")
    private Float gForce;

}
