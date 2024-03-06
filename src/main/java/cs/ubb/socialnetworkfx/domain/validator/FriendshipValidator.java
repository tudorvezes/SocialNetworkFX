package cs.ubb.socialnetworkfx.domain.validator;

import cs.ubb.socialnetworkfx.domain.Friendship;

import java.util.Objects;

public class FriendshipValidator implements Validator<Friendship> {

    /**
     * This method validates a Frindship entity.
     *
     * @param entity The entity to be validated.
     * @throws ValidationException If the entity is not valid.
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(Objects.equals(entity.getUser1(), entity.getUser2()))
            throw new ValidationException("Users must be different!");
    }
}