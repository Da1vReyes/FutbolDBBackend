package edu.uam.futboldb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// La autenticación es por JWT propio: se desactiva el usuario por defecto que genera Spring Security.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class FutbolDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(FutbolDbApplication.class, args);
    }
}
