package com.popoworld.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;


@SpringBootApplication
@EnableScheduling
public class BackendApplication {
	public static void main(String[] args) throws Exception{
		SpringApplication.run(BackendApplication.class, args);

		String publicKeyBase64Url = base64ToBase64Url("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwWYX7nXygQpWciEhbjOgsRZl34iqVToY6kw6Nn2qqFNIffixwW8C/I8ATyZqJeURMOHODiwuI996K9oYzB5wOw==");
		String publicKeyBase64Url1 = base64ToBase64Url("MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA5uzZKxluoBGOjuPn+mpZTIiOOjFi6ugtY2jxI5OdVSA==");

		System.out.println("Public Key (Base64): " + publicKeyBase64Url);
		System.out.println("Private Key (Base64): " + publicKeyBase64Url1);
	}

	public static String base64ToBase64Url(String base64) {
		return base64.replace('+', '-')
				.replace('/', '_')
				.replaceAll("=+$", "");
	}

}
