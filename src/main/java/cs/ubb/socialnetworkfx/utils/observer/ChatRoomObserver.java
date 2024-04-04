package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;

public interface ChatRoomObserver {

    /**
     * Updates the observer with the given event.
     * @param event ChatRoomChangeEvent - the event that occurred
     */
    void update(ChatRoomChangeEvent event);
}
