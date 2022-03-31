package model;

import java.util.ArrayList;
import java.util.List;

public class Well extends Coordinate {

    List<Integer> connectedHouses;
    boolean marked = false;

    public Well(int x, int y) {
        super(x, y);
        connectedHouses = new ArrayList<>();
    }

    public List<Integer> getConnectedHouses() {
        return connectedHouses;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setConnectedHouses(List<Integer> connectedHouses) {
        this.connectedHouses = connectedHouses;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}