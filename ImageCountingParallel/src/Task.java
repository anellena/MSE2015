
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class Task extends Thread
{
	private MainTask mainTask;
	private ChildTask childTask[];
	private int index;
	private int objectCounter = 0;
	private int taskCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;
	private Semaphore workingLock, communicationLock;

	public Task(int index, MainTask mainTask, ChildTask childTask[])
	{
		this.mainTask = mainTask;
		this.childTask = childTask;
		this.index = index;
		this.workingLock = new Semaphore(1);
		this.communicationLock = new Semaphore(1);
		this.taskAcquire(workingLock);
	}
	
	public void run()
	{
		System.out.println("Start Task" + index);
		this.taskAcquire(this.workingLock);
		
		BufferedImage[][] slices = new BufferedImage[1][6];
		imageProcessor = new ImageProcessing();
		slices = imageProcessor.DivideImage(image, 1, 6);
		for(int index = (this.index*6); index < ((this.index*6) + 6); index++) {
			sendImageRange(index, slices[0][this.index]);
			childTask[index].start();
		}
		
		for(int index = (this.index*6); index < ((this.index*6) + 6); index++) {
			try {
				childTask[index].join();
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
		mainTask.receiveCount(index, objectCounter);
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
		childTask[index].receiveRange(image);
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
			System.out.println(String.format("Fatal error on task id: %d", this.index));
		}
	}

}
