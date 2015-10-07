package operate;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PanoramaPair {
	
	public BufferedImage panorama1;
	public BufferedImage panorama2;
	
	public PanoramaPair(String pano1,String pano2){
		try{
			panorama1=ImageIO.read(new File(pano1));
			panorama2=ImageIO.read(new File(pano2));
			setPanoramaSize();
		}catch(IOException e){
			System.out.println("file error.\n");
			System.exit(0);
		}
	}
	
	public PanoramaPair(BufferedImage pano1,BufferedImage pano2){
		panorama1=pano1;
		panorama2=pano2;
		setPanoramaSize();
	}
	
	private void setPanoramaSize(){
		if(panorama1.getWidth()!=panorama2.getWidth()||panorama1.getHeight()!=panorama2.getHeight()){
			int baseWidth=panorama1.getWidth();
			int baseHeight=panorama1.getHeight();
			int stateWidth;		//-1:1>2,0:1=2:1=1<2
			int stateHeight;	//-1:1>2,0:1=2:1=1<2
			
			if(panorama1.getWidth()>panorama2.getWidth()){
				baseWidth=panorama2.getWidth();
				stateWidth=-1;
			}
			else if(panorama1.getWidth()<panorama2.getWidth()){
				baseWidth=panorama1.getWidth();
				stateWidth=1;
			}
			else{
				stateWidth=-0;
			}
			if(panorama1.getHeight()>panorama2.getHeight()){
				baseHeight=panorama2.getHeight();
				stateHeight=-1;
			}
			else if(panorama1.getHeight()<panorama2.getHeight()){
				baseHeight=panorama1.getHeight();
				stateHeight=1;
			}
			else{
				stateHeight=0;
			}
			
			if(stateWidth==-1){
				panorama1=Resize(panorama1,baseWidth,baseHeight);
				if(stateHeight==1){
					panorama2=Resize(panorama2,baseWidth,baseHeight);
				}
			}
			else if(stateWidth==1){
				panorama2=Resize(panorama2,baseWidth,baseHeight);
				if(stateHeight==-1){
					panorama1=Resize(panorama1,baseWidth,baseHeight);
				}
			}
			else{
				if(stateHeight==-1){
					panorama1=Resize(panorama1,baseWidth,baseHeight);
				}
				else{
					panorama2=Resize(panorama2,baseWidth,baseHeight);
				}
			}
		}
	}
	
	private BufferedImage Resize(BufferedImage image,int width,int height){
		BufferedImage sizingImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				sizingImage.setRGB(i,j,image.getRGB((int)((double)image.getWidth()/(double)width*i),(int)((double)image.getHeight()/(double)height*j)));
			}
		}
		return sizingImage;
	}
	
	public double getSimilarityUsingLuminance(){
		double value=0.0;
		for(int i=0;i<panorama1.getWidth();i++){
			for(int j=0;j<panorama1.getHeight();j++){
				int rgb1=panorama1.getRGB(i, j);
				int rgb2=panorama2.getRGB(i, j);
				double lum1=0.298912 * (rgb1/256/256) + 0.586611 * ((rgb1/256)%256) + 0.114478 * rgb1%256;
				double lum2=0.298912 * (rgb2/256/256) + 0.586611 * ((rgb2/256)%256) + 0.114478 * rgb2%256;
				value+=(lum1-lum2)*(lum1-lum2)/(panorama1.getWidth()*panorama1.getHeight());
			}
		}
		return 1.0/(1.0+Math.sqrt(value));
	}

}
