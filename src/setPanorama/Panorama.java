package setPanorama;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Panorama {

	private String fileName;
	private String filePath;
	private int aboutX,aboutY;
	private int pixelNorth;
	private String panoID;
	private int imageWidth,imageHeight;
	
	private double x,y;
	
	public ArrayList<nearPanoramaData> nearData;
	public boolean inSearch;		//�T������
	public boolean getPoint;		//�ʒu���v�Z�ς݂�
	
	
	private ArrayList<PanoramaLink> panoLinkList = new ArrayList<PanoramaLink>();
	
	
	Panorama(String path){
		File file = new File(path);
		this.filePath = path;
		this.fileName = file.getName();
		try {
			BufferedImage temp = ImageIO.read(new File(path));
			this.imageWidth = temp.getWidth();
			this.imageHeight = temp.getHeight();
			this.pixelNorth=-1;
		} catch (IOException e) {
			System.out.println("�w��̃p�m���}�摜�͂���܂���  :  " + path );
			//e.printStackTrace();
		}
	}
	
	
	public void setLocation(double x,double y){
		this.aboutX = (int)x;
		this.aboutY = (int)y;
		this.x = x;
		this.y = y;
	}
	
	public int getAboutX(){
		return this.aboutX;
	}
	public int getAboutY(){
		return this.aboutY;
	}
	
	public double getX(){
		return this.x;
	}
	public double getY(){
		return this.y;
	}
	
	
	public void setID(String id){
		this.panoID = id;
	}
	public String getID(){
		return this.panoID;
	}
	
	public void setPixelNorth(int pixel){
		this.pixelNorth = pixel;
	}
	public int getPixelNorth(){
		return this.pixelNorth;
	}
	
	public void setImageInfo(int width,int height){
		this.imageWidth = width;
		this.imageHeight = height;
	}
	public int getImageWidth(){
		return this.imageWidth;
	}
	public int getImageHeight(){
		return this.imageHeight;
	}
	
	public void addPanoLink(PanoramaLink panoLink){
		panoLinkList.add(panoLink);
	}
	public ArrayList<PanoramaLink> getPanoLink(){
		return this.panoLinkList;
	}
	public String getFileName(){
		return this.fileName;
	}
	public String getFilePath(){
		return this.filePath;
	}
	

	
	/**
	 * �p�m���}�摜���ɂ�����A�w�肵���p�m���}ID�ւ̕������s�N�Z���l�Ŏ擾����
	 * @param panoID
	 * @return
	 */
	public Integer dirPixelTo(String panoID){
		for(int i=0;i<panoLinkList.size();i++){
			PanoramaLink panoLink = panoLinkList.get(i);
			if(panoLink.getTargetPanoID().equals(panoID)){
				return panoLink.getPixelDirToTarget();
			}
		}
		
		return null;
	}
	
	/**
	 * �w���panoID�ւ̕��ʂ����߂�
	 * @param panoID
	 * @return
	 */
	public double directionTo(String panoID){
		//�w��p�m���}�ւ̃����N��T������
		PanoramaLink linkToTarget = null;
		for(int i=0;i<panoLinkList.size();i++){
			PanoramaLink temp = panoLinkList.get(i);
			if(temp.getTargetPanoID().equals(panoID)){
				linkToTarget = temp;
			}
		}
		if(linkToTarget == null) return Double.NaN;
		
		
		//�p�m���}�摜�ɂ�����w��p�m���}�ւ̈ʒu(�s�N�Z��)���擾����
		int pixelToTarget = linkToTarget.getPixelDirToTarget();
		int pixelNorth = this.pixelNorth;
		
		//�w��p�m���}�ւ̕��ʂ����߂�
		int pixelDiff = pixelToTarget - pixelNorth;
		double dir =(double)pixelDiff / (double)imageWidth * 360.0;
		dir = (360.0 + dir)%360.0;
		
		return dir;
	}
	
	
	
}
