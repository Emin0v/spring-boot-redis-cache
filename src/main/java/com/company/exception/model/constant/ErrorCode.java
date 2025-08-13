package com.company.exception.model.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCode {

    public static final String CONFLICT = "conflict";
    public static final String BAD_REQUEST = "bad_request";
    public static final String FORBIDDEN = "forbidden";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String ALREADY_EXIST = "already_exist";
    public static final String DATA_NOT_FOUND = "data_not_found";
    public static final String RESOURCE_MISSING = "resource_missing";
    public static final String PARAMETER_INVALID = "parameter_invalid";
    public static final String REQUEST_INVALID = "request_body_invalid";
    public static final String INTERNAL_SERVER = "unexpected_internal_error";
}
