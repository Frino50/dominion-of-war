package dow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DominionOfWarApplication {
    public static void main(String[] args) {
        SpringApplication.run(DominionOfWarApplication.class, args);
    }
}