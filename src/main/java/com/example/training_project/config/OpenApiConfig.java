package com.example.training_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI trainingProjectOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Training Project API")
                        .description(
                                "REST API для управления тренировками, спортсменами, "
                                        + "тренерами, программами и упражнениями"
                        )
                        .version("v1")
                        .contact(new Contact().name("Training Project"))
                        .license(new License().name("Demo API")));
    }
}
