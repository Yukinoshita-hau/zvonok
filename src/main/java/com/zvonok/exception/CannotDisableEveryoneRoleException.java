package com.zvonok.exception;

import com.zvonok.exception_handler.annotation.ApiException;
import org.springframework.http.HttpStatus;

@ApiException(status = HttpStatus.FORBIDDEN)
public class CannotDisableEveryoneRoleException extends RuntimeException {
    public CannotDisableEveryoneRoleException(String message) {
        super(message);
    }
}


