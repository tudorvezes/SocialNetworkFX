package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}
