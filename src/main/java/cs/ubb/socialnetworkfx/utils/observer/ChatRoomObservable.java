package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;

public interface ChatRoomObservable {
    void addObserver(ChatRoomObserver observer);
    void removeObserver(ChatRoomObserver observer);
    void notifyObservers(ChatRoomChangeEvent event);
}
