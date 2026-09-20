package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringPath;
import cz.cyberrange.platform.userandgroup.persistence.entity.Microservice;
import cz.cyberrange.platform.userandgroup.persistence.entity.QMicroservice;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/** The JPA repository interface to manage {@link Microservice} instances. */
public interface MicroserviceRepository
    extends JpaRepository<Microservice, Long>,
        QuerydslPredicateExecutor<Microservice>,
        QuerydslBinderCustomizer<QMicroservice> {

  /**
   * Binds every string-typed property so QueryDSL predicates match its values case-insensitively.
   *
   * @param querydslBindings bindings being customized for this repository
   * @param qMicroservice unused
   */
  @Override
  default void customize(QuerydslBindings querydslBindings, QMicroservice qMicroservice) {
    querydslBindings
        .bind(String.class)
        .all(
            (StringPath path, Collection<? extends String> values) -> {
              BooleanBuilder predicate = new BooleanBuilder();
              values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
              return Optional.ofNullable(predicate);
            });
  }

  /**
   * Finds the microservice with the given name.
   *
   * @param name name to match
   * @return the matching microservice, or an empty Optional when no microservice has that name
   */
  Optional<Microservice> findByName(String name);

  /**
   * Checks whether a microservice with the given name exists.
   *
   * @param name name to match
   * @return true when a microservice has that name, false otherwise
   */
  boolean existsByName(String name);
}
