package model;

public class House extends Coordinate {

    int connectedWell = -1;
    boolean marked = false;

    public House(int x, int y) {
        super(x, y);
    }

    public int getConnectedWell() {
        return connectedWell;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setConnectedWell(int connectedWell) {
        this.connectedWell = connectedWell;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
