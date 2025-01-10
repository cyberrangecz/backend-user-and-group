package cz.cyberrange.platform.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.definition.exceptions.EntityNotFoundException;
import cz.cyberrange.platform.userandgroup.persistence.repository.MicroserviceRepository;
import cz.cyberrange.platform.userandgroup.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest(classes = { TestDataFactory.class })
class MicroserviceServiceTest {

    private MicroserviceService microserviceService;
    @MockBean
    private MicroserviceRepository microserviceRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    private Microservice uAGMicroservice, trainingMicroservice;
    private Predicate predicate;

    @BeforeEach
    void init() {
        microserviceService = new MicroserviceService(microserviceRepository);

        uAGMicroservice = testDataFactory.getCrczpUaGMicroservice();
        uAGMicroservice.setId(1L);
        trainingMicroservice = testDataFactory.getCrczpTrainingMicroservice();
        trainingMicroservice.setId(2L);
    }

    @Test
    void getMicroserviceById() {
        given(microserviceRepository.findById(uAGMicroservice.getId())).willReturn(Optional.of(uAGMicroservice));
        Microservice microservice = microserviceService.getMicroserviceById(uAGMicroservice.getId());

        assertEquals(uAGMicroservice.getId(), microservice.getId());
        assertEquals(uAGMicroservice.getEndpoint(), microservice.getEndpoint());
        assertEquals(uAGMicroservice.getName(), microservice.getName());
        then(microserviceRepository).should().findById(uAGMicroservice.getId());
    }

    @Test
    void getMicroserviceByIdNotFound() {
        Long id = 100L;
        given(microserviceRepository.findById(id)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> microserviceService.getMicroserviceById(id));
    }


    @Test
    void getMicroserviceByName() {
        given(microserviceRepository.findByName(uAGMicroservice.getName())).willReturn(Optional.of(uAGMicroservice));
        Microservice microservice = microserviceService.getMicroserviceByName(uAGMicroservice.getName());

        assertEquals(uAGMicroservice.getId(), microservice.getId());
        assertEquals(uAGMicroservice.getEndpoint(), microservice.getEndpoint());
        assertEquals(uAGMicroservice.getName(), microservice.getName());
        then(microserviceRepository).should().findByName(uAGMicroservice.getName());
    }

    @Test
    void getMicroserviceByNameNotFound() {
        String name = "unknown-microservice";
        given(microserviceRepository.findByName(name)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> microserviceService.getMicroserviceByName(name));
    }

    @Test
    void getMicroservices() {
        Pageable pageable = PageRequest.of(0, 10);
        given(microserviceRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(uAGMicroservice, trainingMicroservice)));

        List<Microservice> microservices = microserviceService.getMicroservices(predicate, pageable).getContent();
        assertEquals(2, microservices.size());
        assertTrue(microservices.containsAll(Set.of(uAGMicroservice, trainingMicroservice)));
        then(microserviceRepository).should().findAll(predicate, pageable);
    }

    @Test
    void createMicroservice() {
        microserviceService.createMicroservice(trainingMicroservice);
        then(microserviceRepository).should().save(trainingMicroservice);
    }
}
