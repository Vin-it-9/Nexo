package org.frontendserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Frontend Service API")
                        .description("Frontend service API documentation for Nexo application.\n\n" +
                                "**Authentication Flow:**\n" +
                                "1. Register using `/api/auth/register`\n" +
                                "2. Login using `/api/auth/login` to get JWT token\n" +
                                "3. Click the 'Authorize' button and enter your token as: `Bearer your-token`\n" +
                                "4. Now you can access protected endpoints\n" +
                                "5. Use `/api/auth/refresh-token` to get a new token when expired")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nexo Team")
                                .email("support@nexo.com")
                                .url("https://nexo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token with Bearer prefix, e.g.: Bearer eyJhbGciOiJIUzI1NiJ9..."))
                        .addParameters("AuthorizationHeader",
                                new HeaderParameter()
                                        .name("Authorization")
                                        .description("JWT token with Bearer prefix")
                                        .required(true)
                                        .schema(new Schema<String>().type("string"))
                                        .example("Bearer eyJhbGciOiJIUzI1NiJ9...")));
    }
}