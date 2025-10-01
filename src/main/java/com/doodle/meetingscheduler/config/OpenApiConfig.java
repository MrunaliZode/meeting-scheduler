package com.doodle.meetingscheduler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setDescription("Meeting scheduling server");
        server.setUrl("http://localhost:8080");
        return new OpenAPI()
                .info(new Info()
                        .title("Meeting Scheduler API")
                        .version("1.0")
                        .description("API documentation for meeting scheduler"))
                .addServersItem(server);
    }
}

