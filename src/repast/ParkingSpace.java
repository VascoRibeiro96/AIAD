package repast;

import agents.Driver;
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
			for(int j = 0; j < y; j++)
				mapSpace.putObjectAt(i, j, new Integer(0));
	}

	public Object2DGrid getDriverSpace() {
		return driverSpace;
	}
	
	public Driver getDriverAt(int x, int y){
		return (Driver) driverSpace.getObjectAt(x,y);
	}
	
	public boolean addDriver(Driver d) {
		boolean retVal = false;
		int count = 0;
		int countLimit = driverSpace.getSizeX() * driverSpace.getSizeY();

		while((!retVal) && (count < countLimit)){
			int x = (int)(Math.random()*(driverSpace.getSizeX()));
			int y = (int)(Math.random()*(driverSpace.getSizeY()));
			if(getDriverAt(x,y) == null){
				driverSpace.putObjectAt(x,y,d);
				d.setStart(x,y);
				retVal = true;
			}
			count++;
		}

		return retVal;
	}

	public Object2DGrid getParkSpace() {
		return parkSpace;
	}

	public void setParkSpace(Object2DGrid parkSpace) {
		this.parkSpace = parkSpace;
	}

	public Object2DGrid getMapSpace() {
		return mapSpace;
	}

	public void setMapSpace(Object2DGrid mapSpace) {
		this.mapSpace = mapSpace;
	}
}
