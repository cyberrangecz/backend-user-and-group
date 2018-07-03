package cz.muni.ics.kypo.userandgroup.rest.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BeanMapping {

    <T> List<T> mapTo(Collection<?> objects, Class<T> mapToClass);

    <T> Set<T> mapToSet(Collection<?> objects, Class<T> mapToClass);

    <T> T mapTo(Object u, Class<T> mapToClass);
}
