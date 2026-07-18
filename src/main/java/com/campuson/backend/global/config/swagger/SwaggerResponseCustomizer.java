package com.campuson.backend.global.config.swagger;

import com.campuson.backend.global.exception.ExceptionType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class SwaggerResponseCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        SwaggerApiResponses annotation = handlerMethod.getMethodAnnotation(SwaggerApiResponses.class);
        if (annotation == null) {
            return operation;
        }

        ApiResponses apiResponses = operation.getResponses();
        if (apiResponses == null) {
            apiResponses = new ApiResponses();
            operation.setResponses(apiResponses);
        }

        applySuccess(apiResponses, annotation.success());
        applyErrors(apiResponses, annotation.errors());

        return operation;
    }

    private void applySuccess(ApiResponses apiResponses, SwaggerApiSuccessResponse success) {
        // 스키마는 springdoc이 컨트롤러 반환 타입(ResponseEntity<ResponseBody<T>>)으로 이미 만들어둠 -> description만 덮어쓰기
        io.swagger.v3.oas.models.responses.ApiResponse successResponse = apiResponses.get("200");
        if (successResponse == null) {
            successResponse = new io.swagger.v3.oas.models.responses.ApiResponse();
            apiResponses.addApiResponse("200", successResponse);
        }
        successResponse.setDescription(success.description());
    }

    private void applyErrors(ApiResponses apiResponses, SwaggerApiFailedResponse[] errors) {
        for (SwaggerApiFailedResponse error : errors) {
            ExceptionType type = error.value();
            String status = String.valueOf(type.getStatus().value());
            String desc = "`" + type.name() + "` (" + type.getCode() + ") - " + type.getMessage();

            io.swagger.v3.oas.models.responses.ApiResponse existing = apiResponses.get(status);
            if (existing != null) {
                String merged = (existing.getDescription() == null ? "" : existing.getDescription() + "<br/>") + desc;
                existing.setDescription(merged);
            } else {
                apiResponses.addApiResponse(status, new io.swagger.v3.oas.models.responses.ApiResponse().description(desc));
            }
        }
    }
}
