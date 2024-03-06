package cs.ubb.socialnetworkfx.utils;

public class Pair<T1,T2> {
    private T1 first;
    private T2 second;

    public Pair() {
        this.first = null;
        this.second = null;
    }

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair<T1,T2> pair) {
        this.first = pair.getFirst();
        this.second = pair.getSecond();
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setFirst(T1 first) {
        this.first = first;
    }

    public void setSecond(T2 second) {
        this.second = second;
    }
}
