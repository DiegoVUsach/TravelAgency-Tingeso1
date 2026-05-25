package com.example.demo.controller;

import com.example.demo.entity.ExperienceTypeState;
import com.example.demo.service.BundleService;
import com.example.demo.entity.BundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bundle")
@CrossOrigin("*")
public class BundleController {

    @Autowired
    BundleService bundleService;

    @GetMapping
    public ResponseEntity<List<BundleEntity>> getAllBundles() {
        List<BundleEntity> bundles = bundleService.findByPriceBundleGreaterThan(0);
        return ResponseEntity.ok(bundles);
    }

    @GetMapping("/sort/greaterThan/{price}")
    public List<BundleEntity> findByPriceBundleGreaterThan(@PathVariable("price") int price) {
        return bundleService.findByPriceBundleGreaterThan(price);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public BundleEntity saveBundle(@RequestBody BundleEntity bundleEntity) {
        return bundleService.saveBundle(bundleEntity);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deleteBundle(@PathVariable Long id) {
        bundleService.deleteBundle(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public BundleEntity updateBundle(@PathVariable Long id, @RequestBody BundleEntity bundleEntity) {
        return bundleService.updateBundle(id, bundleEntity);
    }

    // E3 method, for bundle search
    @GetMapping("/search")
    public ResponseEntity<List<BundleEntity>> searchBundles(
            @RequestParam(required = false) String destiny,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false)ExperienceTypeState experience)
            {

        List<BundleEntity> results = bundleService.searchAvailableBundles(
                destiny, minPrice, maxPrice, duration, startDate, endDate, experience
        );

        return ResponseEntity.ok(results);
    }
}