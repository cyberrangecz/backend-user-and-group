package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.domain.Microservice;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MicroserviceService {

    private final MicroserviceRepository microserviceRepository;

    @Autowired
    public MicroserviceService(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    public Microservice getMicroserviceById(Long id) {
        return microserviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Microservice.class, "id", id.getClass(), id)));
    }

    public Microservice getMicroserviceByName(String microserviceName) {
        return microserviceRepository.findByName(microserviceName)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Microservice.class, "microserviceName", microserviceName.getClass(), microserviceName)));
    }

    public Page<Microservice> getMicroservices(Predicate predicate, Pageable pageable) {
        return microserviceRepository.findAll(predicate, pageable);
    }

    public Microservice createMicroservice(Microservice microserviceToCreate) {
        return microserviceRepository.save(microserviceToCreate);
    }

    public boolean existsByName(String microserviceName) {
        return microserviceRepository.existsByName(microserviceName);
    }
}
