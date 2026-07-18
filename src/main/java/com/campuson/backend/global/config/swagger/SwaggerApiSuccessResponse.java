package com.campuson.backend.global.config.swagger;

import java.lang.annotation.*;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface SwaggerApiSuccessResponse {
    Class<?> response() default Void.class; // 명시 안 하면 컨트롤러 반환타입에서 springdoc이 자동으로 스키마 추론
    String description() default "";
}