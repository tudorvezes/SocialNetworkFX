package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.StatusChangeEvent;

public interface StatusObservable {
    /**
     * Adds an observer to the list of observers.
     * @param observer StatusObserver - the observer to be added
     */
    void addObserver(StatusObserver observer);

    /**
     * Removes an observer from the list of observers.
     * @param observer StatusObserver - the observer to be removed
     */
    void removeObserver(StatusObserver observer);

    /**
     * Notifies all observers that a change has occurred.
     * @param event StatusChangeEvent - the event that occurred
     */
    void notifyObservers(StatusChangeEvent event);
}
