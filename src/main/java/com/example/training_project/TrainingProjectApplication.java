package com.example.training_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableSpringDataWebSupport
@EnableAsync
public class TrainingProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingProjectApplication.class, args);
    }

}
