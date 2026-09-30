package cz.cyberrange.platform.userandgroup.persistence.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cz.cyberrange.platform.userandgroup.persistence.entity.User;
import javax.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class UserEntityTest {

  private final String sub = "sub";
  @Autowired private TestEntityManager entityManager;

  @Test
  public void createWhenLoginIsNullShouldThrowException() {
    assertThrows(
        PersistenceException.class,
        () -> this.entityManager.persist(new User(null, "https://oidc.provider.cz/oidc/")));
  }

  @Test
  public void saveShouldPersistData() {
    User u = this.entityManager.persistFlushFind(new User(sub, "https://oidc.provider.cz/oidc/"));
    assertEquals(sub, u.getSub());
  }
}
