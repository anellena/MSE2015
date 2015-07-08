
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class Task extends Thread
{
	private MainTask mainTask;
	private ChildTask childTask[] = new ChildTask[6];;
	private int index;
	private int objectCounter = 0;
	private int taskCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;
	private Semaphore workingLock, communicationLock;

	public Task(int index, MainTask mainTask)
	{
		this.mainTask = mainTask;
		this.index = index;
		this.workingLock = new Semaphore(1);
		this.communicationLock = new Semaphore(1);
		this.taskAcquire(workingLock);
	}
	
	public void run()
	{
		System.out.println("Start Task" + index);
		this.taskAcquire(this.workingLock);
		
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
		
		for (int i = 0; i < childTask.length; i++){
			try {
				childTask[i].join();
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
