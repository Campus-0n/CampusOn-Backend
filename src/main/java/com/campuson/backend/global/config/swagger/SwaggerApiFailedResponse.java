package com.campuson.backend.global.config.swagger;

import com.campuson.backend.global.exception.ExceptionType;

import java.lang.annotation.*;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerApiFailedResponse {
    ExceptionType value();
}
