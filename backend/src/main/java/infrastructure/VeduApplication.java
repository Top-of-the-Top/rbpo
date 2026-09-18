package infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"domain", "application", "infrastructure"})
public class VeduApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeduApplication.class, args);
    }
}
