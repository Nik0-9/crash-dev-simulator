package it.telematics.esercizio_telematics.event.dto;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class EventLogRecord {
    private long id;
    private String user;
    private OffsetDateTime receivedAt;
    private Map<String, Object> jsonData;
}
