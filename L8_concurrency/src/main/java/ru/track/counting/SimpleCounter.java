package ru.track.counting;

/**
 *
 */
public class SimpleCounter implements Counter {
    private long val;

    public long inc() {
        return val++;
    }

}
