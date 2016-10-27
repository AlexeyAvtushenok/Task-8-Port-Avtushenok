package main;

import java.util.ArrayList;
import java.util.List;

import port.Port;
import ship.Ship;
import warehouse.Container;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		int warehousePortSize = 15;

        Port port = new Port(2, 90);

        List<Container> containerList = new ArrayList<Container>(warehousePortSize);
		for (int i=0; i<warehousePortSize; i++){
			containerList.add(new Container(i));
		}
		port.setContainersToWarehouse(containerList);


		Ship ship1 = new Ship("Ship1", port, 90);
		containerList = new ArrayList<Container>(warehousePortSize);
		for (int i=0; i<warehousePortSize; i++){
			containerList.add(new Container(i+30));
		}
		ship1.setContainersToWarehouse(containerList);

        Ship ship2 = new Ship("Ship2", port, 90);
		containerList = new ArrayList<Container>(warehousePortSize);
		for (int i=0; i<warehousePortSize; i++){
			containerList.add(new Container(i+45));
		}
        ship2.setContainersToWarehouse(containerList);

        Ship ship3 = new Ship("Ship3", port, 90);
		containerList = new ArrayList<Container>(warehousePortSize);
		for (int i=0; i<warehousePortSize; i++){
			containerList.add(new Container(i+60));
		}
        ship3.setContainersToWarehouse(containerList);
		
		
		new Thread(ship1).start();		
		new Thread(ship2).start();		
		new Thread(ship3).start();
		

//		Thread.sleep(3000);
//
//		ship1.stopThread();
//		ship2.stopThread();
//		ship3.stopThread();

	}

}
