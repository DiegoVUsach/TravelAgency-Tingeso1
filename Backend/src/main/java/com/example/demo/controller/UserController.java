package com.example.demo.controller;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ========== Client Endpoints ==========

    // Sync user from Keycloak token into local DB
    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> syncUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String keycloakId = jwt.getSubject();
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");
        String fullName = "";
        if (givenName != null) fullName = givenName;
        if (familyName != null) fullName = fullName.isBlank() ? familyName : fullName + " " + familyName;

        UserProfileDTO syncedUser = userService.syncUser(email, keycloakId, fullName.isBlank() ? null : fullName);
        return ResponseEntity.ok(syncedUser);
    }

    // Get my profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        UserProfileDTO user = userService.getUserProfile(email);
        return ResponseEntity.ok(user);
    }

    // Update my profile
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserUpdateDTO updatedData) {
        String email = jwt.getClaimAsString("email");
        UserProfileDTO user = userService.updateUserProfile(email, updatedData);
        return ResponseEntity.ok(user);
    }

    // Delete my account (soft-delete if has reservations)
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteMyAccount(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        userService.deleteUserAccount(email);
        return ResponseEntity.ok("User account processed for deletion.");
    }

    // ========== Admin Endpoints ==========

    // List all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        List<UserProfileDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long id) {
        UserProfileDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // Toggle user active/inactive
    @PutMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileDTO> toggleUserActive(@PathVariable Long id) {
        UserProfileDTO user = userService.toggleUserActive(id);
        return ResponseEntity.ok(user);
    }
}
