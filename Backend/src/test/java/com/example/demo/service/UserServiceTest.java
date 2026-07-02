package com.example.demo.service;

import com.example.demo.dto.UserProfileDTO;
import com.example.demo.dto.UserUpdateDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new UserEntity();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@example.com");
        sampleUser.setFullName("Test User");
        sampleUser.setPhone("+56912345678");
        sampleUser.setIdentityDocument("12345678-9");
        sampleUser.setNationality("Chilean");
        sampleUser.setActive(true);
        sampleUser.setRole("CLIENT");
        sampleUser.setKeycloakId("kc-123");
    }

    // ==================== toProfileDTO ====================

    @Test
    void toProfileDTO_MapsCorrectly() {
        UserProfileDTO dto = userService.toProfileDTO(sampleUser);

        assertEquals(1L, dto.getId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test User", dto.getFullName());
        assertEquals("+56912345678", dto.getPhone());
        assertEquals("12345678-9", dto.getIdentityDocument());
        assertEquals("Chilean", dto.getNationality());
        assertTrue(dto.isActive());
        assertEquals("CLIENT", dto.getRole());
    }

    // ==================== syncUser ====================

    @Test
    void syncUser_ExistingByKeycloakId_ReturnsProfile() {
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(sampleUser));

        UserProfileDTO result = userService.syncUser("test@example.com", "kc-123", "Test User");

        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUser_ExistingByKeycloakId_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalStateException.class,
                () -> userService.syncUser("test@example.com", "kc-123", "Test User"));
    }

    @Test
    void syncUser_ExistingByKeycloakId_UpdatesFullNameIfBlank() {
        sampleUser.setFullName(null);
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        userService.syncUser("test@example.com", "kc-123", "New Name");

        assertEquals("New Name", sampleUser.getFullName());
        verify(userRepository).save(sampleUser);
    }

    @Test
    void syncUser_ExistingByKeycloakId_DoesNotOverwriteExistingFullName() {
        sampleUser.setFullName("Original Name");
        when(userRepository.findByKeycloakId("kc-123")).thenReturn(Optional.of(sampleUser));

        userService.syncUser("test@example.com", "kc-123", "New Name");

        assertEquals("Original Name", sampleUser.getFullName());
        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUser_ExistingByEmail_LinksKeycloakId() {
        sampleUser.setKeycloakId(null);
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        UserProfileDTO result = userService.syncUser("test@example.com", "kc-new", null);

        assertEquals("test@example.com", result.getEmail());
        assertEquals("kc-new", sampleUser.getKeycloakId());
        verify(userRepository).save(sampleUser);
    }

    @Test
    void syncUser_ExistingByEmail_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(IllegalStateException.class,
                () -> userService.syncUser("test@example.com", "kc-new", null));
    }

    @Test
    void syncUser_ExistingByEmail_KeycloakIdAlreadySet_DoesNotOverwrite() {
        sampleUser.setKeycloakId("kc-old");
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserProfileDTO result = userService.syncUser("test@example.com", "kc-new", null);

        assertEquals("kc-old", sampleUser.getKeycloakId());
        verify(userRepository, never()).save(any());
    }

    @Test
    void syncUser_ExistingByEmail_LinksKeycloakIdAndUpdatesName() {
        sampleUser.setKeycloakId(null);
        sampleUser.setFullName(null);
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        userService.syncUser("test@example.com", "kc-new", "New Name");

        assertEquals("kc-new", sampleUser.getKeycloakId());
        assertEquals("New Name", sampleUser.getFullName());
    }

    @Test
    void syncUser_NewUser_CreatesUser() {
        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> {
            UserEntity u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserProfileDTO result = userService.syncUser("new@example.com", "kc-new", "New User");

        assertEquals("new@example.com", result.getEmail());
        assertTrue(result.isActive());
        assertEquals("CLIENT", result.getRole());
        verify(userRepository).save(any(UserEntity.class));
    }

    // ==================== getUserProfile ====================

    @Test
    void getUserProfile_ActiveUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserProfileDTO result = userService.getUserProfile("test@example.com");

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserProfile_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class,
                () -> userService.getUserProfile("test@example.com"));
    }

    @Test
    void getUserProfile_NotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.getUserProfile("unknown@example.com"));
    }

    // ==================== getUserEntity ====================

    @Test
    void getUserEntity_ActiveUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        UserEntity result = userService.getUserEntity("test@example.com");

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserEntity_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class,
                () -> userService.getUserEntity("test@example.com"));
    }

    // ==================== updateUserProfile ====================

    @Test
    void updateUserProfile_UpdatesAllFields() {
        UserUpdateDTO update = new UserUpdateDTO("Updated Name", "+56999999999", "99999999-K", "Peruvian");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        UserProfileDTO result = userService.updateUserProfile("test@example.com", update);

        assertEquals("Updated Name", sampleUser.getFullName());
        assertEquals("+56999999999", sampleUser.getPhone());
        assertEquals("99999999-K", sampleUser.getIdentityDocument());
        assertEquals("Peruvian", sampleUser.getNationality());
    }

    @Test
    void updateUserProfile_PartialUpdate_OnlyPhone() {
        UserUpdateDTO update = new UserUpdateDTO(null, "+56900000000", null, null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        userService.updateUserProfile("test@example.com", update);

        assertEquals("Test User", sampleUser.getFullName()); // unchanged
        assertEquals("+56900000000", sampleUser.getPhone()); // updated
    }

    @Test
    void updateUserProfile_InactiveUser_ThrowsException() {
        sampleUser.setActive(false);
        UserUpdateDTO update = new UserUpdateDTO("Name", null, null, null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(RuntimeException.class,
                () -> userService.updateUserProfile("test@example.com", update));
    }

    @Test
    void updateUserProfile_UserNotFound_ThrowsException() {
        UserUpdateDTO update = new UserUpdateDTO("Name", null, null, null);
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.updateUserProfile("unknown@example.com", update));
    }

    // ==================== deleteUserAccount ====================

    @Test
    void deleteUserAccount_WithReservations_SoftDeletes() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(reservationRepository.existsByUser_Email("test@example.com")).thenReturn(true);
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        userService.deleteUserAccount("test@example.com");

        assertFalse(sampleUser.isActive());
        verify(userRepository).save(sampleUser);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUserAccount_WithoutReservations_HardDeletes() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(reservationRepository.existsByUser_Email("test@example.com")).thenReturn(false);

        userService.deleteUserAccount("test@example.com");

        verify(userRepository).delete(sampleUser);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserAccount_NotFound_ThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.deleteUserAccount("unknown@example.com"));
    }

    // ==================== Admin operations ====================

    @Test
    void getAllUsers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserProfileDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserProfileDTO result = userService.getUserById(1L);

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
    }

    @Test
    void toggleUserActive_TogglesFromActiveToInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        UserProfileDTO result = userService.toggleUserActive(1L);

        assertFalse(sampleUser.isActive());
    }

    @Test
    void toggleUserActive_TogglesFromInactiveToActive() {
        sampleUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

        UserProfileDTO result = userService.toggleUserActive(1L);

        assertTrue(sampleUser.isActive());
    }

    @Test
    void toggleUserActive_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.toggleUserActive(999L));
    }
}
