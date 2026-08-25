package co.sena.iam.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Separado de IamApiApplication a propósito: si estas anotaciones estuvieran en la clase
 * principal, @WebMvcTest las seguiría procesando (son @Import, no @Component) y el test
 * fallaría buscando un entityManagerFactory que ese slice no carga.
 */
@Configuration
@EntityScan(basePackages = "co.sena.iam.adapter.out.persistence")
@EnableJpaRepositories(basePackages = "co.sena.iam.adapter.out.persistence")
public class JpaConfig {
}
