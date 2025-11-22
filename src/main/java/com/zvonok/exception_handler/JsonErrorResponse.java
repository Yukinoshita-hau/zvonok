package com.zvonok.exception_handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JsonErrorResponse {

    private String message;
    private int status;
}
