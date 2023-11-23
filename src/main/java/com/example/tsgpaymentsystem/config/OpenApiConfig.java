package com.example.tsgpaymentsystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SecurityScheme(
        name = "JWTAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Configuration
@OpenAPIDefinition
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setEmail("samdim2011@mail.ru");
        contact.setName("SaintAmbrozii");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");



        return new OpenAPI()
                .info(
                        new Info().description("TSG Payment System Api")
                                .title("testing API")
                                .version("1.0.0")
                                .contact(contact)
                                .license(mitLicense)

                );
    }

}
