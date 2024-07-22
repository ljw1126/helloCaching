package hello.caching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;


@EnableCaching
@SpringBootApplication
public class HelloCachingApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloCachingApplication.class, args);
	}

}
