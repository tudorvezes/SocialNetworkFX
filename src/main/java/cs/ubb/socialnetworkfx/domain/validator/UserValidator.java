package cs.ubb.socialnetworkfx.domain.validator;

import cs.ubb.socialnetworkfx.domain.User;

public class UserValidator implements Validator<User>{

    /**
     * This method validates a User entity.
     *
     * @param entity The entity to be validated.
     * @throws ValidationException If the entity is not valid.
     */
    @Override
    public void validate(User entity) throws ValidationException {
        StringBuilder errorString = new StringBuilder();
        if(entity == null) {
            errorString.append("entity must be not null!\n");
        }
        if(entity.getUsername().length() < 4) {
            errorString.append("username must be at least 4 characters long!\n");
        }
        if(!entity.getUsername().matches("[a-z0-9_-]+")) {
            errorString.append("username can contain only uncapitalized letters, digits, dashes and underscores!\n");
        }
        if(entity.getName().isEmpty()) {
            errorString.append("name must be not empty!\n");
        }
        if (!errorString.isEmpty()) {
            throw new ValidationException(errorString.toString());
        }
    }
}
