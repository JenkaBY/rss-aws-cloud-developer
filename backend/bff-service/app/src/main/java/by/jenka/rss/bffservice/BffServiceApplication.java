package by.jenka.rss.bffservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BffServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffServiceApplication.class, args);
    }

}
