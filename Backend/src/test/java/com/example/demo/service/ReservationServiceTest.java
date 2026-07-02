package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.DiscountConfigRepository;
import com.example.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private BundleRepository bundleRepository;
    @Mock
    private DiscountConfigRepository discountConfigRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private ReservationService reservationService;

    private BundleEntity sampleBundle;
    private UserEntity sampleUser;

    @BeforeEach
    void setUp() {
        sampleBundle = new BundleEntity();
        sampleBundle.setIdBundle(1L);
        sampleBundle.setNameBundle("Caribe");
        sampleBundle.setDestinationBundle("Punta Cana");
        sampleBundle.setPriceBundle(100000);
        sampleBundle.setAvailableSlotsBundle(20);
        sampleBundle.setStateBundle(BundleState.AVAILABLE);
        sampleBundle.setStartDateBundle(LocalDate.now().plusDays(10));
        sampleBundle.setEndDateBundle(LocalDate.now().plusDays(20));
        sampleBundle.setExperienceTypes(Set.of(ExperienceTypeState.RELAX));

        sampleUser = new UserEntity();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@example.com");
        sampleUser.setFullName("Test User");
        sampleUser.setActive(true);
        sampleUser.setRole("CLIENT");
    }

    // ==================== processCartReservations ====================

    @Test
    void processCartReservations_HappyPath_SingleItem() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(100L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        assertNotNull(response);
        assertEquals("Reservas creadas exitosamente.", response.getMessage());
        assertEquals(200000, response.getSubtotal()); // 100000 * 2
        assertEquals(200000, response.getFinalTotal()); // no discounts
        assertEquals(0, response.getTotalDiscount());
        assertTrue(response.getGeneratedReservationIds().contains(100L));
        verify(bundleRepository).save(any(BundleEntity.class));
        verify(reservationRepository).save(any(ReservationEntity.class));
    }

    @Test
    void processCartReservations_EmptyCart_ThrowsException() {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_NullItems_ThrowsException() {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(null);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_ZeroPassengers_ThrowsException() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(0);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_BundleNotFound_ThrowsException() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(999L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(999L)).thenReturn(Optional.empty());
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(RuntimeException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_CanceledBundle_ThrowsException() {
        sampleBundle.setStateBundle(BundleState.CANCELED);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_ExpiredBundle_ThrowsException() {
        sampleBundle.setStateBundle(BundleState.EXPIRED);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_SoldOutBundle_ThrowsException() {
        sampleBundle.setStateBundle(BundleState.SOLD_OUT);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_EndedBundle_ThrowsException() {
        sampleBundle.setEndDateBundle(LocalDate.now().minusDays(1));

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    @Test
    void processCartReservations_InsufficientSlots_ThrowsException() {
        sampleBundle.setAvailableSlotsBundle(1);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(5);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.processCartReservations(request, "test@example.com"));
    }

    // ==================== Discounts ====================

    @Test
    void processCartReservations_MultiplePackagesDiscount() {
        CartItemDTO item1 = new CartItemDTO();
        item1.setBundleId(1L);
        item1.setPassengers(1);

        BundleEntity bundle2 = new BundleEntity();
        bundle2.setIdBundle(2L);
        bundle2.setNameBundle("Patagonia");
        bundle2.setDestinationBundle("Torres del Paine");
        bundle2.setPriceBundle(100000);
        bundle2.setAvailableSlotsBundle(10);
        bundle2.setStateBundle(BundleState.AVAILABLE);
        bundle2.setStartDateBundle(LocalDate.now().plusDays(10));
        bundle2.setEndDateBundle(LocalDate.now().plusDays(20));

        CartItemDTO item2 = new CartItemDTO();
        item2.setBundleId(2L);
        item2.setPassengers(1);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item1, item2)); // 2 packages >= threshold 2

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(bundleRepository.findById(2L)).thenReturn(Optional.of(bundle2));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(100L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        // 5% multiple packages discount on each item
        assertEquals(200000, response.getSubtotal());
        assertTrue(response.getTotalDiscount() > 0);
        assertTrue(response.getAppliedDiscounts().stream()
                .anyMatch(d -> d.getType().equals("MULTIPLE_PACKAGES")));
    }

    @Test
    void processCartReservations_FrequentClientDiscount() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        // 3 or more confirmed reservations = frequent client
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(5L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(101L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        assertTrue(response.getTotalDiscount() > 0);
        assertTrue(response.getAppliedDiscounts().stream()
                .anyMatch(d -> d.getType().equals("FREQUENT_CLIENT")));
    }

    @Test
    void processCartReservations_VolumeDiscount() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(5); // >= 4 default threshold

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(102L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        assertTrue(response.getTotalDiscount() > 0);
        assertTrue(response.getAppliedDiscounts().stream()
                .anyMatch(d -> d.getType().equals("VOLUME_DISCOUNT")));
    }

    @Test
    void processCartReservations_PromoDiscount() {
        // Set active promo on the bundle
        sampleBundle.setPromoStartDate(LocalDate.now().minusDays(5));
        sampleBundle.setPromoEndDate(LocalDate.now().plusDays(5));
        sampleBundle.setPromoDiscountPercent(0.10);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(1);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(103L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        assertTrue(response.getTotalDiscount() > 0);
        assertTrue(response.getAppliedDiscounts().stream()
                .anyMatch(d -> d.getType().equals("PROMOTION")));
    }

    @Test
    void processCartReservations_InactivePromo_NoDiscount() {
        // Promo already ended
        sampleBundle.setPromoStartDate(LocalDate.now().minusDays(30));
        sampleBundle.setPromoEndDate(LocalDate.now().minusDays(10));
        sampleBundle.setPromoDiscountPercent(0.10);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(1);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(104L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        assertEquals(0, response.getTotalDiscount());
        assertTrue(response.getAppliedDiscounts().stream()
                .noneMatch(d -> d.getType().equals("PROMOTION")));
    }

    @Test
    void processCartReservations_DiscountCappedAt20Percent() {
        // Activate ALL discounts: frequent client, volume, promo, multiple packages
        sampleBundle.setPromoStartDate(LocalDate.now().minusDays(5));
        sampleBundle.setPromoEndDate(LocalDate.now().plusDays(5));
        sampleBundle.setPromoDiscountPercent(0.15);

        BundleEntity bundle2 = new BundleEntity();
        bundle2.setIdBundle(2L);
        bundle2.setNameBundle("Patagonia");
        bundle2.setDestinationBundle("Torres del Paine");
        bundle2.setPriceBundle(100000);
        bundle2.setAvailableSlotsBundle(10);
        bundle2.setStateBundle(BundleState.AVAILABLE);
        bundle2.setStartDateBundle(LocalDate.now().plusDays(10));
        bundle2.setEndDateBundle(LocalDate.now().plusDays(20));

        CartItemDTO item1 = new CartItemDTO();
        item1.setBundleId(1L);
        item1.setPassengers(5); // volume discount

        CartItemDTO item2 = new CartItemDTO();
        item2.setBundleId(2L);
        item2.setPassengers(1);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item1, item2)); // multiple packages

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(bundleRepository.findById(2L)).thenReturn(Optional.of(bundle2));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(5L); // frequent
        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(105L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        // For item1: globalDiscount(0.05+0.05) + volume(0.10) + promo(0.15) = 0.35, capped at 0.20
        // base for item1 = 100000*5 = 500000, final = 500000 * 0.80 = 400000
        // For item2: globalDiscount(0.10), no volume no promo = 0.10
        // base for item2 = 100000*1 = 100000, final = 100000 * 0.90 = 90000
        int expectedSubtotal = 600000;
        assertEquals(expectedSubtotal, response.getSubtotal());
        // Final should be 400000 + 90000 = 490000
        assertEquals(490000, response.getFinalTotal());
        assertTrue(response.getFinalTotal() >= 0);
    }

    @Test
    void processCartReservations_WithCustomConfigValues() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        DiscountConfigEntity volumeConfig = new DiscountConfigEntity();
        volumeConfig.setConfigKey("VOLUME_DISCOUNT");
        volumeConfig.setConfigValue(0.15);
        volumeConfig.setThreshold(2);

        when(userService.getUserEntity("test@example.com")).thenReturn(sampleUser);
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey("VOLUME_DISCOUNT")).thenReturn(Optional.of(volumeConfig));
        when(discountConfigRepository.findByConfigKey("MULTIPLE_PACKAGES")).thenReturn(Optional.empty());
        when(discountConfigRepository.findByConfigKey("FREQUENT_CLIENT")).thenReturn(Optional.empty());
        when(discountConfigRepository.findByConfigKey("MAX_DISCOUNT_LIMIT")).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);

        ReservationEntity savedReservation = new ReservationEntity();
        savedReservation.setId(106L);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(savedReservation);

        ReservationResponseDTO response = reservationService.processCartReservations(request, "test@example.com");

        // Passengers(2) >= threshold(2) so volume discount of 15% applies
        assertTrue(response.getTotalDiscount() > 0);
        assertTrue(response.getAppliedDiscounts().stream()
                .anyMatch(d -> d.getType().equals("VOLUME_DISCOUNT")));
    }

    // ==================== calculateQuote ====================

    @Test
    void calculateQuote_HappyPath() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        ReservationResponseDTO response = reservationService.calculateQuote(request, "test@example.com");

        assertNotNull(response);
        assertEquals("Cotización calculada exitosamente.", response.getMessage());
        assertEquals(200000, response.getSubtotal());
        assertEquals(200000, response.getFinalTotal());
    }

    @Test
    void calculateQuote_EmptyCart_ThrowsException() {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_NullItems_ThrowsException() {
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(null);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_ZeroPassengers_ThrowsException() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(0);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_CanceledBundle_ThrowsException() {
        sampleBundle.setStateBundle(BundleState.CANCELED);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_WithVolumeDiscount() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(5);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        ReservationResponseDTO response = reservationService.calculateQuote(request, "test@example.com");

        assertTrue(response.getTotalDiscount() > 0);
    }

    @Test
    void calculateQuote_BundleNotFound_ThrowsException() {
        CartItemDTO item = new CartItemDTO();
        item.setBundleId(999L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(999L)).thenReturn(Optional.empty());
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(RuntimeException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_EndedBundle_ThrowsException() {
        sampleBundle.setEndDateBundle(LocalDate.now().minusDays(1));

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(2);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_InsufficientSlots_ThrowsException() {
        sampleBundle.setAvailableSlotsBundle(1);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(5);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(0L);

        assertThrows(IllegalStateException.class,
                () -> reservationService.calculateQuote(request, "test@example.com"));
    }

    @Test
    void calculateQuote_DiscountCappedAt20Percent() {
        sampleBundle.setPromoStartDate(LocalDate.now().minusDays(5));
        sampleBundle.setPromoEndDate(LocalDate.now().plusDays(5));
        sampleBundle.setPromoDiscountPercent(0.15);

        CartItemDTO item = new CartItemDTO();
        item.setBundleId(1L);
        item.setPassengers(5);

        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setItems(List.of(item));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(sampleBundle));
        when(discountConfigRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());
        when(reservationRepository.countByUser_EmailAndState("test@example.com", ReservationState.CONFIRMED)).thenReturn(5L);

        ReservationResponseDTO response = reservationService.calculateQuote(request, "test@example.com");

        // All: frequent(0.05) + volume(0.10) + promo(0.15) = 0.30, capped at 0.20
        int basePrice = 100000 * 5;
        int expectedFinal = (int)(basePrice * 0.80);
        assertEquals(basePrice, response.getSubtotal());
        assertEquals(expectedFinal, response.getFinalTotal());
    }

    // ==================== cancelExpiredReservations ====================

    @Test
    void cancelExpiredReservations_ReturnsSlots() {
        ReservationEntity expiredReservation = new ReservationEntity();
        expiredReservation.setId(1L);
        expiredReservation.setState(ReservationState.PENDING_PAYMENT);
        expiredReservation.setNumberOfPassengers(3);
        expiredReservation.setBundle(sampleBundle);

        when(reservationRepository.findByStateAndReservationDateBefore(
                eq(ReservationState.PENDING_PAYMENT), any(LocalDate.class)))
                .thenReturn(List.of(expiredReservation));
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(sampleBundle);
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(expiredReservation);

        reservationService.cancelExpiredReservations();

        assertEquals(ReservationState.CANCELED, expiredReservation.getState());
        assertEquals(23, sampleBundle.getAvailableSlotsBundle()); // 20 + 3
        verify(bundleRepository).save(sampleBundle);
        verify(reservationRepository).save(expiredReservation);
    }

    @Test
    void cancelExpiredReservations_NoExpired_DoesNothing() {
        when(reservationRepository.findByStateAndReservationDateBefore(
                eq(ReservationState.PENDING_PAYMENT), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        reservationService.cancelExpiredReservations();

        verify(bundleRepository, never()).save(any());
        verify(reservationRepository, never()).save(any());
    }

    // ==================== getUserReservations ====================

    @Test
    void getUserReservations_ReturnsList() {
        ReservationEntity r1 = new ReservationEntity();
        r1.setId(1L);
        when(reservationRepository.findByUser_EmailOrderByReservationDateDesc("test@example.com"))
                .thenReturn(List.of(r1));

        List<ReservationEntity> result = reservationService.getUserReservations("test@example.com");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ==================== getAllReservations ====================

    @Test
    void getAllReservations_ReturnsList() {
        when(reservationRepository.findAll()).thenReturn(List.of(new ReservationEntity()));

        List<ReservationEntity> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
    }

    // ==================== updateReservationState ====================

    @Test
    void updateReservationState_HappyPath() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.PENDING_PAYMENT);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(ReservationEntity.class))).thenReturn(reservation);

        ReservationEntity result = reservationService.updateReservationState(1L, ReservationState.CONFIRMED);

        assertEquals(ReservationState.CONFIRMED, result.getState());
    }

    @Test
    void updateReservationState_NotFound_ThrowsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.updateReservationState(999L, ReservationState.CONFIRMED));
    }

    @Test
    void updateReservationState_CanceledToConfirmed_ThrowsException() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.CANCELED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.updateReservationState(1L, ReservationState.CONFIRMED));
    }

    // ==================== generateReceipt ====================

    @Test
    void generateReceipt_HappyPath_Owner() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.CONFIRMED);
        reservation.setUser(sampleUser);
        reservation.setBundle(sampleBundle);
        reservation.setNumberOfPassengers(2);
        reservation.setTotalAmount(200000);
        reservation.setReservationDate(LocalDate.now());

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationReceiptDTO receipt = reservationService.generateReceipt(1L, "test@example.com", false);

        assertNotNull(receipt);
        assertEquals("test@example.com", receipt.getClientEmail());
        assertEquals("Caribe", receipt.getBundleName());
        assertEquals("Punta Cana", receipt.getDestination());
        assertEquals(2, receipt.getNumberOfPassengers());
        assertEquals(200000, receipt.getTotalPaid());
        assertEquals("PAGADO OFICIALMENTE", receipt.getStatus());
        assertTrue(receipt.getReceiptCode().startsWith("REC-"));
    }

    @Test
    void generateReceipt_HappyPath_Admin() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.CONFIRMED);
        reservation.setUser(sampleUser);
        reservation.setBundle(sampleBundle);
        reservation.setNumberOfPassengers(2);
        reservation.setTotalAmount(200000);
        reservation.setReservationDate(LocalDate.now());

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // Admin can access any receipt
        ReservationReceiptDTO receipt = reservationService.generateReceipt(1L, "admin@example.com", true);

        assertNotNull(receipt);
    }

    @Test
    void generateReceipt_NotOwner_ThrowsException() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.CONFIRMED);
        reservation.setUser(sampleUser);
        reservation.setBundle(sampleBundle);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.generateReceipt(1L, "other@example.com", false));
    }

    @Test
    void generateReceipt_NotConfirmed_ThrowsException() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setState(ReservationState.PENDING_PAYMENT);
        reservation.setUser(sampleUser);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class,
                () -> reservationService.generateReceipt(1L, "test@example.com", false));
    }

    @Test
    void generateReceipt_NotFound_ThrowsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservationService.generateReceipt(999L, "test@example.com", false));
    }

    // ==================== getSalesByPeriod ====================

    @Test
    void getSalesByPeriod_HappyPath() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        when(reservationRepository.findSalesByDateRange(start, end, ReservationState.CANCELED))
                .thenReturn(List.of(new ReservationEntity()));

        List<ReservationEntity> result = reservationService.getSalesByPeriod(start, end);

        assertEquals(1, result.size());
    }

    @Test
    void getSalesByPeriod_InvalidDates_ThrowsException() {
        LocalDate start = LocalDate.of(2026, 12, 31);
        LocalDate end = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getSalesByPeriod(start, end));
    }

    // ==================== getPackageRanking ====================

    @Test
    void getPackageRanking_HappyPath() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        when(reservationRepository.findPackageRanking(ReservationState.CANCELED, start, end))
                .thenReturn(Collections.emptyList());

        List<PackageRankingDTO> result = reservationService.getPackageRanking(start, end);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPackageRanking_InvalidDates_ThrowsException() {
        LocalDate start = LocalDate.of(2026, 12, 31);
        LocalDate end = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> reservationService.getPackageRanking(start, end));
    }
}
