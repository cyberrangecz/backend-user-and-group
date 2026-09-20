package cz.cyberrange.platform.userandgroup.persistence.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
