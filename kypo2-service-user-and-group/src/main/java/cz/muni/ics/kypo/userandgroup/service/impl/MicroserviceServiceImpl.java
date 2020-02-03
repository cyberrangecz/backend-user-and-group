package cz.muni.ics.kypo.userandgroup.service.impl;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exceptions.ErrorCode;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

@Service
public class MicroserviceServiceImpl implements MicroserviceService {

    private MicroserviceRepository microserviceRepository;

    @Autowired
    public MicroserviceServiceImpl(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    public Microservice getMicroserviceById(Long id) {
        Assert.notNull(id, "In method getMicroserviceById(id) the input must not be null.");
        return microserviceRepository.findById(id)
                .orElseThrow(() -> new UserAndGroupServiceException("Microservice with id " + id + " not found", ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Override
    public Page<Microservice> getMicroservices(Predicate predicate, Pageable pageable) {
        return microserviceRepository.findAll(predicate, pageable);
    }

    @Override
    public boolean createMicroservice(Microservice microserviceToCreate) {
        Assert.notNull(microserviceToCreate, "In method createMicroservice(microserviceToCreate) the input must not be null.");
        Optional<Microservice> microserviceOpt = microserviceRepository.findByName(microserviceToCreate.getName());
        if (microserviceOpt.isPresent()) {
            return false;
        } else {
            microserviceRepository.save(microserviceToCreate);
            return true;
        }
    }
}
