package com.zvonok.exception;

public class ServerBanNotFoundException extends RuntimeException {
    public ServerBanNotFoundException(String message) {
        super(message);
    }
}

