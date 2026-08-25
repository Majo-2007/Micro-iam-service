package co.sena.iam.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// scanBasePackages covers adapters (co.sena.iam.adapter) which live in a sibling module.
// @EnableJpaRepositories/@EntityScan viven en config/JpaConfig, NO aquí: si estuvieran en la
// clase principal, @WebMvcTest (slice de solo MVC, sin JPA) fallaría con
// "No bean named 'entityManagerFactory'" porque igual las registraría.
@SpringBootApplication(scanBasePackages = "co.sena.iam")
public class IamApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(IamApiApplication.class, args);
    }
}
