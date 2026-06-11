package com.example.demo.service;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    // ---------- DTO conversion ----------

    public UserProfileDTO toProfileDTO(UserEntity user) {
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getIdentityDocument(),
                user.getNationality(),
                user.isActive(),
                user.getRole()
        );
    }

    // ---------- Sync from Keycloak ----------

    @Transactional
    public UserProfileDTO syncUser(String email, String keycloakId, String fullName) {
        // Try to find by keycloakId first, then by email
        Optional<UserEntity> existingUser = userRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            if (!user.isActive()) {
                throw new IllegalStateException("User account is inactive. Contact an administrator.");
            }
            // Update fullName from Keycloak if it was set there
            if (fullName != null && !fullName.isBlank() && (user.getFullName() == null || user.getFullName().isBlank())) {
                user.setFullName(fullName);
                userRepository.save(user);
            }
            return toProfileDTO(user);
        }

        // Check by email as fallback
        Optional<UserEntity> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            UserEntity user = byEmail.get();
            if (!user.isActive()) {
                throw new IllegalStateException("User account is inactive. Contact an administrator.");
            }
            // Link keycloakId if not already set
            if (user.getKeycloakId() == null) {
                user.setKeycloakId(keycloakId);
                if (fullName != null && !fullName.isBlank() && (user.getFullName() == null || user.getFullName().isBlank())) {
                    user.setFullName(fullName);
                }
                userRepository.save(user);
            }
            return toProfileDTO(user);
        }

        // Create new user
        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setKeycloakId(keycloakId);
        newUser.setFullName(fullName);
        newUser.setActive(true);
        newUser.setRole("CLIENT");
        return toProfileDTO(userRepository.save(newUser));
    }

    // ---------- Profile operations ----------

    public UserProfileDTO getUserProfile(String email) {
        return toProfileDTO(getUserEntity(email));
    }

    public UserEntity getUserEntity(String email) {
        return userRepository.findByEmail(email)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new RuntimeException("User not found or inactive: " + email));
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String email, UserUpdateDTO updatedData) {
        UserEntity user = userRepository.findByEmail(email)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new RuntimeException("User not found or inactive: " + email));

        if (updatedData.getFullName() != null) {
            user.setFullName(updatedData.getFullName());
        }
        if (updatedData.getPhone() != null) {
            user.setPhone(updatedData.getPhone());
        }
        if (updatedData.getIdentityDocument() != null) {
            user.setIdentityDocument(updatedData.getIdentityDocument());
        }
        if (updatedData.getNationality() != null) {
            user.setNationality(updatedData.getNationality());
        }

        return toProfileDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUserAccount(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        boolean hasReservations = reservationRepository.existsByUser_Email(email);

        if (hasReservations) {
            // Logical deletion — cannot remove users with reservation history
            user.setActive(false);
            userRepository.save(user);
        } else {
            // Physical deletion, only if it doesn't have any reservations
            userRepository.delete(user);
        }
    }

    // ---------- Admin operations ----------

    public List<UserProfileDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toProfileDTO)
                .collect(Collectors.toList());
    }

    public UserProfileDTO getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toProfileDTO(user);
    }

    @Transactional
    public UserProfileDTO toggleUserActive(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        user.setActive(!user.isActive());
        return toProfileDTO(userRepository.save(user));
    }
}
