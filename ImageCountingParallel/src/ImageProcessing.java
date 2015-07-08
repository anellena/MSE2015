import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.awt.Point;
import java.util.ArrayList;
import java.awt.Color;

public class ImageProcessing
{
	private BufferedImage initialImage;
	private BufferedImage finalImage;
	
	
   public BufferedImage getFinalImage() {
		return finalImage;
	}

	public void setFinalImage(BufferedImage finalImage) {
		this.finalImage = finalImage;
	}

  static BufferedImage deepCopy(BufferedImage bi) {
    ColorModel cm = bi.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = bi.copyData(null);
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
  }
	
  static boolean IsBorder(Point currentPixel, int width, int height) {
	  if ((currentPixel.x == 0) || (currentPixel.x == (width-1)) || (currentPixel.y == 0) || (currentPixel.y == (height-1))) {
		  return true;
	  }
	  
	  
//	  if ((currentPixel.x == 1024) || (currentPixel.x == 2048) || (currentPixel.y == 768)) {
//		  return true;
//	  }
//	  
	  return false;
  }
  
  static void GetNeighborList(Point currentPixel, BufferedImage image, ArrayList<Point> neighborsList, ArrayList<Point> bordersList) 
  {
	for (int i = -1; i < 2; i++) {
		for (int j = -1; j < 2; j++) {
			if (j == 0 && i == 0){
					continue;
			}
			int x = currentPixel.x + i;
			int y = currentPixel.y + j;
			if ((x < image.getWidth()) && (x >= 0) && (y < image.getHeight()) && (y >= 0) && (image.getRGB(x, y) == 0xFF000000)) {
				Point neighbor = new Point(x,y);
				neighborsList.add(neighbor);
	    	    image.setRGB(x, y, 0xff1493);
    			if (IsBorder (currentPixel, image.getWidth(), image.getHeight())) {
  	    			bordersList.add(currentPixel);
  	    		}
			}
		}
	}
  }	
  
  public int ObjectsCount(BufferedImage image, ArrayList<Point> bordersList)
  {
	  int count=0;
//	  int countMoreThanOneSegment=0;
//	  ArrayList <Point> bordersList;
	  ArrayList <Point> neighborsList;
	 
	  //System.out.println("Image height: " + image.getHeight() + " Image width: " + image.getWidth());
	  
	  for (int y = 0; y < image.getHeight(); y++) {
  	    for (int x = 0; x < image.getWidth(); x++) {
  	    	if(image.getRGB(x, y) == 0xFF000000) {
  	    		count++;
  	    		image.setRGB(x, y, 0xff1493);
  	    		Point currentPixel = new Point(x,y);
  	    		neighborsList = new ArrayList<Point>();
  	    		bordersList = new ArrayList<Point>();
  	    	    GetNeighborList(currentPixel, image, neighborsList, bordersList);
  	    		while (!neighborsList.isEmpty()) {
  	    			//Get always first element to get its neighbors
  	    			currentPixel = neighborsList.get(0);
  	    			neighborsList.remove(0);
  	  	    		GetNeighborList(currentPixel, image, neighborsList, bordersList);
  	    		}
//  	   		if (!bordersList.isEmpty()) {
//  	    			countMoreThanOneSegment++;
//  	    		}
  	    	}
  	    }
  	  }
	  
	  System.out.println("The image has: " + count + "objects.");
	  
	  return count;
  }
  
  public BufferedImage[][] DivideImage(BufferedImage image, int columns, int rows)
  {
	  BufferedImage[][] smallImages = new BufferedImage[columns][rows];
	  int smallWidth = image.getWidth() / columns;
	  int smallHeight = image.getHeight() / rows;
	  
	  for (int y = 0; y < rows; y++) {
		  for (int x = 0; x < columns; x++) {
			  smallImages[x][y] = image.getSubimage(x*smallWidth, y*smallHeight, smallWidth, smallHeight);
			  /*String filename = "image" + x + "" + y + ".png";
			  try {
				  File outputfile = new File(filename);
				  ImageIO.write(smallImages[x][y], "png", outputfile);
			  }
			  catch (IOException e)
			  {
			     // log the exception
			     // re-throw if desired
			  }*/
		  }
	  }
	  
	  return smallImages;
  }

  public void CreateNewImage(int widthStart, int widthEnd, int heightStart, int heightEnd)
  {
    try
    {
      initialImage = ImageIO.read(new File("initialImage.png"));
      finalImage = deepCopy(initialImage);
      
      // TODO - add this to the tasks job
      for (int y = 0; y < initialImage.getHeight(); y++) {
    	    for (int x = 0; x < initialImage.getWidth(); x++) {
    	    	//blue arrow
    	    	if ((y < 768) && (x > 1024)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x, y+678, initialImage.getRGB(x, y));
    	    	} else if ((y < 768) && (x < 2048)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x, y+678, initialImage.getRGB(x, y));
    	    	} else if (y < 768) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x, y+678, initialImage.getRGB(x, y));
    	    	}
    	    	
    	    	//green arrow
    	    	if ((y > 767) && (x < 1024)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x+1024, y-678, initialImage.getRGB(x, y));
    	    	} else if ((y > 767) && (x < 2048)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x+1024, y-678, initialImage.getRGB(x, y));
    	    	}
    	    	
    	    	//red arrow
    	    	if ((x>2047) && (y<768)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x-2048, y, initialImage.getRGB(x, y));
    	    	} else if ((x>2047) && (y>767)) {
    	    		if(initialImage.getRGB(x, y) == 0xFF000000)
    	    	    	finalImage.setRGB(x-2048, y, initialImage.getRGB(x, y));
    	    	}
    	    }
    	}
      
      File outputfile = new File("finalImage.png");
      ImageIO.write(finalImage, "png", outputfile);
     
    } 
    catch (IOException e)
    {
      // log the exception
      // re-throw if desired
    }
  }
 
  public ImageProcessing()
  {
//	imageWidthStart = widthStart;
//	imageWidthEnd = widthEnd;
//	imageHeightStart = heightStart;
//	imageHeightEnd = heightEnd;
  }
  
//  public static void main(String[] args)
//  {
//    new ImageProcessing(0, 3071, 0, 1535);
//  }

}
