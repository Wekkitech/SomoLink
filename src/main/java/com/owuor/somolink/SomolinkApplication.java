package com.owuor.somolink;

import com.owuor.somolink.payment.config.MpesaDarajaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync

public class SomolinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SomolinkApplication.class, args);
	}

}
