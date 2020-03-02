package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityErrorDetail;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class MicroserviceServiceImpl implements MicroserviceService {

    private MicroserviceRepository microserviceRepository;

    @Autowired
    public MicroserviceServiceImpl(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    public Microservice getMicroserviceById(Long id) {
        return microserviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Microservice.class, "id", id.getClass(), id, "Microservice not found")));
    }

    @Override
    public Microservice getMicroserviceByName(String microserviceName) {
        return microserviceRepository.findByName(microserviceName)
                .orElseThrow(() -> new EntityNotFoundException(new EntityErrorDetail(Microservice.class, "microserviceName", microserviceName.getClass(), microserviceName, "Microservice not found")));
    }

    @Override
    public Page<Microservice> getMicroservices(Predicate predicate, Pageable pageable) {
        return microserviceRepository.findAll(predicate, pageable);
    }

    @Override
    public Microservice createMicroservice(Microservice microserviceToCreate) {
        return microserviceRepository.save(microserviceToCreate);
    }

    @Override
    public boolean existsByName(String microserviceName) {
        return microserviceRepository.existsByName(microserviceName);
    }
}
