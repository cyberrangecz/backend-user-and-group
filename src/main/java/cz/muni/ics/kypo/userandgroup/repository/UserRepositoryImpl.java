package cz.muni.ics.kypo.userandgroup.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.muni.ics.kypo.userandgroup.domain.*;
import cz.muni.ics.kypo.userandgroup.dto.user.InitialOIDCUserDto;
import cz.muni.ics.kypo.userandgroup.exceptions.FileCannotReadException;
import cz.muni.ics.kypo.userandgroup.exceptions.FileNotFoundException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Repository
public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    @Value("${path.to.initial.oidc.users:#{null}}")
    private String initialOIDCUsers;
    private ObjectMapper yamlObjectMapper;

    public UserRepositoryImpl(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
        super(User.class);
        this.yamlObjectMapper = yamlObjectMapper;
    }

    public byte[] getInitialOIDCUsers() throws IOException {
        Path oidcUsersPath = Paths.get(initialOIDCUsers);
        if (!Files.isRegularFile(oidcUsersPath)) {
            throw new FileNotFoundException("File with the initial OIDC users doesn't exist. File: " + initialOIDCUsers);
        }
        if (!Files.isReadable(oidcUsersPath)) {
            throw new FileCannotReadException("Not enough permissions to read file with initial OIDC users. File: " + initialOIDCUsers);
        }
        return Files.readAllBytes(oidcUsersPath);
    }

    @Override
    public Page<User> usersNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;
        QMicroservice microservices = QMicroservice.microservice;

        JPQLQuery<User> queryIn = new JPAQueryFactory(entityManager).selectFrom(users)
                .join(users.groups, groups)
                .where(groups.id.eq(groupId));

        JPQLQuery<User> queryNotIn = new JPAQueryFactory(entityManager).selectFrom(users)
                .distinct()
                .leftJoin(users.groups, groups)
                .leftJoin(groups.roles, roles)
                .leftJoin(roles.microservice, microservices)
                .where(users.notIn(queryIn));

        if (predicate != null) {
            queryNotIn.where(predicate);
        }
        return getPage(queryNotIn, pageable);
    }

    @Override
    public Page<User> usersInGivenGroups(Set<Long> groupsIds, Predicate predicate, Pageable pageable) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;

        JPQLQuery<User> query = new JPAQueryFactory(entityManager).selectFrom(users)
                .distinct()
                .join(users.groups, groups)
                .where(groups.id.in(groupsIds));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    @Override
    public Page<User> findAllByRoleId(Long roleId, Predicate predicate, Pageable pageable) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;
        QMicroservice microservices = QMicroservice.microservice;

        JPQLQuery<User> query = new JPAQueryFactory(entityManager).selectFrom(users)
                .distinct()
                .join(users.groups, groups)
                .join(groups.roles, roles)
                .join(roles.microservice, microservices)
                .where(roles.id.eq(roleId));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    @Override
    public Page<User> findAllByRoleType(String roleType, Predicate predicate, Pageable pageable) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;
        QMicroservice microservices = QMicroservice.microservice;

        JPQLQuery<User> query = new JPAQueryFactory(entityManager).selectFrom(users)
                .distinct()
                .join(users.groups, groups)
                .join(groups.roles, roles)
                .join(roles.microservice, microservices)
                .where(roles.roleType.eq(roleType));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    @Override
    public Page<User> findAllByRoleAndNotWithIds(Predicate predicate, Pageable pageable, String roleType, Set<Long> userIds) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;
        QMicroservice microservices = QMicroservice.microservice;

        JPQLQuery<User> query = new JPAQueryFactory(entityManager).selectFrom(users)
                .distinct()
                .join(users.groups, groups)
                .join(groups.roles, roles)
                .join(roles.microservice, microservices)
                .where(roles.roleType.eq(roleType), users.id.notIn(userIds));

        if (predicate != null) {
            query.where(predicate);
        }
        return getPage(query, pageable);
    }

    private <T> Page getPage(JPQLQuery<T> query, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 20);
        }
        query = getQuerydsl().applyPagination(pageable, query);
        long count = query.fetchCount();
        return new PageImpl<>(query.fetch(), pageable, count);
    }

}
