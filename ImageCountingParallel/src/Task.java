
import java.awt.image.BufferedImage;
import java.awt.*;

public class Task extends Thread
{
	private MainTask mainTask;
	private ChildTask childTask[] = new ChildTask[6];;
	private int index;
	private int taskCounter = 0;
	private boolean startImageComputation = false;
	private int objectCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;

	public Task(int index, MainTask mainTask)
	{
		this.mainTask = mainTask;
		this.index = index;
	}
	
	public void run()
	{
		System.out.println("Start Task" + index);
		Thread.yield();
		while(!startImageComputation);
		
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
		Thread.yield();
	}
	
	public synchronized void sendCount()
	{
		mainTask.receiveCount(index, objectCounter);
	}
	
	public synchronized void receiveRange(BufferedImage finalImage)
	{
		this.startImageComputation = true;
		this.image = finalImage;
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
}
