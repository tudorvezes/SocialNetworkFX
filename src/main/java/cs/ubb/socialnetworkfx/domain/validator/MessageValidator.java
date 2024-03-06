package cs.ubb.socialnetworkfx.domain.validator;

import cs.ubb.socialnetworkfx.domain.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getContent().isEmpty()) {
            throw new ValidationException("Message content cannot be empty!");
        }
    }
}
