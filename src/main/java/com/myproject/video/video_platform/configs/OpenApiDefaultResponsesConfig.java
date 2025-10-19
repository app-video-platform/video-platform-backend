package com.myproject.video.video_platform.configs;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Appends standard authentication-related responses to every documented operation.
 */
@Configuration
public class OpenApiDefaultResponsesConfig {

    private static final String JSON = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

    @Bean
    public OpenApiCustomizer globalAuthResponsesCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                if (operation.getResponses() == null) {
                    return;
                }

                var responses = operation.getResponses();

                responses.computeIfAbsent("401", code -> errorResponse("Authentication required."));
                responses.computeIfAbsent("403", code -> errorResponse("Action forbidden for the current principal."));
            }));
        };
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(JSON,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }
}
