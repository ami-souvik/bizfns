package com.bizfns.services.Exceptions;

import org.springframework.dao.DataAccessException;

public class NotificationNotFoundException extends DataAccessException {

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}