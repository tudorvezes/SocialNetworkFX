package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;

public interface StatusObserver {
    void update(StatusChangeEvent event);
}
