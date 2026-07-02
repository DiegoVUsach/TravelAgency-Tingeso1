package com.example.demo.controller;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.example.demo.config.SecurityConfig;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserProfileDTO sampleProfile;

    @BeforeEach
    void setUp() {
        sampleProfile = new UserProfileDTO(1L, "test@example.com", "Test User",
                "+56912345678", "12345678-9", "Chilean", true, "CLIENT");
    }

    // ==================== POST /api/v1/users/sync ====================

    @Test
    void syncUser_Authenticated_ReturnsOk() throws Exception {
        when(userService.syncUser(eq("test@example.com"), eq("kc-123"), anyString()))
                .thenReturn(sampleProfile);

        mockMvc.perform(post("/api/v1/users/sync")
                        .with(jwt().jwt(j -> j
                                .subject("kc-123")
                                .claim("email", "test@example.com")
                                .claim("given_name", "Test")
                                .claim("family_name", "User"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void syncUser_WithOnlyGivenName() throws Exception {
        when(userService.syncUser(eq("test@example.com"), eq("kc-123"), eq("Test")))
                .thenReturn(sampleProfile);

        mockMvc.perform(post("/api/v1/users/sync")
                        .with(jwt().jwt(j -> j
                                .subject("kc-123")
                                .claim("email", "test@example.com")
                                .claim("given_name", "Test"))))
                .andExpect(status().isOk());
    }

    @Test
    void syncUser_WithNoNames() throws Exception {
        when(userService.syncUser(eq("test@example.com"), eq("kc-123"), isNull()))
                .thenReturn(sampleProfile);

        mockMvc.perform(post("/api/v1/users/sync")
                        .with(jwt().jwt(j -> j
                                .subject("kc-123")
                                .claim("email", "test@example.com"))))
                .andExpect(status().isOk());
    }

    @Test
    void syncUser_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/users/sync"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/v1/users/me ====================

    @Test
    void getMyProfile_ReturnsOk() throws Exception {
        when(userService.getUserProfile("test@example.com")).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void getMyProfile_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== PUT /api/v1/users/me ====================

    @Test
    void updateMyProfile_ReturnsOk() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO("Updated Name", "+56999999999", null, null);
        when(userService.updateUserProfile(eq("test@example.com"), any(UserUpdateDTO.class)))
                .thenReturn(sampleProfile);

        mockMvc.perform(put("/api/v1/users/me")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE /api/v1/users/me ====================

    @Test
    void deleteMyAccount_ReturnsOk() throws Exception {
        doNothing().when(userService).deleteUserAccount("test@example.com");

        mockMvc.perform(delete("/api/v1/users/me")
                        .with(jwt().jwt(j -> j.claim("email", "test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(content().string("User account processed for deletion."));
    }

    // ==================== Admin: GET /api/v1/users ====================

    @Test
    void getAllUsers_AsAdmin_ReturnsOk() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleProfile));

        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void getAllUsers_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== Admin: GET /api/v1/users/{id} ====================

    @Test
    void getUserById_AsAdmin_ReturnsOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/v1/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserById_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== Admin: PUT /api/v1/users/{id}/toggle-active ====================

    @Test
    void toggleUserActive_AsAdmin_ReturnsOk() throws Exception {
        when(userService.toggleUserActive(1L)).thenReturn(sampleProfile);

        mockMvc.perform(put("/api/v1/users/1/toggle-active")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void toggleUserActive_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/api/v1/users/1/toggle-active")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }
}


