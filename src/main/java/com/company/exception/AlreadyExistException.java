package com.company.exception;

import static com.company.exception.model.constant.ErrorCode.ALREADY_EXIST;

import java.text.MessageFormat;

public class AlreadyExistException extends CommonException {

    public AlreadyExistException(String message) {
        super(ALREADY_EXIST, message);
    }

    public static AlreadyExistException of(String message, Object... args) {
        return new AlreadyExistException(MessageFormat.format(message, args));
    }

}
