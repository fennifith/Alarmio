package me.jfenn.alarmio.interfaces;

/**
 * A basic interface to allow views, dialogs, and activities
 * and stuff to be subscribed and un-subscribed easily
 * to adhere to their respective life-cycles.
 */
public interface Subscribblable {

    void subscribe();

    void unsubscribe();

}
