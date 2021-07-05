package me.kuku.simbot.config;

import catcode.CatCodeUtil;
import catcode.CodeTemplate;
import catcode.StringTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Beans {

	@Bean
	public StringTemplate stringTemplate(){
		return StringTemplate.getInstance();
	}

}
