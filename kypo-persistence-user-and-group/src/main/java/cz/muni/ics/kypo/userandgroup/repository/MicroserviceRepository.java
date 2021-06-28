package cz.muni.ics.kypo.userandgroup.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringPath;
import cz.muni.ics.kypo.userandgroup.entities.Microservice;
import cz.muni.ics.kypo.userandgroup.entities.QIDMGroup;
import cz.muni.ics.kypo.userandgroup.entities.QMicroservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.Collection;
import java.util.Optional;

/**
 * The JPA repository interface to manage {@link Microservice} instances.
 */
public interface MicroserviceRepository extends JpaRepository<Microservice, Long>, QuerydslPredicateExecutor<Microservice>, QuerydslBinderCustomizer<QMicroservice> {

    /**
     * That method is used to make the query dsl string values case insensitive
     *
     * @param querydslBindings
     * @param qMicroservice
     */
    @Override
    default void customize(QuerydslBindings querydslBindings, QMicroservice qMicroservice) {
        querydslBindings.bind(String.class).all((StringPath path, Collection<? extends String> values) -> {
            BooleanBuilder predicate = new BooleanBuilder();
            values.forEach(value -> predicate.and(path.containsIgnoreCase(value)));
            return Optional.ofNullable(predicate);
        });
    }

    /**
     * Find Microservice by its name.
     *
     * @param name the name of the looking Microservice
     * @return the {@link Microservice} if it is found or null if it is not found. In both cases, the result is wrapped up in {@link Optional}.
     */
    Optional<Microservice> findByName(String name);

    /**
     * Check if microservice exist.
     *
     * @param name the name of the looking Microservice
     * @return true if microservice with given name exists in DB, false otherwise.
     */
    boolean existsByName(String name);
}
