
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

public class MainTask extends Thread
{
	private int objectCounter = 0;
	private int taskCounter = 0;
	private ImageProcessing imageProcessor;
	private Semaphore communicationLock;

	public MainTask()
	{
		this.communicationLock = new Semaphore(1);
	}
	
	public void run()
	{
		System.out.println("Start MainTask");

		for(int i = 0; i < TaskHolder.getTaskSize(); i++){
			TaskHolder.getTaskByIndex(i).start();
		}
		
		BufferedImage[][] smallImages = new BufferedImage[3][2];
		
		imageProcessor = new ImageProcessing();
		imageProcessor.CreateNewImage(0, 3071, 0, 1535);
		smallImages = imageProcessor.DivideImage(imageProcessor.getFinalImage(), 3, 2);
		for(int i = 0; i < TaskHolder.getTaskSize(); i++) {
			if (i == 1) {
				sendImageRange(i, smallImages[0][1]);
			} else if (i == 3) {
				sendImageRange(i, smallImages[1][1]);
			} else if (i == 5) {
				sendImageRange(i, smallImages[2][1]);
			} else if (i == 0) {
				sendImageRange(i, smallImages[0][0]);
			} else if (i == 2) {
				sendImageRange(i, smallImages[1][0]);
			} else if (i == 4) {
				sendImageRange(i, smallImages[2][0]);
			}
		}
		
		try
		{ 
			System.out.println("Waiting for Tasks!");
			for(int i = 0; i < TaskHolder.getTaskSize(); i++){
				//System.out.println(String.format("Waiting for Task %d!", i));
				TaskHolder.getTaskByIndex(i).join();
				//System.out.println(String.format("Task %d finished!", i));
			}
		}	
		catch(InterruptedException e) 
		{ 
			notifyAll(); 
		}
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
		TaskHolder.getTaskByIndex(index).receiveRange(image);
	}
	
	public String toString()
	{
		return "\nMain Task";
	}
}
