package com.example.demo.controller;

import com.example.demo.entity.UserEntity;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Sync user from Keycloak token into local DB
    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserEntity> syncUser(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        String keycloakId = jwt.getSubject();
        UserEntity syncedUser = userService.syncUser(email, keycloakId);
        return ResponseEntity.ok(syncedUser);
    }

    // Get my profile
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserEntity> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        UserEntity user = userService.getUserProfile(email);
        return ResponseEntity.ok(user);
    }

    // Update my profile
    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserEntity> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt, 
            @RequestBody UserEntity updatedData) {
        String email = jwt.getClaimAsString("email");
        UserEntity user = userService.updateUserProfile(email, updatedData);
        return ResponseEntity.ok(user);
    }

    // Delete my account
    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> deleteMyAccount(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        userService.deleteUserAccount(email);
        return ResponseEntity.ok("User account processed for deletion.");
    }
}
