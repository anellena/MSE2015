
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

public class Task extends Thread
{
	private int index;
	private int objectCounter = 0;
	private int taskCounter = 0;
	private BufferedImage image;
	private ImageProcessing imageProcessor;
	private Semaphore workingLock, communicationLock, childrenDone;
	final int HORIZONTAL = 0;
	final int VERTICAL = 0;

	public Task(int index)
	{
		this.index = index;
		this.workingLock = new Semaphore(1);
		this.communicationLock = new Semaphore(1);
		this.childrenDone = new Semaphore(1);
		this.taskAcquire(this.workingLock);
		this.taskAcquire(this.childrenDone);
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
		
		this.childrenDone.release();
	    this.searchForObjectsOnOtherTasks();
		
		System.out.println("Counted " + objectCounter + " objects at task: " + index);
		sendCount();
		System.out.println("Ending Task" + index);
	}
	
	private void searchForObjectsOnOtherTasks(){
		//System.out.println("Starting count: " + this.objectCounter);
		for (int k = (this.index * 6); k < ((this.index * 6) + 6); k++) {
			System.out.println("Starting search: " + k);
			for (int i = 0; i < TaskHolder.getChildTaskByIndex(k).getBorderList().size(); i++){
				ArrayList<Point> currentList = TaskHolder.getChildTaskByIndex(k).getBorderList().get(i); 
				for (int j = 0; j < currentList.size(); j++){
					Point currentPoint = currentList.get(j);
					if (this.index < 5) {
						if (this.index == 0 || this.index == 2 || this.index == 4) {
							if (this.index != 4) {
								if (TaskHolder.getTaskByIndex(this.index + 2).checkBorders(currentPoint.x, currentPoint.y, VERTICAL, k+12) == true){
									this.objectCounter--;
									break;
								}	
							}
							
							if (k == ((this.index * 6) + 5)) {
								if (TaskHolder.getTaskByIndex(this.index + 1).checkBorders(currentPoint.x, currentPoint.y, HORIZONTAL, k+6) == true){
									this.objectCounter--;
									break;
								}
							}
							
						} else {
							if (TaskHolder.getTaskByIndex(this.index + 1).checkBorders(currentPoint.x, currentPoint.y, HORIZONTAL, k+6) == true){
								this.objectCounter--;
								break;
							}
						}	
					}
				}
			}
		}
		System.out.println("Final count: " + this.objectCounter);
	}
	
	private boolean checkBorders(int x, int y, int orientation, int childIndex){
		this.taskAcquire(this.childrenDone);
		for (int i = 0; i < TaskHolder.getChildTaskByIndex(childIndex).getBorderList().size(); i++){
			ArrayList<Point> currentList = TaskHolder.getChildTaskByIndex(childIndex).getBorderList().get(i); 
			for (int j = 0; j < currentList.size(); j++){
				Point currentPoint = currentList.get(j); 
				if (orientation == HORIZONTAL) {
					if(currentPoint.x == x && currentPoint.y == 0){
						this.childrenDone.release();
						return true;
					}
				} else {
					if(currentPoint.x == 0 && currentPoint.y == y){
						this.childrenDone.release();
						return true;
					}
				}
			}
		}
		this.childrenDone.release();
		return false;
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
