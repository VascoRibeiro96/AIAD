package utils;

public class ParkInfo {
    public String name;
    public double price;
    public String type;
    public Coords location;
    public double utility;
    public ParkInfo (String name, String type, double price, double x, double y){
        this.name = name;
        this.price = price;
        this.type = type;
        location = new Coords(x,y);
    }
    public double getUtility(double driverUtil, double timePark, Coords dest){
        utility = driverUtil - (timePark*price) - dest.calculateDistance(location);
        return utility;
    }
}
