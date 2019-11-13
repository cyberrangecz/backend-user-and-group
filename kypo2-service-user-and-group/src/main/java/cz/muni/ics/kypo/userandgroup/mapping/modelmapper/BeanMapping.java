package cz.muni.ics.kypo.userandgroup.mapping.modelmapper;

import cz.muni.ics.kypo.userandgroup.api.dto.PageResultResource;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * That class is used for mapping entities to dto classes. Example of usage for this mapper shown
 * below:
 *
 * <pre>
 * <code>
 public class UserFacadeImpl implements UserFacade {

 &#64;Autowired
 private BeanMapping beanMapping;

 &#64;Override
 public List&lt;UserDTO&gt; getAllUsers() {
 return beanMapping.mapTo(userService.findAll(), UserDTO.class);
 }
 }
 * </code>
 * </pre>
 *
 * This example shows mapping list of user entities to list of user dto classes.
 *
 * @author Pavel Šeda (441048)
 *
 */
public interface BeanMapping {

    <T> List<T> mapTo(Collection<?> objects, Class<T> mapToClass);

    <T> Page<T> mapTo(Page<?> objects, Class<T> mapToClass);

    <T> PageResultResource<T> mapToPageResultDTO(Page<?> objects, Class<T> mapToClass);

    <T> Set<T> mapToSet(Collection<?> objects, Class<T> mapToClass);

    <T> Optional<T> mapToOptional(Object u, Class<T> mapToClass);

    <T> T mapTo(Object u, Class<T> mapToClass);

}

