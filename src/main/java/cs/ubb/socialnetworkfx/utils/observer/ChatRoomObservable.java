package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;

public interface ChatRoomObservable {
    /**
     * Adds an observer to the list of observers.
     * @param observer ChatRoomObserver - the observer to be added
     */
    void addObserver(ChatRoomObserver observer);

    /**
     * Removes an observer from the list of observers.
     * @param observer ChatRoomObserver - the observer to be removed
     */
    void removeObserver(ChatRoomObserver observer);

    /**
     * Notifies all observers that a change has occurred.
     * @param event ChatRoomChangeEvent - the event that occurred
     */
    void notifyObservers(ChatRoomChangeEvent event);
}
