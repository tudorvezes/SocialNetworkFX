package cs.ubb.socialnetworkfx.utils.events;

import cs.ubb.socialnetworkfx.domain.User;

public class StatusChangeEvent implements Event {
    private ChangeEventType type;
    private User from, to;
    boolean seen;

    public StatusChangeEvent(ChangeEventType type, User from, User to) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.seen = false;
    }

    public StatusChangeEvent(ChangeEventType type, User from, User to, boolean seen) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.seen = seen;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
