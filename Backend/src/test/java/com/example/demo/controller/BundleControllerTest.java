package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.BundleService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.example.demo.config.SecurityConfig;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(BundleController.class)
@Import(SecurityConfig.class)
class BundleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BundleService bundleService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    private BundleEntity sampleBundle;

    @BeforeEach
    void setUp() {
        sampleBundle = new BundleEntity();
        sampleBundle.setIdBundle(1L);
        sampleBundle.setNameBundle("Caribe");
        sampleBundle.setDestinationBundle("Punta Cana");
        sampleBundle.setDescriptionBundle("Playa y sol");
        sampleBundle.setPriceBundle(500000);
        sampleBundle.setAvailableSlotsBundle(20);
        sampleBundle.setStartDateBundle(LocalDate.of(2027, 1, 1));
        sampleBundle.setEndDateBundle(LocalDate.of(2027, 1, 11));
        sampleBundle.setDurationBundle(10);
        sampleBundle.setStateBundle(BundleState.AVAILABLE);
        sampleBundle.setExperienceTypes(Set.of(ExperienceTypeState.RELAX));
        sampleBundle.setSeasonType(SeasonTypeState.SUMMER);
        sampleBundle.setCategoryType(CategoryTypeState.PREMIUM);
    }

    // ==================== GET /api/v1/bundle (public) ====================

    @Test
    void getAllBundles_ReturnsOk() throws Exception {
        when(bundleService.findByPriceBundleGreaterThan(0)).thenReturn(List.of(sampleBundle));

        mockMvc.perform(get("/api/v1/bundle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nameBundle").value("Caribe"));
    }

    // ==================== GET /api/v1/bundle/{id} (public) ====================

    @Test
    void getBundleById_ReturnsOk() throws Exception {
        when(bundleService.getBundleById(1L)).thenReturn(sampleBundle);

        mockMvc.perform(get("/api/v1/bundle/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameBundle").value("Caribe"));
    }

    @Test
    void getBundleById_NotFound() throws Exception {
        when(bundleService.getBundleById(999L)).thenThrow(new RuntimeException("Bundle not found with id: 999"));

        mockMvc.perform(get("/api/v1/bundle/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /api/v1/bundle/sort/greaterThan/{price} (public) ====================

    @Test
    void findByPriceGreaterThan_ReturnsOk() throws Exception {
        when(bundleService.findByPriceBundleGreaterThan(100000)).thenReturn(List.of(sampleBundle));

        mockMvc.perform(get("/api/v1/bundle/sort/greaterThan/100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priceBundle").value(500000));
    }

    // ==================== POST /api/v1/bundle (admin only) ====================

    @Test
    void saveBundle_AsAdmin_ReturnsOk() throws Exception {
        when(bundleService.saveBundle(any(BundleEntity.class))).thenReturn(sampleBundle);

        mockMvc.perform(post("/api/v1/bundle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameBundle").value("Caribe"));
    }

    @Test
    void saveBundle_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/bundle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveBundle_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/bundle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveBundle_InvalidData_ReturnsBadRequest() throws Exception {
        when(bundleService.saveBundle(any(BundleEntity.class)))
                .thenThrow(new IllegalArgumentException("Package name is required."));

        mockMvc.perform(post("/api/v1/bundle")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isBadRequest());
    }

    // ==================== DELETE /api/v1/bundle/{id} (admin only) ====================

    @Test
    void deleteBundle_AsAdmin_ReturnsNoContent() throws Exception {
        doNothing().when(bundleService).deleteBundle(1L);

        mockMvc.perform(delete("/api/v1/bundle/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBundle_AsClient_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/bundle/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/v1/bundle/{id} (admin only) ====================

    @Test
    void updateBundle_AsAdmin_ReturnsOk() throws Exception {
        when(bundleService.updateBundle(eq(1L), any(BundleEntity.class))).thenReturn(sampleBundle);

        mockMvc.perform(put("/api/v1/bundle/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nameBundle").value("Caribe"));
    }

    @Test
    void updateBundle_Conflict_ReturnsConflict() throws Exception {
        when(bundleService.updateBundle(eq(1L), any(BundleEntity.class)))
                .thenThrow(new IllegalStateException("Cannot modify price"));

        mockMvc.perform(put("/api/v1/bundle/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBundle)))
                .andExpect(status().isConflict());
    }

    // ==================== GET /api/v1/bundle/search (public) ====================

    @Test
    void searchBundles_ReturnsOk() throws Exception {
        when(bundleService.searchAvailableBundles(
                eq("Punta Cana"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleBundle));

        mockMvc.perform(get("/api/v1/bundle/search")
                        .param("destiny", "Punta Cana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destinationBundle").value("Punta Cana"));
    }

    @Test
    void searchBundles_NoParams_ReturnsOk() throws Exception {
        when(bundleService.searchAvailableBundles(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleBundle));

        mockMvc.perform(get("/api/v1/bundle/search"))
                .andExpect(status().isOk());
    }
}
