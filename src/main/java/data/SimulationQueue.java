package data;

import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.*;


public class SimulationQueue {
	private final BlockingQueue<SimulationState> queue;
	public SimulationQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }
	public void putState(SimulationState frame) throws InterruptedException {
        queue.put(frame);
    }

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
