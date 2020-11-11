package io.holunda.example.camunda.sso;

import io.holunda.example.camunda.sso.config.ApplicationProperties;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableProcessApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class CamundaSSOExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(CamundaSSOExampleApplication.class, args);
    }
}
