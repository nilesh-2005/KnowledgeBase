package com.nilesh.knowledgebase;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nilesh.knowledgebase.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
	}

	@Test
	void registerCreatesUserAndReturnsJwt() throws Exception {
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "fullName": "Admin User",
					  "email": "admin@example.com",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.token", not(blankOrNullString())))
			.andExpect(jsonPath("$.data.user.fullName").value("Admin User"))
			.andExpect(jsonPath("$.data.user.email").value("admin@example.com"))
			.andExpect(jsonPath("$.data.user.role").value("ADMIN"));
	}

	@Test
	void loginReturnsJwtForRegisteredUser() throws Exception {
		register("Admin User", "admin@example.com", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "admin@example.com",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.data.token", not(blankOrNullString())))
			.andExpect(jsonPath("$.data.user.email").value("admin@example.com"));
	}

	@Test
	void jwtTokenAuthenticatesCurrentUserRequest() throws Exception {
		String token = register("Admin User", "admin@example.com", "password123");

		mockMvc.perform(get("/api/users/me")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.fullName").value("Admin User"))
			.andExpect(jsonPath("$.data.email").value("admin@example.com"));
	}

	@Test
	void onlyAdminsCanReadUserCollection() throws Exception {
		String adminToken = register("Admin User", "admin@example.com", "password123");
		String viewerToken = register("Viewer User", "viewer@example.com", "password123");

		mockMvc.perform(get("/api/users")
				.header("Authorization", "Bearer " + viewerToken))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.success").value(false));

		mockMvc.perform(get("/api/users")
				.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.content", hasSize(2)));
	}

	private String register(String fullName, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "fullName": "%s",
					  "email": "%s",
					  "password": "%s"
					}
					""".formatted(fullName, email, password)))
			.andExpect(status().isCreated())
			.andReturn();

		return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
	}
}
