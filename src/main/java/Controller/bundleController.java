package Controller;
//controller receives query


import Service.bundleService;
import entity.bundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/bundle")
@CrossOrigin("*")
public class bundleController {
    @Autowired
    bundleService bundleService;

    @GetMapping("/sort/greaterThan/{price}") //change maybe, probably
    public List<bundleEntity> findByPriceBundleGreaterThan(@PathVariable("price") int price) {
        return bundleService.findByPriceBundleGreaterThan(price);

    }

    @PostMapping
    public bundleEntity saveBundle(@RequestBody bundleEntity bundleEntity) {
        return bundleService.saveBundle(bundleEntity);
    }




}
