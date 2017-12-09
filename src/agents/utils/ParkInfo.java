package agents.utils;

public class ParkInfo {
    public String name;
    public double price;
    public String type;
    public Coords location;
    public double utility;
    public double hourInflation;
    public ParkInfo (String name, String type, double price, double x, double y, double hourInflation){
        this.name = name;
        this.price = price;
        this.type = type;
        this.hourInflation = hourInflation;
        location = new Coords(x,y);
    }
    public double getUtility(double driverUtil, double timePark, Coords dest){
        double totalPrice = 0;
        double inflationValue = price*hourInflation;
        for(int i = 0; i < timePark; i++){
            if(i==0) totalPrice = price;
            else totalPrice += Math.pow(price +inflationValue,i);
        }
        utility = driverUtil - totalPrice - dest.calculateDistance(location);
        return utility;
    }
}
