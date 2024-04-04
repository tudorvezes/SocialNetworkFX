package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.Event;

public interface Observer<E extends Event> {
    /**
     * Updates the observer with the given event.
     * @param e E - the event that occurred
     */
    void update(E e);
}
