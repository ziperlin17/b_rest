package com.example.bankrest.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "add JWT Access Token"
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Card Management System API")
                        .description("bank REST API.")
                        .contact(new Contact().name("ziperlin17").url("https://github.com/ziperlin17/b_rest"))
                        .license(new License().name("").url("")))
                .servers(List.of(
                        new Server().url("http://localhost:8081/api")
                ));
    }

    @Bean
    public OpenApiCustomizer globalApiResponses() {
        return openApi -> {
            ApiResponses globalResponses = new ApiResponses()
                    .addApiResponse("401", createApiResponse("Unauthorized. (Необходим токен)", "ErrorResponse"))
                    .addApiResponse("403", createApiResponse("Forbidden. (Недостаточно прав)", "ErrorResponse"))
                    .addApiResponse("500", createApiResponse("Internal Server Error.", "ErrorResponse"));

            if (openApi.getComponents().getSchemas() == null || !openApi.getComponents().getSchemas().containsKey("ErrorResponse")) {
                openApi.getComponents().addSchemas("ErrorResponse", new Schema<>()
                        .type("object")
                        .description("Standard error response")
                        .$ref("#/components/schemas/ErrorResponse")
                );
            }

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        ApiResponses existingResponses = operation.getResponses();
                        globalResponses.forEach(existingResponses::addApiResponse);
                    })
            );
        };
    }

    private ApiResponse createApiResponse(String description, String schemaRef) {
        return new ApiResponse().description(description)
                .content(new Content()
                        .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + schemaRef)))
                );
    }
}