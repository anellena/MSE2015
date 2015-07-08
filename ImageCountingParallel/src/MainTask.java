
import java.awt.image.BufferedImage;
import java.util.concurrent.Semaphore;

public class MainTask extends Thread
{
	private SystemT systemT;
	private Task task[];
	private int objectCounter = 0;
	private int taskCounter = 0;
	private ImageProcessing imageProcessor;
	private Semaphore lock;

	public MainTask(SystemT systemT, Task task[])
	{
		this.systemT = systemT;
		this.task = task;
		this.lock = new Semaphore(1);
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
		System.out.println("The image has: " + objectCounter + "objects.");
		System.out.println("Ending MainTask");
		Thread.yield();
	}
	
	public synchronized void receiveCount(int index, int objectCount)
	{
		objectCounter += objectCount;
		taskCounter++;
	}
	
	public synchronized void sendImageRange(int index, BufferedImage image)
	{
		// TODO - improve logic for this
		/*if (index == 0) {
			task[index].receiveRange(image, 0, 1023, 0, 767);
		} else if (index == 1) {
			task[index].receiveRange(image, 1024, 2047, 0, 767);
		} else if (index == 2) {
			task[index].receiveRange(image, 2048, 3071, 0, 767);
		} else if (index == 3) {
			task[index].receiveRange(image, 0, 1023, 768, 1535);
		} else if (index == 4) {
			task[index].receiveRange(image, 1024, 2047, 768, 1535);
		} else if (index == 5) {
			task[index].receiveRange(image, 2048, 3071, 768, 1535);
		}*/
		try{
			this.lock.acquire();
		}
		catch (InterruptedException e){
			System.out.println(String.format("Fatal error: main task interrupted!"));
		}
		task[index].receiveRange(image);
		this.lock.release();
	}
	
	public String toString()
	{
		return "\nMain Task";
	}
}
