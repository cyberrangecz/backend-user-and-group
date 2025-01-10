package cz.cyberrange.platform.userandgroup.persistence.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import cz.cyberrange.platform.userandgroup.entity.QIDMGroup;
import cz.cyberrange.platform.userandgroup.entity.QRole;
import cz.cyberrange.platform.userandgroup.entity.QUser;
import cz.cyberrange.platform.userandgroup.persistence.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class RoleRepositoryCustomImpl extends QuerydslRepositorySupport implements RoleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public RoleRepositoryCustomImpl() {
        super(Role.class);
    }

    @Override
    public Page<Role> findAllOfUser(Long userId, Pageable pageable, Predicate predicate) {
        QUser users = QUser.user;
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;

        JPQLQuery<Role> queryRoleOfUser = new JPAQueryFactory(entityManager).select(roles).from(groups)
                .distinct()
                .join(groups.users, users)
                .join(groups.roles, roles)
                .where(users.id.eq(userId));

        if (predicate != null) {
            queryRoleOfUser.where(predicate);
        }
        return getPage(queryRoleOfUser, pageable);
    }

    @Override
    public Page<Role> findAllOfGroup(Long groupId, Pageable pageable, Predicate predicate) {
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;

        JPQLQuery<Role> rolesQuery = new JPAQueryFactory(entityManager).select(roles).from(groups)
                .distinct()
                .join(groups.roles, roles)
                .where(groups.id.eq(groupId));

        if (predicate != null) {
            rolesQuery.where(predicate);
        }
        return getPage(rolesQuery, pageable);
    }

    @Override
    public Page<Role> rolesNotInGivenGroup(Long groupId, Predicate predicate, Pageable pageable) {
        QIDMGroup groups = QIDMGroup.iDMGroup;
        QRole roles = QRole.role;

        JPQLQuery<Role> rolesQuery = new JPAQuery<>(entityManager)
                .from(roles)
                .where(roles.id.notIn(
                        JPAExpressions
                                .select(roles.id)
                                .from(groups)
                                .join(groups.roles, roles)
                                .where(groups.id.eq(groupId))
                )).select(roles);

        if (predicate != null) {
            rolesQuery.where(predicate);
        }
        return getPage(rolesQuery, pageable);
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
