package cz.muni.ics.kypo.userandgroup.rest.mapping;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BeanMappingImpl implements BeanMapping {

    private ModelMapper modelMapper;

    @Autowired
    public BeanMappingImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    @Override
    public <T> List<T> mapTo(Collection<?> objects, Class<T> mapToClass) {
        List<T> mappedCollection = new ArrayList<>();
        for (Object object : objects) {
            mappedCollection.add(modelMapper.map(object, mapToClass));
        }
        return mappedCollection;
    }

    @Override
    public <T> Set<T> mapToSet(Collection<?> objects, Class<T> mapToClass) {
        Set<T> mappedCollection = new HashSet<>();
        for (Object object : objects) {
            mappedCollection.add(modelMapper.map(object, mapToClass));
        }
        return mappedCollection;
    }

    @Override
    public <T> T mapTo(Object u, Class<T> mapToClass) {
        return modelMapper.map(u, mapToClass);
    }

}

