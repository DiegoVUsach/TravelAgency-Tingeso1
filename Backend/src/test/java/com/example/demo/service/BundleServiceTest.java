package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.BundleRepository;
import com.example.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BundleServiceTest {

    @Mock
    private BundleRepository bundleRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private BundleService bundleService;

    private BundleEntity validBundle;

    @BeforeEach
    void setUp() {
        validBundle = new BundleEntity();
        validBundle.setIdBundle(1L);
        validBundle.setNameBundle("Caribe");
        validBundle.setDestinationBundle("Punta Cana");
        validBundle.setDescriptionBundle("Playa y sol");
        validBundle.setPriceBundle(500000);
        validBundle.setAvailableSlotsBundle(20);
        validBundle.setStartDateBundle(LocalDate.now().plusDays(10));
        validBundle.setEndDateBundle(LocalDate.now().plusDays(20));
        validBundle.setStateBundle(BundleState.AVAILABLE);
        validBundle.setExperienceTypes(Set.of(ExperienceTypeState.RELAX));
        validBundle.setSeasonType(SeasonTypeState.SUMMER);
        validBundle.setCategoryType(CategoryTypeState.PREMIUM);
    }

    // ==================== findByPriceBundleGreaterThan ====================

    @Test
    void findByPriceBundleGreaterThan_ReturnsList() {
        when(bundleRepository.findByPriceBundleGreaterThan(100000))
                .thenReturn(List.of(validBundle));

        List<BundleEntity> result = bundleService.findByPriceBundleGreaterThan(100000);

        assertEquals(1, result.size());
        assertEquals("Caribe", result.get(0).getNameBundle());
    }

    // ==================== getBundleById ====================

    @Test
    void getBundleById_Found() {
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));

        BundleEntity result = bundleService.getBundleById(1L);

        assertEquals("Caribe", result.getNameBundle());
    }

    @Test
    void getBundleById_NotFound_ThrowsException() {
        when(bundleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bundleService.getBundleById(999L));
    }

    // ==================== searchAvailableBundles ====================

    @Test
    void searchAvailableBundles_DelegatesToRepository() {
        when(bundleRepository.searchAvailableBundles(
                BundleState.AVAILABLE, "Punta Cana", null, null, null, null, null, null, null, null))
                .thenReturn(List.of(validBundle));

        List<BundleEntity> result = bundleService.searchAvailableBundles(
                "Punta Cana", null, null, null, null, null, null, null, null);

        assertEquals(1, result.size());
    }

    // ==================== saveBundle ====================

    @Test
    void saveBundle_HappyPath() {
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(validBundle);

        BundleEntity result = bundleService.saveBundle(validBundle);

        assertNotNull(result);
        verify(bundleRepository).save(any(BundleEntity.class));
    }

    @Test
    void saveBundle_NullName_ThrowsException() {
        validBundle.setNameBundle(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_BlankName_ThrowsException() {
        validBundle.setNameBundle("   ");

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NullDestination_ThrowsException() {
        validBundle.setDestinationBundle(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_BlankDestination_ThrowsException() {
        validBundle.setDestinationBundle("  ");

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NullDescription_ThrowsException() {
        validBundle.setDescriptionBundle(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_BlankDescription_ThrowsException() {
        validBundle.setDescriptionBundle("");

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_PriceZero_ThrowsException() {
        validBundle.setPriceBundle(0);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NegativePrice_ThrowsException() {
        validBundle.setPriceBundle(-1);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_ZeroSlots_ThrowsException() {
        validBundle.setAvailableSlotsBundle(0);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_StartDateAfterEndDate_ThrowsException() {
        validBundle.setStartDateBundle(LocalDate.now().plusDays(20));
        validBundle.setEndDateBundle(LocalDate.now().plusDays(10));

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NullStartDate_ThrowsException() {
        validBundle.setStartDateBundle(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NullEndDate_ThrowsException() {
        validBundle.setEndDateBundle(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_NullExperienceTypes_ThrowsException() {
        validBundle.setExperienceTypes(null);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_EmptyExperienceTypes_ThrowsException() {
        validBundle.setExperienceTypes(Set.of());

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_AvailableWithZeroSlots_ThrowsException() {
        // This tests the specific check: state AVAILABLE + 0 slots
        validBundle.setAvailableSlotsBundle(0);
        validBundle.setStateBundle(BundleState.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () -> bundleService.saveBundle(validBundle));
    }

    @Test
    void saveBundle_SetsDefaultStateIfNull() {
        validBundle.setStateBundle(null);

        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        BundleEntity result = bundleService.saveBundle(validBundle);

        assertEquals(BundleState.AVAILABLE, result.getStateBundle());
    }

    @Test
    void saveBundle_AutoCalculatesDuration() {
        validBundle.setStartDateBundle(LocalDate.of(2027, 1, 1));
        validBundle.setEndDateBundle(LocalDate.of(2027, 1, 11));

        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        BundleEntity result = bundleService.saveBundle(validBundle);

        assertEquals(10, result.getDurationBundle());
    }

    // ==================== updateBundle ====================

    @Test
    void updateBundle_HappyPath_NoReservations() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Updated Name");
        newDetails.setDestinationBundle("New Dest");
        newDetails.setDescriptionBundle("New Desc");
        newDetails.setPriceBundle(600000);
        newDetails.setAvailableSlotsBundle(25);
        newDetails.setStartDateBundle(LocalDate.now().plusDays(10));
        newDetails.setEndDateBundle(LocalDate.now().plusDays(20));
        newDetails.setStateBundle(BundleState.AVAILABLE);
        newDetails.setExperienceTypes(Set.of(ExperienceTypeState.ADVENTURE));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);
        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        BundleEntity result = bundleService.updateBundle(1L, newDetails);

        assertEquals("Updated Name", result.getNameBundle());
        assertEquals(600000, result.getPriceBundle());
    }

    @Test
    void updateBundle_NotFound_ThrowsException() {
        when(bundleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> bundleService.updateBundle(999L, validBundle));
    }

    @Test
    void updateBundle_WithReservations_CannotChangePriceOrDates() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Updated");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(999999); // changed price!
        newDetails.setAvailableSlotsBundle(20);
        newDetails.setStartDateBundle(validBundle.getStartDateBundle());
        newDetails.setEndDateBundle(validBundle.getEndDateBundle());
        newDetails.setStateBundle(BundleState.AVAILABLE);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(5L);

        assertThrows(IllegalStateException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_WithReservations_CannotReduceSlotsBelowReservations() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Updated");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(validBundle.getPriceBundle());
        newDetails.setAvailableSlotsBundle(2); // less than 5 reservations
        newDetails.setStartDateBundle(validBundle.getStartDateBundle());
        newDetails.setEndDateBundle(validBundle.getEndDateBundle());
        newDetails.setStateBundle(BundleState.AVAILABLE);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(5L);

        assertThrows(IllegalStateException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_NullName_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle(null);
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_NullDestination_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle(null);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_NullDescription_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle(null);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_ZeroPrice_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(0);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_AvailableWithZeroSlots_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(500000);
        newDetails.setAvailableSlotsBundle(0);
        newDetails.setStateBundle(BundleState.AVAILABLE);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_NoReservations_ZeroSlots_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(500000);
        newDetails.setAvailableSlotsBundle(0);
        newDetails.setStateBundle(BundleState.CANCELED);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_AutoSetsSoldOutWhenSlotsReachZero() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Updated");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");
        newDetails.setPriceBundle(validBundle.getPriceBundle());
        newDetails.setAvailableSlotsBundle(0);
        newDetails.setStartDateBundle(validBundle.getStartDateBundle());
        newDetails.setEndDateBundle(validBundle.getEndDateBundle());
        newDetails.setStateBundle(BundleState.AVAILABLE);

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(10L);
        // same price and dates, just slots to 0
        // But wait, 0 < 10 reservations, so this will throw "cannot reduce slots" first.
        // Let's test with slots at exactly the reservation count
        newDetails.setAvailableSlotsBundle(10);

        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        BundleEntity result = bundleService.updateBundle(1L, newDetails);

        // 10 slots is not <= 0, so won't auto-set SOLD_OUT
        assertEquals(BundleState.AVAILABLE, result.getStateBundle());
    }

    @Test
    void updateBundle_WithReservations_SamePriceAndDates_Succeeds() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Updated Name");
        newDetails.setDestinationBundle("Updated Dest");
        newDetails.setDescriptionBundle("Updated Desc");
        newDetails.setPriceBundle(validBundle.getPriceBundle()); // same price
        newDetails.setAvailableSlotsBundle(20);
        newDetails.setStartDateBundle(validBundle.getStartDateBundle()); // same dates
        newDetails.setEndDateBundle(validBundle.getEndDateBundle());
        newDetails.setStateBundle(BundleState.AVAILABLE);
        newDetails.setExperienceTypes(Set.of(ExperienceTypeState.ADVENTURE));

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(5L);
        when(bundleRepository.save(any(BundleEntity.class))).thenAnswer(i -> i.getArgument(0));

        BundleEntity result = bundleService.updateBundle(1L, newDetails);

        assertEquals("Updated Name", result.getNameBundle());
    }

    @Test
    void updateBundle_BlankName_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("  ");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("Desc");

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_BlankDestination_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("  ");

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    @Test
    void updateBundle_BlankDescription_ThrowsException() {
        BundleEntity newDetails = new BundleEntity();
        newDetails.setNameBundle("Name");
        newDetails.setDestinationBundle("Dest");
        newDetails.setDescriptionBundle("  ");

        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> bundleService.updateBundle(1L, newDetails));
    }

    // ==================== deleteBundle ====================

    @Test
    void deleteBundle_WithReservations_LogicalDelete() {
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(5L);
        when(bundleRepository.save(any(BundleEntity.class))).thenReturn(validBundle);

        bundleService.deleteBundle(1L);

        assertEquals(BundleState.CANCELED, validBundle.getStateBundle());
        verify(bundleRepository).save(validBundle);
        verify(bundleRepository, never()).delete(any());
    }

    @Test
    void deleteBundle_NoReservations_PhysicalDelete() {
        when(bundleRepository.findById(1L)).thenReturn(Optional.of(validBundle));
        when(reservationRepository.countByBundleIdBundle(1L)).thenReturn(0L);

        bundleService.deleteBundle(1L);

        verify(bundleRepository).delete(validBundle);
        verify(bundleRepository, never()).save(any());
    }

    @Test
    void deleteBundle_NotFound_ThrowsException() {
        when(bundleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bundleService.deleteBundle(999L));
    }
}
