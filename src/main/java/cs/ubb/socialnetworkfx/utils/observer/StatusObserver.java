package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;

public interface StatusObserver {
    /**
     * Updates the observer with the given event.
     * @param event StatusChangeEvent - the event that occurred
     */
    void update(StatusChangeEvent event);
}
