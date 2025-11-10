package com.zvonok.exception;

import com.zvonok.exception_handler.annotation.ApiException;
import org.springframework.http.HttpStatus;

@ApiException(status = HttpStatus.FORBIDDEN)
public class CannotDeleteEveryoneRoleException extends RuntimeException {
    public CannotDeleteEveryoneRoleException(String message) {
        super(message);
    }
}


