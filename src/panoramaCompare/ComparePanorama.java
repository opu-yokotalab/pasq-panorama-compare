package panoramaCompare;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import mySurf.MySurf;
import mySurf.PairInterestPoints;

import com.stromberglabs.jopensurf.SURFInterestPoint;

/**
 * �p�m���}�摜���r���A�����֌W������s��
 * @author hamano
 *
 */
public class ComparePanorama extends JPanel{
	private static boolean upright = true; //�����_���o�Ɋւ��p�����[�^�B�������Ă��Ȃ�
	private static double threshold = 0.95d; //�����_�̑Ή��_�T����臒l
	
	private String filePathA=null,filePathB=null; //�p�m���}�摜�̃p�X
	private BufferedImage imageA,imageB;  //�p�m���}�摜�̃C���[�W�I�u�W�F�N�g
	private MySurf surfA,surfB; //�p�m���}�摜�ɑ΂���SURF�I�u�W�F�N�g
	public SurmiseRelationalDirection surmisedDirA;  //�����֌W����I�u�W�F�N�g
	private SurmiseRelationalDirection surmisedDirB;
	
	private double value;  //�M���x

	
	
	
	/**
	 * �R���X�g���N�^(�C���[�W�I�u�W�F�N�g�ł���BufferedImage�Ŏw�肷��ꍇ)
	 * @param imageA
	 * @param imageB
	 */
	public ComparePanorama(BufferedImage imageA,BufferedImage imageB){
		initialize(imageA,imageB);
	}
	
