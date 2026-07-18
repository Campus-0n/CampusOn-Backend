package com.campuson.backend.global.config.swagger;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerApiResponses {
    SwaggerApiSuccessResponse success();
    SwaggerApiFailedResponse[] errors() default {};
}
