package cs.ubb.socialnetworkfx.domain;

import java.io.Serial;
import java.io.Serializable;

public class Entity<ID> implements Serializable {
    protected ID id;
    @Serial
    private static final long serialVersionUID = 7331115341259248461L;

    /**
     * @return the id of the entity
     */
    public ID getId() {
        return id;
    }

    /**
     * @param id the new id of the entity
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * @param o the object to be compared to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity<?> entity = (Entity<?>) o;
        return getId().equals(entity.getId());
    }

    /**
     * @return the hashcode of the entity
     */
    @Override
    public int hashCode(){
        return id.hashCode();
    }

    /**
     * @return the string representation of the entity
     */
    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                '}';
    }

}