	/**
	 * �R���X�g���N�^(�p�m���}�摜�̃t�@�C���p�X�Ŏw�肷��ꍇ)
	 * @param fileNameA
	 * @param fileNameB
	 */
	public ComparePanorama(String fileNameA,String fileNameB){
		try {
			File temp = new File(fileNameA);
			filePathA = temp.getAbsolutePath();
			temp = new File(fileNameB);
			filePathB = temp.getAbsolutePath();
			BufferedImage imageA = ImageIO.read(new File(fileNameA));
			BufferedImage imageB = ImageIO.read(new File(fileNameB));
			initialize(imageA,imageB);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ������( �����_�̒��o -> �����_�̑Ή��t�� -> �����֌W���� )
	 * @param imageA
	 * @param imageB
	 */
	private void initialize(BufferedImage imageA,BufferedImage imageB){
		this.imageA = imageA;
		this.imageB = imageB;
		surfA = new MySurf(imageA);
		surfB = new MySurf(imageB);
		
		
		//�����_�f�[�^�̎擾
		List<SURFInterestPoint> pointListA = upright ? surfA.getUprightInterestPoints() : surfA.getFreeOrientedInterestPoints();
		List<SURFInterestPoint> pointListB = upright ? surfB.getUprightInterestPoints() : surfB.getFreeOrientedInterestPoints();
				
		
		//�����_�̑Ή��t��
		ArrayList<PairInterestPoints> pairListA = matchPoints(pointListA, pointListB, threshold);
		ArrayList<PairInterestPoints> pairListB = matchPoints(pointListB, pointListA, threshold);
		
		
		
		this.surmisedDirA = new SurmiseRelationalDirection(pairListA,pairListB,imageA,imageB);
		this.surmisedDirB = new SurmiseRelationalDirection(pairListB,pairListA,imageB,imageA);		
		
		//�����֌W�̌��ʂ��Z�b�g����
		setValue();
	}
	
	
	
	
	
	public SurmiseRelationalDirection getSurmisedDir_A(){
		return this.surmisedDirA;
	}
	public SurmiseRelationalDirection getSurmisedDir_B(){
		return this.surmisedDirB;
	}
	
	
	
	
	/**
	 * �M���x�̐ݒ�
	 */
	private void setValue(){
		double diffDir1 = surmisedDirA.getDirBtoT() - surmisedDirB.getDirTtoB();
		double diffDir2 = surmisedDirB.getDirBtoT() - surmisedDirA.getDirTtoB();
		
		double rate;
		if(Math.abs(diffDir1) > Math.abs(diffDir2)){
			rate = 1.0 - Math.abs(diffDir1) / 360.0;
		}else{
			rate = 1.0 - Math.abs(diffDir2) / 360.0;
		}
		
		
		if( Math.abs(diffDir1) > 30 ||  Math.abs(diffDir2) > 30 ){
			this.value = 0;
			return;
		}
		else{
			//this.value = surmisedDirA.getNumAroundDirPair();
			if(surmisedDirA.getNumFillConditionPair() > surmisedDirB.getNumFillConditionPair()){				
//				this.value = surmisedDirB.getNumFillConditionPair() * rate;
				this.value = surmisedDirB.getNumAroundDirPair() * rate;
			}
			else{
//				this.value = surmisedDirA.getNumFillConditionPair() * rate;
				this.value = surmisedDirA.getNumAroundDirPair() * rate;
			}
		}
	}
	
	/**
	 * �M���x�̎擾
	 * @return
	 */
	public double getValue(){
		return this.value;
	}
	
	
	
    /**
     * �����_�̑Ή��_�T��
     * @param pointsA
     * @param pointsB
     * @param threshold
     * @return
     */
	public ArrayList<PairInterestPoints> matchPoints(List<SURFInterestPoint> pointsA, List<SURFInterestPoint> pointsB , double threshold){
		List<PairInterestPoints> pairPointList = new ArrayList<PairInterestPoints>();
		List<SURFInterestPoint> points = pointsA;
		
		
		for ( SURFInterestPoint a : points ){
			double smallestDistance = Float.MAX_VALUE;
			double nextSmallestDistance = Float.MAX_VALUE;
			SURFInterestPoint possibleMatch = null;
			
			for ( SURFInterestPoint b : pointsB ){
				double distance = a.getDistance(b);
				//System.out.println("Distance = " + distance);
				if ( distance < smallestDistance ){
					nextSmallestDistance = smallestDistance;
					smallestDistance = distance;
					possibleMatch = b;
				} else if ( distance < nextSmallestDistance ){
					nextSmallestDistance = distance;
				}
			}
			
		    // If match has a d1:d2 ratio < 0.65 ipoints are a match
			if ( smallestDistance/nextSmallestDistance < threshold ){
				//not storing change in position
				pairPointList.add(new PairInterestPoints(a,possibleMatch,smallestDistance/nextSmallestDistance));
			}
		}
		return (ArrayList<PairInterestPoints>) pairPointList;
	}
    
	
	
    
    /**
     * �w��̐F�őΉ��t����ꂽ�����_����Ō���
     * @param g
     * @param pairList
     * @param color
     */
    private void drawIPLine(Graphics g,ArrayList<PairInterestPoints> pairList,Color color,int height_base){
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setColor(color);
    	for(PairInterestPoints pair : pairList){
    		SURFInterestPoint objectIPoint = pair.getPointBase();
    		SURFInterestPoint targetIPoint = pair.getPointTarget();
    		int xOIP = (int) (objectIPoint.getX());
    		int yOIP = (int) (objectIPoint.getY());
    		int xTIP = (int) (targetIPoint.getX());
    		int yTIP = (int) (targetIPoint.getY());
    		g2d.drawLine(xOIP, yOIP, xTIP, (int)(yTIP+height_base));
    	}
    }
	

    
    public BufferedImage getCompareImage_AtoB(){
    	//�I�t�Z�b�g�C���[�W�ɕ`��
    	BufferedImage offImage = new BufferedImage(Math.max(imageA.getWidth(),imageB.getWidth()),imageA.getHeight()+imageB.getHeight(),BufferedImage.TYPE_INT_BGR);
    	Graphics2D off = offImage.createGraphics();
    	off.drawImage(imageA,0,0,imageA.getWidth(),imageA.getHeight(),this);
    	off.drawImage(imageB,0,imageA.getHeight(),imageB.getWidth(),imageB.getHeight(),this);
		//�S�Ă̑Ή��t�������_�̕`��
    	drawIPLine(off,surmisedDirA.getAllPairList(),Color.darkGray,imageA.getHeight());
    	//����ɗp�����Ή��t�������_�̕`��
    	drawIPLine(off,surmisedDirA.getPairList(),Color.red,imageA.getHeight());
    	//imageA�ɐ��肳�ꂽA->B�����̕`��
    	off.setColor(Color.yellow);
    	off.fillRect(surmisedDirA.getPixelForward_BtoT()-2, 0, 4, imageA.getHeight());
    	//imageB�ɐ��肳�ꂽB->A�����̕`��
    	off.setColor(Color.yellow);
    	off.fillRect(surmisedDirA.getPixelForward_TtoB()-2, imageA.getHeight(), 4, imageB.getHeight());		
		
    	
    	return offImage;
    }
    
    
    public BufferedImage getCompareImage_BtoA(){
    	//�I�t�Z�b�g�C���[�W�ɕ`��
    	BufferedImage offImage = new BufferedImage(Math.max(imageA.getWidth(),imageB.getWidth()),imageB.getHeight()+imageA.getHeight(),BufferedImage.TYPE_INT_BGR);
    	Graphics2D off = offImage.createGraphics();
    	off.drawImage(imageB,0,0,imageB.getWidth(),imageB.getHeight(),this);
    	off.drawImage(imageA,0,imageB.getHeight(),imageA.getWidth(),imageA.getHeight(),this);
		//�S�Ă̑Ή��t�������_�̕`��
    	drawIPLine(off,surmisedDirB.getAllPairList(),Color.gray,imageB.getHeight());
    	//����ɗp�����Ή��t�������_�̕`��
    	drawIPLine(off,surmisedDirB.getPairList(),Color.red,imageB.getHeight());
    	//imageB�ɐ��肳�ꂽB->A�����̕`��
    	off.setColor(Color.yellow);
    	off.fillRect(surmisedDirB.getPixelForward_BtoT()-2, 0, 4, imageB.getHeight());
    	//imageA�ɐ��肳�ꂽA->B�����̕`��
    	off.setColor(Color.yellow);
    	off.fillRect(surmisedDirB.getPixelForward_TtoB()-2, imageB.getHeight(), 4, imageA.getHeight());		
		
    	
    	return offImage;
    }
	
	
    
    
    
   public static void main(String[] args) throws IOException{
	   if(args.length == 2){
		   ComparePanorama cp = new ComparePanorama(args[0],args[1]);
	   }
	   else if(args.length == 4){
		   ComparePanorama cp = new ComparePanorama(args[0],args[1]);
		   ImageIO.write(cp.getCompareImage_AtoB(), "jpg", new File(args[2]));
		   ImageIO.write(cp.getCompareImage_BtoA(), "jpg", new File(args[3]));
	   }
	   else{
		   System.out.println("����������������܂���");
	   }
	   
   }
    
}
