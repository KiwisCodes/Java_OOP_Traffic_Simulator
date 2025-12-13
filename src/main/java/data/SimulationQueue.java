package data;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.*;

/**
 * Keeping a list of simulation states thread-safe 
 */
public class SimulationQueue {
	private final BlockingQueue<SimulationState> queue;
	/**
	 * Constructor
	 * @param capacity: the maximum length of the queue
	 */
	public SimulationQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }
	/**
	 * put new state to the queue
	 * @param frame: state
	 * @throws InterruptedException throws when action is interrupted
	 */
	public void putState(SimulationState frame) throws InterruptedException {
        queue.put(frame);
    }
	/**
	 * take 
	 * @return
	 * @throws InterruptedException
	 */
    public SimulationState takeState() throws InterruptedException {
        return queue.take(); 
    }
    
    public SimulationState pollState() throws InterruptedException{
    	return queue.poll();
    }
    
    public void offerState(SimulationState frame) throws InterruptedException{
    	queue.offer(frame);
    }

}
