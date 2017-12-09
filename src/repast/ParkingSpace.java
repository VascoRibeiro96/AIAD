package repast;

import agents.Driver;
import agents.Park;
import uchicago.src.sim.space.Object2DGrid;

public class ParkingSpace {
	private Object2DGrid driverSpace;
	private Object2DGrid parkSpace;
	private Object2DGrid mapSpace;
	
	public ParkingSpace(int x, int y){
		driverSpace = new Object2DGrid(x,y);
		parkSpace = new Object2DGrid(x,y);
		mapSpace = new Object2DGrid(x,y);

		for(int i = 0;i < x; i++)
			for(int j = 0; j < y; j++){
                if((i % 2 == 0 && j % 2 != 0 ) || (i % 2 != 0 && j % 2 == 0 )) mapSpace.putObjectAt(i, j, new Integer(0));
                else mapSpace.putObjectAt(i, j, new Integer(1));
            }

	}

	public Object2DGrid getDriverSpace() {
		return driverSpace;
	}
	
	public Driver getDriverAt(int x, int y){
		return (Driver) driverSpace.getObjectAt(x,y);
	}

	public Park getParkAt(int x, int y) {
	    return (Park) parkSpace.getObjectAt(x,y);
    }

	public boolean addDriver(Driver d) {
		boolean retVal = false;
		int count = 0;
		int countLimit = driverSpace.getSizeX() * driverSpace.getSizeY();

		// drivers começam nas periferias do mapa
		while((!retVal) && (count < countLimit)){
		    double rand = Math.random();
		    int x,y;
		    if(rand >= 0 && rand < 0.5){
		        x = (Math.random() < 0.5) ? 0 : driverSpace.getSizeX()-1;
		        y = (int)(Math.random()*(driverSpace.getSizeY()));
            }
            else {
                y = (Math.random() < 0.5) ? 0 : driverSpace.getSizeY()-1;
                x = (int)(Math.random()*(driverSpace.getSizeX()));
            }
			if(getDriverAt(x,y) == null && setDest(d)){
				driverSpace.putObjectAt(x,y,d);
				d.setStart(x,y);
				retVal = true;
			}
			count++;
		}

		return retVal;
	}

	public boolean setDest(Driver d){
        int x = (int)(Math.random()*(parkSpace.getSizeX()));
        int y = (int)(Math.random()*(parkSpace.getSizeY()));
        if(x == 0 || x == parkSpace.getSizeX()-1) return false;
        if(y == 0 || y == parkSpace.getSizeX()-1) return false;
        if(getParkAt(x,y) == null){
            d.setDest(x,y);
            return true;
        }
        else return false;
    }

	public boolean addPark(Park p) {
	    boolean retVal = false;
	    int count = 0;
	    int countLimit = parkSpace.getSizeX() * parkSpace.getSizeY();

	    while((!retVal) && (count < countLimit)){
	        int x = (int)(Math.random()*(parkSpace.getSizeX()));
            int y = (int)(Math.random()*(parkSpace.getSizeY()));
            if(x == 0 || x == parkSpace.getSizeX()-1) continue;
            if(y == 0 || y == parkSpace.getSizeX()-1) continue;
            if(getParkAt(x,y) == null){
                parkSpace.putObjectAt(x,y,p);
                mapSpace.putObjectAt(x, y, new Integer(2));
                p.updateLocation(x,y);
                retVal = true;
            }
            count++;
        }
        return retVal;
    }

	public Object2DGrid getParkSpace() {
		return parkSpace;
	}

	public Object2DGrid getMapSpace() {
		return mapSpace;
	}


	public int getTotalMoney(){
		int totalMoney = 0;
		for(int k = 0; k < 100; k++){
			for(int l = 0; l < 100; l++){
				Park park = getParkAt(k,l); //ver coordenadas dos parks
				if(park != null){
					totalMoney += park.getTotalRevenue();//park.getPrice()*(park.getTotalSpots() - park.getSpots());
				}
			}
		}

		return totalMoney;

	}
}
