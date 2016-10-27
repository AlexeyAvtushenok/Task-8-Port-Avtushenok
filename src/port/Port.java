package port;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private final static Logger logger = Logger.getLogger(Port.class);

	private BlockingQueue<Berth> berthList; // очередь причалов
	private Warehouse portWarehouse; // хранилище порта
	private Map<Ship, Berth> usedBerths; // какой корабль у какого причала стоит


	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // создаем пустое хранилище
		berthList = new ArrayBlockingQueue<Berth>(berthSize); // создаем очередь причалов
		for (int i = 0; i < berthSize; i++) { // заполняем очередь причалов непосредственно самими причалами
			berthList.add(new Berth(i, portWarehouse));
		}
		usedBerths = new ConcurrentHashMap<Ship, Berth>(); // создаем объект, который будет
		// хранить связь между кораблем и причалом usedBerths - разделяемый ресурс
		logger.debug("Порт создан.");
	}
	
	public void setContainersToWarehouse(List<Container> containerList){
		portWarehouse.addContainer(containerList);
		logger.debug("Склад порта: " + portWarehouse.getRealSize() + "/" + portWarehouse.getSize());
	}

	public boolean lockBerth(Ship ship) {
		Berth berth;
		try {
			berth = berthList.take();
			logger.debug("Корабль " + ship.getName() + " заблокировал причал " + berth.getId());
			usedBerths.put(ship, berth);
			logger.debug("Корабль " + ship.getName() + " внес запись в журнал о причале " + berth.getId());
		} catch (InterruptedException e) {
			logger.debug("Кораблю " + ship.getName() + " отказано в швартовке.");

			return false;
		}		
		return true;
	}
	
	//был неправильный порядок удаления из журнала/ разблокировки причала
	public boolean unlockBerth(Ship ship) {
		Lock lock = new ReentrantLock();

		
		try {
			lock.lock();
			Berth berth = usedBerths.get(ship);

			usedBerths.remove(ship);// сначала удаляем связь корабля-причала
			logger.debug("Корабль " + ship.getName() + " удалил запись о причале " + berth.getId());

			berthList.put(berth); // потом добавляем прчал в список(разблокируем его)
			logger.debug("Корабль " + ship.getName() + " разблокировал причал " + berth.getId());

		} catch (InterruptedException e) {
			logger.debug("Корабль " + ship.getName() + " не смог отшвартоваться.");
			return false;
		}
		finally {
			lock.unlock();
		}
		return true;
	}
	
	public Berth getBerth(Ship ship) throws PortException {
		
		Berth berth = usedBerths.get(ship);
		if (berth == null){
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;		
	}
}
