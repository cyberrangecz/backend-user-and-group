package cz.muni.ics.kypo.userandgroup.service.impl;

import cz.muni.ics.kypo.userandgroup.exception.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
public class MicroserviceServiceImpl implements MicroserviceService {

    private static Logger LOGGER = LoggerFactory.getLogger(MicroserviceServiceImpl.class);

    private MicroserviceRepository microserviceRepository;

    @Autowired
    public MicroserviceServiceImpl(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    public Microservice get(Long id) throws UserAndGroupServiceException {
        Assert.notNull(id, "Input id must not be null");
        Optional<Microservice> optionalMicroservice = microserviceRepository.findById(id);
        Microservice microservice = optionalMicroservice.orElseThrow(() -> new UserAndGroupServiceException("Microservice with id " + id + " not found"));
        LOGGER.info(microservice + " loaded.");
        return microservice;
    }

    @Override
    public List<Microservice> getMicroservices() {
        List<Microservice> microservices = microserviceRepository.findAll();
        LOGGER.info("All microservices loaded");
        return microservices;
    }
}
