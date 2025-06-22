package it.telematics.esercizio_telematics.event.service;

import it.telematics.esercizio_telematics.exception.DataPersistenceException;

import it.telematics.esercizio_telematics.event.dto.EventLogRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.RowMapper; // <-- Importante

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventLoggingService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${logging.database.sql.insert-statement}")
    private String insertSql;
    @Value("${logging.database.static-user-identifier}")
    private String staticUserIdentifier;
    @Value("${logging.database.generated-id-column}")
    private String generatedIdColumn;
    @Value("${logging.database.sql.delete-statement}")
    private String deleteSql;
    @Value("${logging.database.sql.select-critical-statement}")
    private String selectCriticalSql;
    @Value("${logging.database.sql.select-by-deviceid-statement}")
    private String selectByDeviceIdSql;

    // ... (Il resto del codice con il RowMapper e i metodi va bene così com'è)
    // Assicurati solo di aver importato DataPersistenceException e di non usare STR."..."
    // come discusso in precedenza.

    // Esempio di metodo corretto:
    private RowMapper<EventLogRecord> getEventLogRecordRowMapper() {
        return (rs, rowNum) -> {
            EventLogRecord record = new EventLogRecord();
            record.setId(rs.getLong("id"));
            record.setUser(rs.getString("user"));

            String jsonString = rs.getString("json");
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> jsonData = objectMapper.readValue(jsonString, Map.class); // <-- Ora è sicuro
                record.setJsonData(jsonData);

                if (jsonData.get("originalPayload") instanceof Map<?,?> originalPayload) {
                    if (originalPayload.get("eventTimestamp") instanceof String timestampStr) {
                        record.setReceivedAt(OffsetDateTime.parse(timestampStr));
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("Impossibile deserializzare il JSON per il record con id: {}", record.getId(), e);
            }
            return record;
        };
    }

    /**
     * Registra un evento nel database come una stringa JSON.
     * @param payload L'oggetto Java da serializzare in JSON e registrare.
     * @return L'ID generato dal database per il nuovo record.
     */
    public long logRawEvent(Object payload) {
        String jsonPayload;
        try {
            // 1. Serializza l'oggetto Java ricevuto in una stringa JSON
            jsonPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Impossibile serializzare il payload in JSON", e);
            throw new RuntimeException("Errore di serializzazione", e);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            // Creiamo un PreparedStatement che richiede la restituzione delle chiavi generate.
            PreparedStatement ps = connection.prepareStatement(insertSql, new String[]{generatedIdColumn});
            ps.setString(1, jsonPayload); // Il primo '?' è per il JSON
            ps.setString(2, staticUserIdentifier); // Il secondo '?' è per l'utente statico
            return ps;
        }, keyHolder);

        // Dopo l'esecuzione, recuperiamo la chiave dal KeyHolder.
        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new RuntimeException("L'inserimento nel DB non ha restituito un ID.");
        }

        log.info("Payload registrato con successo. ID generato dal DB: {}", generatedId.longValue());

        // Restituiamo il valore come long.
        return generatedId.longValue();
    }

/**
     * Elimina tutti i log associati a un utente specifico.
     * @param user L'utente per cui eliminare i log.
     * @return Il numero di righe eliminate.
     */
    public int deleteLogsByUser(String user) {
        log.warn("Esecuzione operazione di eliminazione di massa per l'utente: {}", user);
        int deletedRows = jdbcTemplate.update(deleteSql, user);

        log.info("Operazione completata. Eliminate {} righe per l'utente: {}", deletedRows, user);
        return deletedRows;
    }

    /**
     * Trova tutti gli eventi con severità "CRITICAL".
     * @return Una lista di EventLogRecord che rappresentano gli eventi critici.
     */
    public List<EventLogRecord> findCriticalSeverityEvents() {
        log.info("Esecuzione ricerca per eventi critici...");
        try {
            // Usiamo il metodo invece del campo
            return jdbcTemplate.query(selectCriticalSql, getEventLogRecordRowMapper());
        } catch (DataAccessException e) {
            throw new DataPersistenceException("Errore durante la ricerca di eventi critici.", e);
        }
    }

    /**
     * Trova tutti gli eventi associati a un deviceId specifico.
     * @param deviceId Il deviceId per cui cercare gli eventi.
     * @return Una lista di EventLogRecord che rappresentano gli eventi associati al deviceId.
     */
    public List<EventLogRecord> findEventsByDeviceId(String deviceId) {
        log.info("Esecuzione ricerca per deviceId: {}", deviceId);
        try {
            // Usiamo il metodo invece del campo
            return jdbcTemplate.query(selectByDeviceIdSql, getEventLogRecordRowMapper(), deviceId);
        } catch (DataAccessException e) {
            String errorMessage = String.format("Errore durante la ricerca di eventi per il deviceId: %s", deviceId);
            throw new DataPersistenceException(errorMessage, e);
        }
    }
}