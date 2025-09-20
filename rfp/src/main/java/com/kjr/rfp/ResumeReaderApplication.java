package com.kjr.rfp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ComponentScan
public class ResumeReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeReaderApplication.class, args);
	}

}
