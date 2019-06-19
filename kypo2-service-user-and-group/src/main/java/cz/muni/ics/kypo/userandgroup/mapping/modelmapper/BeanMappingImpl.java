package cz.muni.ics.kypo.userandgroup.mapping.modelmapper;

import cz.muni.ics.kypo.userandgroup.api.config.PageResultResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Pavel Šeda (441048)
 */
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
    public <T> Page<T> mapTo(Page<?> objects, Class<T> mapToClass) {
        List<T> mappedCollection = new ArrayList<>();
        objects.forEach(obj -> {
            mappedCollection.add(modelMapper.map(obj, mapToClass));
        });
        return new PageImpl<T>(mappedCollection, objects.getPageable(), mappedCollection.size());
    }

    @Override
    public <T> PageResultResource<T> mapToPageResultDTO(Page<?> objects, Class<T> mapToClass) {
        List<T> mappedCollection = new ArrayList<>();
        objects.forEach(obj -> {
            mappedCollection.add(modelMapper.map(obj, mapToClass));
        });
        PageResultResource<T> pageResultDTO = new PageResultResource<T>(mappedCollection, createPagination(objects));
        return pageResultDTO;
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
    public <T> Optional<T> mapToOptional(Object u, Class<T> mapToClass) {
        return Optional.ofNullable(modelMapper.map(u, mapToClass));
    }

    @Override
    public <T> T mapTo(Object u, Class<T> mapToClass) {
        return modelMapper.map(u, mapToClass);
    }

    public boolean isCollection(Object obj) {
        return (obj instanceof Collection) || (obj instanceof Map);
    }

    private <T> PageResultResource.Pagination createPagination(Page<?> objects) {
        PageResultResource.Pagination pageMetadata = new PageResultResource.Pagination();
        pageMetadata.setNumber(objects.getNumber());
        pageMetadata.setNumberOfElements(objects.getNumberOfElements());
        pageMetadata.setSize(objects.getSize());
        pageMetadata.setTotalElements(objects.getTotalElements());
        pageMetadata.setTotalPages(objects.getTotalPages());
        return pageMetadata;
    }

}


