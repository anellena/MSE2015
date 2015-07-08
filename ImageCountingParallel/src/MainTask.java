
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class MainTask extends Thread
{
	private SystemT systemT;
	private Task task[];
	private int objectCounter = 0;
	private int taskCounter = 0;
	private ImageProcessing imageProcessor;
	private Semaphore communicationLock;

	public MainTask(SystemT systemT, Task task[])
	{
		this.systemT = systemT;
		this.task = task;
		this.communicationLock = new Semaphore(1);
	}
	
	public void run()
	{
		System.out.println("Start MainTask");
		
		BufferedImage[][] smallImages = new BufferedImage[3][2];
		
		imageProcessor = new ImageProcessing();
		imageProcessor.CreateNewImage(0, 3071, 0, 1535);
		smallImages = imageProcessor.DivideImage(imageProcessor.getFinalImage(), 3, 2);
		for(int index = 0; index < task.length; index++) {
			// TODO - IMPROVE
			if (index == 0) {
				sendImageRange(index, smallImages[0][1]);
			} if (index == 1) {
				sendImageRange(index, smallImages[1][1]);
			} if (index == 2) {
				sendImageRange(index, smallImages[2][1]);
			} if (index == 3) {
				sendImageRange(index, smallImages[0][0]);
			} if (index == 4) {
				sendImageRange(index, smallImages[1][0]);
			} if (index == 5) {
				sendImageRange(index, smallImages[2][0]);
			}
		}
		
		try
		{ 
			System.out.println("Waiting for children!");
			for(int i = 0; i < task.length; i++){
				System.out.println(String.format("Waiting for child %d!", i));
				task[i].join();
				System.out.println(String.format("Child %d finished!", i));
			}
		}	
		catch(InterruptedException e) 
		{ 
			notifyAll(); 
		}
		System.out.println(systemT);
		System.out.println("The image has: " + objectCounter + " objects.");
		System.out.println("Ending MainTask");
	}
	
	public synchronized void receiveCount(int index, int objectCount)
	{
		try{
			this.communicationLock.acquire();
		}
		catch (InterruptedException e){
			System.out.println(String.format("Fatal error: main task interrupted!"));
		}
		objectCounter += objectCount;
		taskCounter++;
		this.communicationLock.release();
	}
	
	public synchronized void sendImageRange(int index, BufferedImage image)
	{
		task[index].receiveRange(image);
	}
	
	public String toString()
	{
		return "\nMain Task";
	}
}
