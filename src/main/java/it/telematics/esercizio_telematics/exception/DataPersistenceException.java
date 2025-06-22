package it.telematics.esercizio_telematics.exception;

import org.springframework.dao.DataAccessException;

public class DataPersistenceException extends RuntimeException {
    public DataPersistenceException(String message, DataAccessException e) {
        super(message);
    }
}
