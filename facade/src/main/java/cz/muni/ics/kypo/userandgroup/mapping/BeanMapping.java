package cz.muni.ics.kypo.userandgroup.mapping;

import cz.muni.ics.kypo.userandgroup.api.PageResultResource;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * That class is used for mapping entities to DTO classes. Example of usage for this mapper shown
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
 * This example shows mapping list of user entities to list of user DTO classes.
 *
 * @author Pavel Šeda (441048)
 *
 */
public interface BeanMapping {

    public <T> List<T> mapTo(Collection<?> objects, Class<T> mapToClass);

    public <T> Page<T> mapTo(Page<?> objects, Class<T> mapToClass);

    public <T> PageResultResource<T> mapToPageResultDTO(Page<?> objects, Class<T> mapToClass);

    public <T> Set<T> mapToSet(Collection<?> objects, Class<T> mapToClass);

    public <T> Optional<T> mapToOptional(Object u, Class<T> mapToClass);

    public <T> T mapTo(Object u, Class<T> mapToClass);

}

