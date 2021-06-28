package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.api.exceptions.EntityNotFoundException;
import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import cz.muni.ics.kypo.userandgroup.repository.MicroserviceRepository;
import cz.muni.ics.kypo.userandgroup.service.impl.MicroserviceServiceImpl;
import cz.muni.ics.kypo.userandgroup.service.interfaces.MicroserviceService;
import cz.muni.ics.kypo.userandgroup.util.TestDataFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestDataFactory.class)
public class MicroserviceServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private MicroserviceService microserviceService;
    @MockBean
    private MicroserviceRepository microserviceRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    private Microservice uAGMicroservice, trainingMicroservice;
    private Predicate predicate;
    @Before
    public void init() {
        microserviceService = new MicroserviceServiceImpl(microserviceRepository);

        uAGMicroservice = testDataFactory.getKypoUaGMicroservice();
        uAGMicroservice.setId(1L);
        trainingMicroservice = testDataFactory.getKypoTrainingMicroservice();
        trainingMicroservice.setId(2L);
    }

    @Test
    public void getMicroserviceById(){
        given(microserviceRepository.findById(uAGMicroservice.getId())).willReturn(Optional.of(uAGMicroservice));
        Microservice microservice = microserviceService.getMicroserviceById(uAGMicroservice.getId());

        assertEquals(uAGMicroservice.getId(), microservice.getId());
        assertEquals(uAGMicroservice.getEndpoint(), microservice.getEndpoint());
        assertEquals(uAGMicroservice.getName(), microservice.getName());
        then(microserviceRepository).should().findById(uAGMicroservice.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMicroserviceByIdNotFound(){
        Long id = 100L;
        given(microserviceRepository.findById(id)).willReturn(Optional.empty());
        microserviceService.getMicroserviceById(id);
    }


    @Test
    public void getMicroserviceByName(){
        given(microserviceRepository.findByName(uAGMicroservice.getName())).willReturn(Optional.of(uAGMicroservice));
        Microservice microservice = microserviceService.getMicroserviceByName(uAGMicroservice.getName());

        assertEquals(uAGMicroservice.getId(), microservice.getId());
        assertEquals(uAGMicroservice.getEndpoint(), microservice.getEndpoint());
        assertEquals(uAGMicroservice.getName(), microservice.getName());
        then(microserviceRepository).should().findByName(uAGMicroservice.getName());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMicroserviceByNameNotFound(){
        String name = "kypo-microservice";
        given(microserviceRepository.findByName(name)).willReturn(Optional.empty());
        microserviceService.getMicroserviceByName(name);
    }

    public void existsByName(){
        String name = "kypo-microservice";
        given(microserviceRepository.existsByName(name)).willReturn(true);
        boolean exists = microserviceService.existsByName(name);
        assertTrue(exists);
    }

    public void doesNotExistsByName(){
        String name = "kypo-microservice";
        given(microserviceRepository.existsByName(name)).willReturn(false);
        boolean exists = microserviceService.existsByName(name);
        assertFalse(exists);
    }


    @Test
    public void getMicroservices(){
        Pageable pageable = PageRequest.of(0, 10);
        given(microserviceRepository.findAll(predicate, pageable))
                .willReturn(new PageImpl<>(Arrays.asList(uAGMicroservice, trainingMicroservice)));

        List<Microservice> microservices = microserviceService.getMicroservices(predicate, pageable).getContent();
        assertEquals(2, microservices.size());
        assertTrue(microservices.containsAll(Set.of(uAGMicroservice, trainingMicroservice)));
        then(microserviceRepository).should().findAll(predicate, pageable);
    }

    @Test
    public void createMicroservice(){
        microserviceService.createMicroservice(trainingMicroservice);
        then(microserviceRepository).should().save(trainingMicroservice);
    }
}
