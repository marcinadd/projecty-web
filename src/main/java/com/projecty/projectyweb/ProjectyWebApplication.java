package com.projecty.projectyweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
//@EnableJpaAuditing
@EnableAsync
public class ProjectyWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectyWebApplication.class, args);
    }

}