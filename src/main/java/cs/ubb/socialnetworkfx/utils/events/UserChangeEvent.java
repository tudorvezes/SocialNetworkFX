package cs.ubb.socialnetworkfx.utils.events;

import cs.ubb.socialnetworkfx.domain.User;

public class UserChangeEvent implements Event {
    private ChangeEventType type;
    private User data, oldData;

    public UserChangeEvent(ChangeEventType type, User data) {
        this.type = type;
        this.data = data;
    }

    public UserChangeEvent(ChangeEventType type, User data, User oldData) {
        this.type = type;
        this.data = data;
        this.oldData = oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getData() {
        return data;
    }

    public User getOldData() {
        return oldData;
    }
}
