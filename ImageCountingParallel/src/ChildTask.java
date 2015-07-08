import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.Semaphore;
import java.awt.Point;
import java.util.ArrayList;

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
		
//		String filename = "image" + index + "" + motherTaskIndex + ".png";
//		try {
//			File outputfile = new File(filename);
//			ImageIO.write(image, "png", outputfile);
//		}
//		catch (IOException e){
		     // log the exception
		     // re-throw if desired
//		}
		
		//This if is here for limiting to only, remove
		if (this.index == 0 && this.motherTaskIndex == 0){
			ArrayList<ArrayList<Point>> bordersList = new ArrayList<ArrayList<Point>>();
			objectCounter = ImageProcessing.ObjectsCount(image, bordersList);
			System.out.println("Counted " + objectCounter + " objects at task: " + index + " of mother " + this.motherTaskIndex);
			for (int i = 0; i < bordersList.size(); i++){
				System.out.println("Number of borders: " + bordersList.get(i).size());	
			}
			System.out.println(bordersList);
		}
		
		sendCount();
		//System.out.println("Ending ChildTask" + index);
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
