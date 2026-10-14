package cz.cyberrange.platform.userandgroup.persistence.config;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * JPA repository factory bean whose repositories complete the sort of every {@link
 * org.springframework.data.domain.Pageable} argument with {@code id}.
 *
 * @param <T> repository type
 * @param <S> domain type of the repository
 * @param <ID> identifier type of the domain type
 */
public class IdOrderJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends JpaRepositoryFactoryBean<T, S, ID> {

  public IdOrderJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
    RepositoryFactorySupport repositoryFactory = super.createRepositoryFactory(entityManager);
    repositoryFactory.addRepositoryProxyPostProcessor(
        (proxyFactory, repositoryInformation) ->
            proxyFactory.addAdvice(new IdOrderRepositoryInterceptor()));
    return repositoryFactory;
  }
}
