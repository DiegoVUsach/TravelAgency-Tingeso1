package com.example.demo.controller;


import com.example.demo.service.BundleService;
import com.example.demo.entity.BundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/bundle")
@CrossOrigin("*")
public class BundleController {
    @Autowired
    BundleService bundleService;

    @GetMapping
    public ResponseEntity<List<BundleEntity>> getAllBundles() {
        List<BundleEntity> bundles = bundleService.findByPriceBundleGreaterThan(0); // Assuming you want to get all bundles with price greater than 0
        return ResponseEntity.ok(bundles);
    }

    @GetMapping("/sort/greaterThan/{price}") //change maybe, probably
    public List<BundleEntity> findByPriceBundleGreaterThan(@PathVariable("price") int price) {
        return bundleService.findByPriceBundleGreaterThan(price);

    }

    @PostMapping
    public BundleEntity saveBundle(@RequestBody BundleEntity bundleEntity) {
        return bundleService.saveBundle(bundleEntity);
    }




}
