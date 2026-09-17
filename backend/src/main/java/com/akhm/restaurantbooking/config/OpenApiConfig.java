package com.akhm.restaurantbooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI restaurantBookingOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Booking API")
                        .description(
                                "API REST pour la gestion des restaurants, " +
                                        "tables, horaires, utilisateurs et réservations."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ahmed Khemiri")));
    }
}