package de.unima.info.ki.minesweeper.api;

public class Tuple {
    private int x;
    private int y;
    private boolean value;

    /**
     * Sind die Zellen
     * @param x
     * @param y
     */
    public Tuple(int x, int y){
        this.x = x;
        this.y = y;
        value = false;
    }

    public Tuple(int x, int y, boolean value){
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public boolean isTupel(int x, int y) {
        return (this.x == x && this.y == y);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        value = value;
    }
}
