package com.company.exception;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public CommonException(String errorCode, String errorMessage) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
