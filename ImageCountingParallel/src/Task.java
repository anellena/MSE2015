
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class Task extends Thread
{
	private int index;
	private int objectCounter = 0;
	private int taskCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;
	private Semaphore workingLock, communicationLock;

	public Task(int index)
	{
		this.index = index;
		this.workingLock = new Semaphore(1);
		this.communicationLock = new Semaphore(1);
		this.taskAcquire(workingLock);
	}
	
	public void run()
	{
		System.out.println("Start Task " + this.index);
		this.taskAcquire(this.workingLock);
		
		BufferedImage[][] slices = new BufferedImage[1][6];
		imageProcessor = new ImageProcessing();
		slices = imageProcessor.DivideImage(image, 1, 6);
		for(int i = 0; i < 6; i++) {
			int childIndex = (this.index*6) + i;
			sendImageRange(childIndex, slices[0][i]);
			TaskHolder.getChildTaskByIndex(childIndex).start();
		}
		
		for(int i = (this.index*6); i < ((this.index*6) + 6); i++) {
			try {
				TaskHolder.getChildTaskByIndex(i).join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("Counted " + objectCounter + " objects at task: " + index);
		sendCount();
		System.out.println("Ending Task" + index);
	}
	
	public synchronized void sendCount()
	{
		TaskHolder.getMainTask().receiveCount(index, objectCounter);
	}
	
	public synchronized void receiveRange(BufferedImage finalImage)
	{
		this.image = finalImage;
		this.workingLock.release();
	}
	
	public synchronized void receiveCount(int index, int objectCount)
	{
		this.taskAcquire(this.communicationLock);
		this.objectCounter += objectCount;
		this.taskCounter++;
		this.communicationLock.release();
	}
	
	public synchronized void sendImageRange(int index, BufferedImage image)
	{
		TaskHolder.getChildTaskByIndex(index).receiveRange(image);
	}
	
	public String toString()
	{
		return "\nTask_" + index ;
	}

	private void taskAcquire(Semaphore lock){
		try{
			lock.acquire();
		}
		catch (InterruptedException e) {
			System.out.println("Fatal error on task id: %d" + this.index);
		}
	}

}
