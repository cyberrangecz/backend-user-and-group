package cz.muni.ics.kypo.userandgroup.service;

import com.querydsl.core.types.Predicate;
import cz.muni.ics.kypo.userandgroup.exceptions.UserAndGroupServiceException;
import cz.muni.ics.kypo.userandgroup.model.Microservice;
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

    @Test
    public void getMicroserviceByIdNotFound(){
        Long id = 100L;
        thrown.expect(UserAndGroupServiceException.class);
        thrown.expectMessage("Microservice with id " + id + " not found");
        given(microserviceRepository.findById(id)).willReturn(Optional.empty());
        microserviceService.getMicroserviceById(id);
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
        given(microserviceRepository.findByName(trainingMicroservice.getName())).willReturn(Optional.empty());
        boolean response = microserviceService.createMicroservice(trainingMicroservice);
        assertTrue(response);
    }

    @Test
    public void createMicroserviceWithExistingMicroservice(){
        given(microserviceRepository.findByName(uAGMicroservice.getName())).willReturn(Optional.of(uAGMicroservice));
        boolean response = microserviceService.createMicroservice(uAGMicroservice);
        assertFalse(response);
    }
}
