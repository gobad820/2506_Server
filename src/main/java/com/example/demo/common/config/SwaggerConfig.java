package com.example.demo.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(title = "demo 서비스 API 명세서",
        description = "스프링부트 demo 서비스 CRUD 실습 API 명세서",
        version = "v1.0.0"))
@SecurityScheme(
    name = "Bearer Authentification",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi userApi() {
        String[] paths = {"/app/users/**"};
        return GroupedOpenApi.builder()
            .group("사용자 API")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        String[] paths = {"/app/admin/**"};
        return GroupedOpenApi.builder()
            .group("관리자 API")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi auditApi() {
        String[] paths = {"/app/audit/**"};
        return GroupedOpenApi.builder()
            .group("감사 로그 API")
            .pathsToMatch(paths)
            .build();
    }

    @Bean
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/**"};

        return GroupedOpenApi.builder()
            .group("demo 서비스 API v1")
            .pathsToMatch(paths)
            .build();
    }
}
