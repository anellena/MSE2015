
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.concurrent.Semaphore;

public class Task extends Thread
{
	private MainTask mainTask;
	private ChildTask childTask[] = new ChildTask[6];;
	private int index;
	private int taskCounter = 0;
	private int objectCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;
	private Semaphore lock;

	public Task(int index, MainTask mainTask)
	{
		this.mainTask = mainTask;
		this.index = index;
		this.lock = new Semaphore(1);
		this.taskAcquire();
	}
	
	public void run()
	{
		System.out.println("Start Task" + index);
		this.taskAcquire();
		
		//objectCounter = imageProcessor.ObjectsCount(image);
		for(int i = 0; i < childTask.length; i++) {
			childTask[i] = new ChildTask(i, this, index);
		}
		
		BufferedImage[][] slices = new BufferedImage[1][6];
		imageProcessor = new ImageProcessing();
		slices = imageProcessor.DivideImage(image, 1, 6);
		for(int index = 0; index < childTask.length; index++) {
			// TODO - IMPROVE
			sendImageRange(index, slices[0][index]);
			childTask[index].start();
		}
		
		try
		{ 
			while(taskCounter < childTask.length) 
				Thread.sleep(500);
		}	
		catch(InterruptedException e) 
		{ 
			notifyAll(); 
		}
		
		System.out.println("Counted " + objectCounter + " objects at task: " + index);
		sendCount();
		System.out.println("Ending Task" + index);
	}
	
	public synchronized void sendCount()
	{
		mainTask.receiveCount(index, objectCounter);
	}
	
	public synchronized void receiveRange(BufferedImage finalImage)
	{
		this.image = finalImage;
		this.lock.release();
	}
	
	public synchronized void receiveCount(int index, int objectCount)
	{
		objectCounter += objectCount;
		taskCounter++;
	}
	
	public synchronized void sendImageRange(int index, BufferedImage image)
	{
		childTask[index].receiveRange(image);
	}
	
	public String toString()
	{
		return "\nTask_" + index ;
	}

	private void taskAcquire(){
		try{
			this.lock.acquire();
		}
		catch (InterruptedException e) {
			System.out.println(String.format("Fatal error on task id: %d", this.index));
		}
	}

}