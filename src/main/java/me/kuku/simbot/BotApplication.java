package me.kuku.simbot;

import love.forte.simbot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableSimbot
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class BotApplication {
	public static void main(String[] args) {
		SpringApplication.run(BotApplication.class, args);
	}
}
