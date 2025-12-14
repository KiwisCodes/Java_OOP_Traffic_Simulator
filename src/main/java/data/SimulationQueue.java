package data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A thread-safe communication channel between the Simulation Thread (Producer) 
 * and the JavaFX UI Thread (Consumer).
 * <p>
 * This class implements the <b>Producer-Consumer</b> design pattern using a {@link BlockingQueue}.
 * It ensures that the heavy calculations performed by SUMO do not freeze the User Interface
 * by buffering the results in a thread-safe manner.
 * </p>
 * @author pth
 * @version 1.0
 */
public class SimulationQueue {
    
    /** The underlying thread-safe queue storage. */
	private final BlockingQueue<SimulationState> queue;

    /**
     * Constructs a new {@code SimulationQueue} with a fixed capacity.
     *
     * @param capacity The maximum number of simulation states (frames) the queue can hold 
     * before blocking the producer. This prevents memory overflow if the UI lags.
     */
	public SimulationQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Inserts a simulation state into the queue, waiting if necessary for space to become available.
     * <p>
     * <b>Usage:</b> This method is called by the <i>Simulation Thread (Producer)</i>.
     * </p>
     *
     * @param frame The computed state of the simulation (snapshot).
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
	public void putState(SimulationState frame) throws InterruptedException {
        queue.put(frame);
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     * <p>
     * <b>Usage:</b> General purpose blocking retrieval.
     * </p>
     *
     * @return The next {@link SimulationState} object.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public SimulationState takeState() throws InterruptedException {
        return queue.take(); 
    }
    
    /**
     * Retrieves and removes the head of this queue, or returns {@code null} if this queue is empty.
     * <p>
     * <b>Usage:</b> This method is called by the <i>JavaFX AnimationTimer (Consumer)</i>. 
     * It is <b>non-blocking</b> to ensure the UI never freezes, even if the simulation loop 
     * is running slower than the frame rate.
     * </p>
     *
     * @return The next {@link SimulationState} object, or {@code null} if the queue is empty.
     * @throws InterruptedException if the thread is interrupted.
     */
    public SimulationState pollState() throws InterruptedException{
    	return queue.poll();
    }
    
    /**
     * Inserts the specified element into this queue if it is possible to do so immediately 
     * without violating capacity restrictions.
     *
     * @param frame The state to add.
     * @throws InterruptedException if the thread is interrupted.
     */
    public void offerState(SimulationState frame) throws InterruptedException{
    	queue.offer(frame);
    }

}