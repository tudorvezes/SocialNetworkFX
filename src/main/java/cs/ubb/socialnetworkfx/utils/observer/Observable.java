package cs.ubb.socialnetworkfx.utils.observer;

import cs.ubb.socialnetworkfx.utils.events.Event;

public interface Observable<E extends Event> {

    /**
     * Adds an observer to the list of observers.
     * @param e Observer<E> - the observer to be added
     */
    void addObserver(Observer<E> e);

    /**
     * Removes an observer from the list of observers.
     * @param e Observer<E> - the observer to be removed
     */
    void removeObserver(Observer<E> e);

    /**
     * Notifies all observers that a change has occurred.
     * @param t E - the event that occurred
     */
    void notifyObservers(E t);
}
