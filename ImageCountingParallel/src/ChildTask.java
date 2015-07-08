import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.Semaphore;

public class ChildTask extends Thread 
{
	private Task motherTask;
	private int motherTaskIndex;
	private int index;
	private int objectCounter = 0;
	private Semaphore workingLock;
	private BufferedImage image;

	public ChildTask(int index, Task motherTask, int motherIndex)
	{
		this.motherTask = motherTask;
		this.motherTaskIndex = motherIndex;
		this.index = index;
		this.workingLock = new Semaphore(1);
		this.childAcquire();
	}
	
	public void run()
	{
		System.out.println("Start ChildTask" + index);
		this.childAcquire();
		
		//objectCounter = imageProcessor.ObjectsCount(image);
		String filename = "image" + index + "" + motherTaskIndex + ".png";
		try {
			  File outputfile = new File(filename);
			  ImageIO.write(image, "png", outputfile);
		  }
		  catch (IOException e)
		  {
		     // log the exception
		     // re-throw if desired
		  }
		
		System.out.println("Counted " + objectCounter + " objects at task: " + index);
		sendCount();
		System.out.println("Ending ChildTask" + index);
	}
	

	public synchronized void sendCount()
	{
		motherTask.receiveCount(index, objectCounter);
	}
	
	public synchronized void receiveRange(BufferedImage finalImage)
	{
		this.image = finalImage;
		this.workingLock.release();
	}
	private void childAcquire(){
		try{
			this.workingLock.acquire();
		}
		catch (InterruptedException e) {
			System.out.println(String.format("Fatal error on child task id: %d", this.index));
		}
	}
	
}
