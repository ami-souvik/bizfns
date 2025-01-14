package com.bizfns.services.Exceptions;

import org.springframework.dao.DataAccessException;

public class RecordNotFoundException extends DataAccessException {

    public RecordNotFoundException(String message) {
        super(message);
    }
}