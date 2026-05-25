package com.example.demo.service;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public UserEntity syncUser(String email, String keycloakId) {
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        UserEntity newUser = new UserEntity();
        newUser.setEmail(email);
        newUser.setKeycloakId(keycloakId);
        newUser.setActive(true);
        return userRepository.save(newUser);
    }

    public UserEntity getUserProfile(String email) {
        return userRepository.findByEmail(email)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new RuntimeException("User not found or inactive: " + email));
    }

    @Transactional
    public UserEntity updateUserProfile(String email, UserEntity updatedData) {
        UserEntity user = getUserProfile(email);
        
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
        
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserAccount(String email) {
        UserEntity user = getUserProfile(email);
        boolean hasReservations = reservationRepository.existsByUser_Email(email);

        if (hasReservations) {
            // Logical deletion
            user.setActive(false);
            userRepository.save(user);
        } else {
            // Physical deletion, only if it doesn't have any reservations
            userRepository.delete(user);
        }
    }
}
