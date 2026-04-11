package Service;

import Controller.bundleController;
import entity.bundleEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.bundleRepository;

import java.util.List;

@Service

public class bundleService {

    private bundleRepository bundleRepository;

    @Autowired
    public bundleService(bundleRepository bundleRepository) {
        this.bundleRepository = bundleRepository;
    }

    public List<bundleEntity> findByPriceBundleGreaterThan(int price) {
        return bundleRepository.findByPriceBundleGreaterThan(price);
    }

    public bundleEntity saveBundle(bundleEntity bundleEntity) {
        return bundleRepository.save(bundleEntity);
    }
}
