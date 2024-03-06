package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.ChatRoomChangeEvent;

public interface ChatRoomObserver {
    void update(ChatRoomChangeEvent event);
}
