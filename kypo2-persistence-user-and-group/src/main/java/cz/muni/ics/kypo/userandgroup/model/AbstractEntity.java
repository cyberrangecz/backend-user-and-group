package cz.muni.ics.kypo.userandgroup.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @param <PK> Primary key for a given entity.
 * @author Pavel Seda
 */
@MappedSuperclass
public class AbstractEntity<PK extends Serializable> implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, insertable = false)
    private PK id;

    public AbstractEntity() {
    }

    public PK getId() {
        return id;
    }

    public void setId(PK id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AbstractEntity{" +
                "id=" + id +
                '}';
    }
}
