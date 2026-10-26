package cz.cyberrange.platform.userandgroup.persistence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/** Enables the JPA repositories with their paged queries ordered by {@code id} as a last key. */
@Configuration
@EnableJpaRepositories(
    basePackages = "cz.cyberrange.platform.userandgroup.persistence.repository",
    repositoryFactoryBeanClass = IdOrderJpaRepositoryFactoryBean.class)
public class PersistenceConfiguration {}
