package com.example.tracker;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsAnonymousAccessToExpenses() throws Exception {
        mockMvc.perform(get("/api/expenses"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsAGarbageToken() throws Exception {
        mockMvc.perform(get("/api/expenses").header("Authorization", "Bearer not-a-jwt"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAccessWithATokenFromLogin() throws Exception {
        String token = registerAndLogin("flow@example.com", "Passw0rd!");

        mockMvc.perform(get("/api/expenses").header("Authorization", "Bearer " + token))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void rejectsADuplicateRegistration() throws Exception {
        register("dupe@example.com", "Passw0rd!").andExpect(status().isCreated());
        register("dupe@example.com", "Passw0rd!").andExpect(status().isConflict());
    }

    @Test
    void rejectsAWrongPasswordWithoutRevealingTheAccountExists() throws Exception {
        register("wrongpw@example.com", "Passw0rd!").andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(
                       new java.util.HashMap<>(java.util.Map.of(
                           "email", "wrongpw@example.com", "password", "WrongPassword!")))))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Invalid email or password"));

        // An unknown address must produce the identical response
        mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(
                       new java.util.HashMap<>(java.util.Map.of(
                           "email", "nobody@example.com", "password", "WrongPassword!")))))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void rejectsAShortPasswordOnRegistration() throws Exception {
        register("short@example.com", "abc")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void doesNotLeakAnotherUsersExpense() throws Exception {
        String ownerToken = registerAndLogin("owner@example.com", "Passw0rd!");
        String intruderToken = registerAndLogin("intruder@example.com", "Passw0rd!");

        String created = mockMvc.perform(post("/api/expenses")
                   .header("Authorization", "Bearer " + ownerToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"title\":\"Rent\",\"amount\":500.00,\"category\":\"HOUSING\"}"))
               .andExpect(status().isCreated())
               .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).get("id").asLong();

        // The row exists, but not for this user: 404, not 403, so the response
        // cannot be used to probe for other users' ids
        mockMvc.perform(get("/api/expenses/" + id)
                   .header("Authorization", "Bearer " + intruderToken))
               .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/expenses/" + id)
                   .header("Authorization", "Bearer " + intruderToken))
               .andExpect(status().isNotFound());

        // The owner still sees it, so the delete above really did nothing
        mockMvc.perform(get("/api/expenses/" + id)
                   .header("Authorization", "Bearer " + ownerToken))
               .andExpect(status().isOk());
    }

    @Test
    void listsOnlyTheCallersOwnExpenses() throws Exception {
        String aToken = registerAndLogin("list-a@example.com", "Passw0rd!");
        String bToken = registerAndLogin("list-b@example.com", "Passw0rd!");

        mockMvc.perform(post("/api/expenses")
                   .header("Authorization", "Bearer " + aToken)
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{\"title\":\"Coffee\",\"amount\":4.50,\"category\":\"GROCERIES\"}"))
               .andExpect(status().isCreated());

        mockMvc.perform(get("/api/expenses").header("Authorization", "Bearer " + bToken))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void reportsMalformedJsonAsBadRequestNotServerError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content("{not valid json"))
               .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions register(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(
                       new java.util.HashMap<>(java.util.Map.of("email", email, "password", password)))));
    }

    private String registerAndLogin(String email, String password) throws Exception {
        register(email, password).andExpect(status().isCreated());

        String body = mockMvc.perform(post("/api/auth/login")
                   .contentType(MediaType.APPLICATION_JSON)
                   .content(objectMapper.writeValueAsString(
                       new java.util.HashMap<>(java.util.Map.of("email", email, "password", password)))))
               .andExpect(status().isOk())
               .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }
}
