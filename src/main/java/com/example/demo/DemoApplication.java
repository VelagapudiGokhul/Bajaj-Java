package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Step 1: Call generateWebhook API
		String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("name", "Gokhul Sri Sai Seshendra Chowdary Velagapudi");
		requestBody.put("regNo", "22BCE9501");
		requestBody.put("email", "seshendra.22bce9501@vitapstudent.ac.in");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

		JsonNode jsonNode = objectMapper.readTree(response.getBody());
		String webhookUrl = jsonNode.get("webhook").asText();
		String accessToken = jsonNode.get("accessToken").asText();

		System.out.println("Webhook: " + webhookUrl);
		System.out.println("AccessToken: " + accessToken);

		// Step 2: Prepare final SQL query
		String finalQuery =
				"SELECT p.AMOUNT AS SALARY, " +
						"CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
						"TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
						"d.DEPARTMENT_NAME " +
						"FROM PAYMENTS p " +
						"JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
						"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
						"WHERE DAY(p.PAYMENT_TIME) <> 1 " +
						"ORDER BY p.AMOUNT DESC " +
						"LIMIT 1;";

		// Step 3: Submit solution
		Map<String, String> body = new HashMap<>();
		body.put("finalQuery", finalQuery);

		HttpHeaders authHeaders = new HttpHeaders();
		authHeaders.setContentType(MediaType.APPLICATION_JSON);
		authHeaders.set("Authorization", accessToken); // set raw token, no "Bearer"

		HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(body, authHeaders);

		ResponseEntity<String> submitResponse = restTemplate.exchange(
				webhookUrl, // use dynamic webhook from Step 1
				HttpMethod.POST,
				submitEntity,
				String.class
		);

		System.out.println("Submission Response: " + submitResponse.getBody());
	}
}
