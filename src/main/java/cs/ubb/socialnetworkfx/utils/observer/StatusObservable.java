package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;

public interface StatusObservable {
    void addObserver(StatusObserver observer);
    void removeObserver(StatusObserver observer);
    void notifyObservers(StatusChangeEvent event);
}
