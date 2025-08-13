package com.company.exception.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage {

    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Unexpected internal server error occurs";
    public static final String PRODUCT_SERVICE_CODE_NOT_FOUND_MESSAGE =
            "Product Service Code with {0} - {1} not found.";
    public static final String PRODUCT_SERVICE_CODE_ALREADY_EXISTS_MESSAGE =
            "Product Service Code with {0} - {1} already exists.";
}
