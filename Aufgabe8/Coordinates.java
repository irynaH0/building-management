package Aufgabe8;

import java.util.Objects;

public class Coordinates {
    public final int x;
    public final int y;
    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinates)) return false;
        Coordinates other = (Coordinates) obj;
        return this.x == other.x && this.y == other.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
