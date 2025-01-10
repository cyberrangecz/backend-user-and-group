package cz.cyberrange.platform.userandgroup.persistence.domain;

import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserEntityTest {


    @Autowired
    private TestEntityManager entityManager;

    private final String sub = "sub";

    @Test
    public void createWhenLoginIsNullShouldThrowException() {
        assertThrows(PersistenceException.class, () -> this.entityManager.persist(new User(null, "https://oidc.provider.cz/oidc/")));
    }

    @Test
    public void saveShouldPersistData() {
        User u = this.entityManager.persistFlushFind(new User(sub, "https://oidc.provider.cz/oidc/"));
        assertEquals(sub, u.getSub());
    }
}
