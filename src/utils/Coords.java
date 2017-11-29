package utils;

public class Coords {
    public double x,y;
    public Coords(double x,double y){
        this.x = x;
        this.y = y;
    }

    public double calculateDistance(Coords p){
        return Math.sqrt(Math.pow(x-p.x,2) + Math.pow(y-p.y,2));
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
