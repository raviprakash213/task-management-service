package com.epam.AsyncDataPipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AsyncDataPipelineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AsyncDataPipelineApplication.class, args);
	}

}
