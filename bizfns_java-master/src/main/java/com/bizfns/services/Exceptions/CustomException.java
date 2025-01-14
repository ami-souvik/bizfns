package com.bizfns.services.Exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomException extends Exception {
    public CustomException(String message) {
        super(message);
    }
}
