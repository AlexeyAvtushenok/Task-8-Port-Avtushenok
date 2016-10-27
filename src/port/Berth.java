package port;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import warehouse.Container;
import warehouse.Warehouse;

public class Berth {
	private static final Logger logger = Logger.getRootLogger();
	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	//неправильно задано условие загрузки контейнеров
	public boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();
		boolean portLock = false;

		try{

			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				//int newConteinerCount = portWarehouse.getRealSize()	+ numberOfConteiners;
				if (numberOfConteiners <= portWarehouse.getFreeSize()) {
					result = doMoveFromShip(shipWarehouse, numberOfConteiners);	
				} else {
					logger.debug("На складе порта нет места для контейнеров корабля ");
				}

			}
		} finally{
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}
	
	private boolean doMoveFromShip(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException{
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;
		
		try{
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				if(shipWarehouse.getRealSize() >= numberOfConteiners){
					List<Container> containers = shipWarehouse.getContainer(numberOfConteiners);
					portWarehouse.addContainer(containers);
					return true;
				}
				else {
					logger.debug("На корабле  нет столько контейнеров");
				}
			}
		}finally{
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}
		
		return false;		
	}


	public boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();	
		boolean portLock = false;

		try{
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				if (numberOfConteiners <= portWarehouse.getRealSize()) {
					result = doMoveFromPort(shipWarehouse, numberOfConteiners);	
				}
				else {
					logger.debug("На складе порта нет столько контейнеров");
				}

			}
		} finally{
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}

	//неправильно задано условие отгрузки контейнеров со склада корабля
	private boolean doMoveFromPort(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException{
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;
		
		try{
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				//int newConteinerCount = shipWarehouse.getRealSize() + numberOfConteiners;
				if(numberOfConteiners <= shipWarehouse.getFreeSize()){
					List<Container> containers = portWarehouse.getContainer(numberOfConteiners);
					shipWarehouse.addContainer(containers);
					return true;
				}
				else {
					logger.debug("На cкладе корабля нет места");
				}
			}
		}finally{
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}
		
		return false;		
	}
}
