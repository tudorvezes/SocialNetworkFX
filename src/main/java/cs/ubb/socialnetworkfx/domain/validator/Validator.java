package cs.ubb.socialnetworkfx.domain.validator;

public interface Validator<E> {

    /**
     * This method validates an entity.
     * @param entity The entity to be validated.
     * @throws ValidationException If the entity is not valid.
     */
    void validate(E entity) throws ValidationException;
}
