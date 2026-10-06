package cz.cyberrange.platform.userandgroup.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Base persistence entity that gives every concrete entity a database-generated primary key.
 *
 * @param <PK> type of the primary key
 */
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false, insertable = false)
  private PK id;

  public AbstractEntity() {}

  public PK getId() {
    return id;
  }

  public void setId(PK id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "AbstractEntity{" + "id=" + id + '}';
  }
}
