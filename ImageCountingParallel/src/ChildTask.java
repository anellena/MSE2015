import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.concurrent.Semaphore;
import java.awt.Point;
import java.util.ArrayList;

public class ChildTask extends Thread 
{
	private int motherTaskIndex;
	private int index;
	private int objectCounter = 0;
	private Semaphore workingLock, communicationLock, finishLock;
	private BufferedImage image;
	private ArrayList<ArrayList<Point>> bordersList;

	public ChildTask(int motherIndex, int index)
	{
		this.motherTaskIndex = motherIndex;
		this.index = index;
		this.bordersList = new ArrayList<ArrayList<Point>>();
		this.workingLock = new Semaphore(1);
		this.communicationLock = new Semaphore(1);
		this.finishLock = new Semaphore(1);
		this.childAcquire(this.workingLock);
		this.childAcquire(this.communicationLock);
		this.childAcquire(this.finishLock);
	}
	
	public void run()
	{
		System.out.println("Start ChildTask " + index);
		this.childAcquire(this.workingLock);
		
		this.saveFile();
		//This if is here for limiting to only, remove
		if ((this.index == 0 || this.index == 1) && this.motherTaskIndex == 0){
			this.countChildTaskObjects();
		}
		
		if (this.index == 0 && this.motherTaskIndex == 0){
			this.searchForObjectsOnOtherChildTasks();
		}
		
		sendCount();
		//System.out.println("Ending ChildTask: " + index);
	}
	
	
	public synchronized void sendCount()
	{
		TaskHolder.getTaskByIndex(this.motherTaskIndex).receiveCount(this.index, this.objectCounter);
	}
	
	public synchronized void receiveRange(BufferedImage finalImage)
	{
		this.image = finalImage;
		this.workingLock.release();
	}
	
	private void childAcquire(Semaphore lock){
		try{
			lock.acquire();
		}
		catch (InterruptedException e) {
			System.out.println("Fatal error on child task id: " + this.index);
		}
	}
	
	private void saveFile(){
		String filename = "image" + motherTaskIndex + "" + index + ".png";
		try {
			File outputfile = new File(filename);
			ImageIO.write(image, "png", outputfile);
		}
		catch (IOException e){
			//log the exception
			//re-throw if desired
		}		
	}
	
	private void countChildTaskObjects(){
		objectCounter = ImageProcessing.ObjectsCount(image, this.bordersList);
		System.out.println("Counted " + objectCounter + " objects at task: " + index + " of mother " + this.motherTaskIndex);
		for (int i = 0; i < this.bordersList.size(); i++){
			System.out.println("Number of borders: " + this.bordersList.get(i).size());	
		}
		System.out.println(this.bordersList);
		
		// We are ready to communicate with other threads
		this.communicationLock.release();
	}
	
	private boolean checkBorders(int x){
		this.childAcquire(this.communicationLock);
		for (int i = 0; i < this.bordersList.size(); i++){
			ArrayList<Point> currentList = this.bordersList.get(i); 
			for (int j = 0; j < currentList.size(); j++){
				Point currentPoint = currentList.get(j); 
				if(currentPoint.x == x && currentPoint.y == 0){
					this.communicationLock.release();
					return true;
				}
			}
		}
		this.communicationLock.release();
		return false;
	}
	
	private void searchForObjectsOnOtherChildTasks(){
		System.out.println("Starting count: " + this.objectCounter);
		for (int i = 0; i < this.bordersList.size(); i++){
			ArrayList<Point> currentList = this.bordersList.get(i); 
			for (int j = 0; j < currentList.size(); j++){
				Point currentPoint = currentList.get(j);
				if (TaskHolder.getChildTaskByIndex(this.index + 1).checkBorders(currentPoint.x) == true){
					this.objectCounter--;
					break;
				}
			}
		}
		System.out.println("Final count: " + this.objectCounter);
	}
	
}
