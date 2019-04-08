package pt.tecnico.sec;


public class Pair<Key, Value> {

    private final Key element0;
    private final Value element1;

    public static <Key,Value> Pair<Key, Value> createPair(Key element0, Value element1) {
        return new Pair<Key, Value>(element0, element1);
    }

    public Pair(Key element0, Value element1) {
        this.element0 = element0;
        this.element1 = element1;
    }

    public Key getKey() {
        return element0;
    }

    public Value getValue() {
        return element1;
    }

}