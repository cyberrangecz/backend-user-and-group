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

    private static Logger LOG = LoggerFactory.getLogger(MicroserviceServiceImpl.class);

    private MicroserviceRepository microserviceRepository;

    @Autowired
    public MicroserviceServiceImpl(MicroserviceRepository microserviceRepository) {
        this.microserviceRepository = microserviceRepository;
    }

    @Override
    public Microservice get(Long id) {
        LOG.debug("get({})", id);
        Assert.notNull(id, "Input id must not be null");
        return microserviceRepository.findById(id).orElseThrow(() -> new UserAndGroupServiceException("Microservice with id " + id + " not found"));
    }

    @Override
    public List<Microservice> getMicroservices() {
        LOG.debug("getMicroservices()");
        return microserviceRepository.findAll();
    }

    @Override
    public Microservice create(Microservice microserviceToCreate) {
        LOG.debug("create({})", microserviceToCreate);
        Assert.notNull(microserviceToCreate, "Input microservice must not be null.");
        Optional<Microservice> optionalMicroservice = microserviceRepository.findByName(microserviceToCreate.getName());
        if (optionalMicroservice.isPresent()) {
            optionalMicroservice.get().setEndpoint(microserviceToCreate.getEndpoint());
            return optionalMicroservice.get();
        } else {
            return microserviceRepository.save(microserviceToCreate);
        }
    }
}
