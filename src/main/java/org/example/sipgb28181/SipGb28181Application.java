package org.example.sipgb28181;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SipGb28181Application {

    public static void main(String[] args) {
        SpringApplication.run(SipGb28181Application.class, args);
    }

}
