package com.bizfns.services;

import com.bizfns.services.Serviceimpl.EmailSenderService;
import com.bizfns.services.Serviceimpl.PKCS7Padding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import javax.sql.DataSource;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class ServicesApplication {

	public static void main(String[] args) {

		SpringApplication.run(ServicesApplication.class, args);

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:postgresql://bizfns_pgsql:5432/Bizfns");
		dataSource.setUsername("admin");
		dataSource.setPassword("example");
		return dataSource;
	}

}
