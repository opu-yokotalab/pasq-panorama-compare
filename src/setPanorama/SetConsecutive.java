package setPanorama;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import operate.PanoramaPair;
import panoramaCompare.ComparePanorama;
import panoramaCompare.SurmiseRelationalDirection;

/**
 * ���I�ȑ��Έʒu���ʐ�����s���N���X
 * @author hamano
 *
 */
public class SetConsecutive {
	//for debug
	private boolean debug = false;
	private String savePath;
	private String[] fileArray;
	//for debug
	
	
	private static double Dist = 300;  //�B�e�Ԋu
	private ArrayList<Panorama> panoramaList;
	private Panorama[] panoArray;
	

	
	public SetConsecutive(String[] fileArray , String savePath){
		this.debug=true;
		this.savePath = savePath;
		//initialize(fileArray);
		initialize_space(fileArray);
	}
	
	
	
	public SetConsecutive(String[] fileArray){
		initialize(fileArray);
		//initialize_space(fileArray);
	}
	
	
	
	private void initialize(String[] fileArray){
		this.fileArray = fileArray;
		ArrayList<Panorama> undefinedList = new ArrayList<Panorama>(); //�ʒu��������ȃp�m���}�̃��X�g
		ArrayList<Panorama> definedList = new ArrayList<Panorama>(); //�ʒu�����肳�ꂽ�p�m���}�̃��X�g
		
		
		//�p�m���}�ꗗ�̍쐬
		panoArray = new Panorama[fileArray.length];
		for(int i=0;i<fileArray.length;i++){
			panoArray[i] = new Panorama(fileArray[i]);
			panoArray[i].setID("pano"+i);
			undefinedList.add(panoArray[i]);
		}
		
		//�n�_�ƂȂ�p�m���}�̐ݒ�
		Panorama panoramaA = panoArray[0];
		panoramaA.setLocation(0, 0);
		panoramaA.setPixelNorth(0);
		definedList.add(panoramaA);
		undefinedList.remove(panoramaA);
		
		//�n�_�p�m���}���珇�X�Ƀp�m���}�̑��Έʒu�����肵�Ă���
		for(int i=0;i<panoArray.length-1;i++){
			Panorama panoA = panoArray[i];  //���Έʒu�����肳�ꂽ�p�m���}A
			Panorama panoB = panoArray[i+1]; //���ɑ��Έʒu�����肷��p�m���}B
			ComparePanorama compare = new ComparePanorama(panoA.getFilePath(),panoB.getFilePath());
			
			//// for debug start ////
			//�����֌W�̌��ʂ��摜(�摜���ɉ��F�̏c���Ŏ���)�Ƃ��ĕۑ�
			if(debug){
				saveImage_test(compare.getCompareImage_AtoB(), panoA.getFilePath(), panoB.getFilePath(), savePath);
				//saveImage_test(compare.getCompareImage_BtoA(), panoB.getFilePath(), panoA.getFilePath(), savePath);
			}
			//// for debug end////
			System.out.println("["+panoA.getID()+","+panoB.getID()+"] = "+compare.getValue());
			
			if(compare.getValue() <= 20){
				//System.out.println("warning   " + panoA.getID() + " , " + panoB.getID());
				if(compare.getValue() <= 0){
					//System.out.println("error   " + panoA.getID() + "  ,   " + panoB.getID());
				}			
			}
			
			
			
			//�������茋�ʂ̎擾
			SurmiseRelationalDirection surmiseAtoB = compare.getSurmisedDir_A();
			SurmiseRelationalDirection surmiseBtoA = compare.getSurmisedDir_B();
			
			//�p�m���}�Ƀ����N�ǉ�
			PanoramaLink aLink = new PanoramaLink(panoA.getID(),panoB.getID(),surmiseAtoB.getPixelForward_BtoT());
			aLink.setValue(compare.getValue());
			panoA.addPanoLink(aLink);
			PanoramaLink bLink = new PanoramaLink(panoB.getID(),panoA.getID(),surmiseBtoA.getPixelForward_BtoT());
			bLink.setValue(compare.getValue());
			panoB.addPanoLink(bLink);
			
			//panoB�̑��Έʒu�̌v�Z
			int pixelDirAtoB = panoA.dirPixelTo(panoB.getID());  //�p�m���}A�ɂ�����p�m���}B�ւ̕����������ʒu
			double dirAtoB =(360.0 + (double)(pixelDirAtoB - panoA.getPixelNorth())/(double)panoA.getImageWidth()*360.0)%360.0;
			double angleA = (360.0 - dirAtoB + 90.0)%360.0;
			angleA = angleA / 180 * Math.PI; //���W�A���ɕϊ�			
			double xB = panoA.getX() + Dist * Math.cos(angleA);
			double yB = panoA.getY() + Dist * Math.sin(angleA);
			panoB.setLocation(xB, yB);
			
			//panoB�̖k�̈ʒu���v�Z
			int pixelDirBtoA = panoB.dirPixelTo(panoA.getID());
			int pxNorth_B = calculatePixelNorth(xB, yB, panoA.getX(), panoA.getY(), pixelDirBtoA,panoB.getImageWidth()); 
			panoB.setPixelNorth(pxNorth_B);
			definedList.add(panoB);
			undefinedList.remove(panoB);
		}
		
		/*for(int i=0;i<definedList.size();i++){
			System.out.println("["+definedList.get(i).getFileName()+"]");
			for(int j=0;j<definedList.get(i).getPanoLink().size();j++){
				System.out.println(definedList.get(i).getPanoLink().get(j).getBasePanoID()+":"+definedList.get(i).getPanoLink().get(j).getTargetPanoID());
			}
		}*/
		
		panoramaList = definedList;
		
	}
	
	private void initialize_space(String[] fileArray){
		//�֐�initialize()��{���ł݂̂̋�Ԏ����\�z
		//�{�֐�initialize_space()�͋��(�L�ꓙ)�ł������\�z�ł���悤�ɂ�������
		//������ by matsuba
		
		int num_near=10;
		
		this.fileArray = fileArray;
		ArrayList<Panorama> definedList = new ArrayList<Panorama>();
		
		panoArray = new Panorama[fileArray.length];
		for(int i=0;i<fileArray.length;i++){
			panoArray[i] = new Panorama(fileArray[i]);
			panoArray[i].setID("pano"+i);
		}
		
		Panorama panoramaA = panoArray[0];
		panoramaA.setLocation(0, 0);
		panoramaA.setPixelNorth(0);
		//definedList.add(panoramaA);
		
		//�S��r�̑g�ݍ��킹�ŗގ��x�v�Z���s��
		System.out.println("Start calcurate similarity.");
		double[][] sim_matrix = new double[panoArray.length][panoArray.length]; 
		for(int i=0;i<fileArray.length-1;i++){
			for(int j=i+1;j<fileArray.length;j++){
				PanoramaPair pair=new PanoramaPair(fileArray[i],fileArray[j]);
				double value=pair.getSimilarityUsingLuminance();
				System.out.println(fileArray[i]+","+fileArray[j]+":"+value);
				sim_matrix[i][j]=value;
				sim_matrix[j][i]=value;
			}
		}
		System.out.println("Finish calcurate similarity.");
		
		//�v�Z���ꂽ�ގ��x����e�p�m���}�摜�ɂ�����
		//num_near���̋ߗ׃p�m���}�����߂�
		System.out.println("Start set similarity_panorama.");
		for(int i=0;i<panoArray.length;i++){
			ArrayList<nearPanoramaData> nears=new ArrayList<nearPanoramaData>();
			double[] i_value=sim_matrix[i];
			//System.out.println(fileArray[i]+":");
			for(int j=0;j<num_near;j++){
				nearPanoramaData near_data;
				int i_max=0;
				double max=Double.MIN_VALUE;
				for(int k=0;k<i_value.length;k++){
					if(i_value[k]>max){
						max=i_value[k];
						i_max=k;
					}
				}
				near_data=new nearPanoramaData(panoArray[i_max]);
				near_data.similarity=max;
				nears.add(near_data);
				i_value[i_max]=0.0;
				//System.out.println("add:"+fileArray[i_max]);
			}
			panoArray[i].nearData=nears;
		}
		System.out.println("Finish set similarity_panorama.");
		
		//�e�p�m���}�摜�Ƃ��ꂼ��̋ߗ׃p�m���}�ŕ��ʐ�����s��
		//�����������肳�ꂽ�ꍇ�͗ގ��x�������ق���D��
		//���̌�S�Ă̖k�����v�Z
		System.out.println("Start set graph.");
		calDirection(panoArray[0]);
		setAllNorth(panoArray[0]);
		System.out.println("Finish set graph.");
		
		for(int i=0;i<panoArray.length;i++){
			definedList.add(panoArray[i]);
		}
		
		//���X�g�\�� �m�F�p
		printPanoramaList(definedList);
		
		panoramaList = definedList;
	}
	
	private void calDirection(Panorama pano){
		double threshold=20.0;
		boolean exist_sameDirection=true;
		System.out.println("["+pano.getFileName()+"]:");
		//�T������
		pano.inSearch=true;
		//�ߗ׃p�m���}�ƕ��ʐ���
		for(int i=0;i<pano.nearData.size();i++){
			System.out.println(" -> ["+pano.nearData.get(i).panorama.getFileName()+"]:");
			ComparePanorama compare=new ComparePanorama(pano.getFilePath(),pano.nearData.get(i).panorama.getFilePath());
			pano.nearData.get(i).direction=compare.getSurmisedDir_A().getPixelForward_BtoT();
		}
		//���������͂�������
		while(exist_sameDirection){
			ArrayList<nearPanoramaData> removeList=new ArrayList<nearPanoramaData>();
			exist_sameDirection=false;
			for(int i=0;i<pano.nearData.size()-1;i++){
				for(int j=i+1;j<pano.nearData.size();j++){
					double diff;
					double Aangle=360.0*(double)pano.nearData.get(i).direction/(double)pano.getImageWidth();
					double Bangle=360.0*(double)pano.nearData.get(j).direction/(double)pano.getImageWidth();
					if(Math.abs(Aangle-Bangle)>180.0){
						diff=360.0-Math.abs(Aangle-Bangle);
					}
					else{
						diff=Math.abs(Aangle-Bangle);
					}
					if(diff<threshold){
						if(pano.nearData.get(i).similarity>pano.nearData.get(j).similarity){
							removeList.add(pano.nearData.get(j));
						}
						else{
							removeList.add(pano.nearData.get(i));
						}
						exist_sameDirection=true;
					}
				}
			}
			while(!removeList.isEmpty()){
				pano.nearData.remove(removeList.get(0));
				removeList.remove(0);
			}
		}
		//�����N�ǉ�
		for(int i=0;i<pano.nearData.size();i++){
			PanoramaLink link=new PanoramaLink(pano.getID(),pano.nearData.get(i).panorama.getID(),pano.nearData.get(i).direction);
			link.setValue(0.0);
			pano.addPanoLink(link);
		}
		//�אڃp�m���}�Ɉʒu��񂪂Ȃ���Όv�Z
		for(int i=0;i<pano.nearData.size();i++){
			if(pano.nearData.get(i).panorama.getPoint==false){
				int pixelDirAtoB=pano.dirPixelTo(pano.nearData.get(i).panorama.getID());
				double dirAtoB =(360.0 + (double)(pixelDirAtoB - pano.getPixelNorth())/(double)pano.getImageWidth()*360.0)%360.0;
				double angleA = (360.0 - dirAtoB + 90.0)%360.0;
				angleA = angleA / 180 * Math.PI; //���W�A���ɕϊ�
				double xB = pano.getX() + Dist * Math.cos(angleA);
				double yB = pano.getY() + Dist * Math.sin(angleA);
				pano.nearData.get(i).panorama.setLocation(xB, yB);
				pano.nearData.get(i).panorama.getPoint=true;
			}
		}
		//�אڃp�m���}�����T���Ȃ�T��
		for(int i=0;i<pano.nearData.size();i++){
			if(pano.nearData.get(i).panorama.inSearch==false){
				calDirection(pano.nearData.get(i).panorama);
			}
		}
		
	}
	
	private void setAllNorth(Panorama pano){
		for(int i=0;i<pano.nearData.size();i++){
			Panorama panoA=pano.nearData.get(i).panorama;
			if(panoA.getPixelNorth()==-1){
				int pixelDirBtoA=pano.dirPixelTo(panoA.getID());
				int pxNorth_B=calculatePixelNorth(panoA.getX(), panoA.getY(), pano.getX(), pano.getY(), pixelDirBtoA,panoA.getImageWidth());
				pano.nearData.get(i).panorama.setPixelNorth(pxNorth_B);
				System.out.println("Set north:"+pano.nearData.get(i).panorama.getFileName());
				setAllNorth(pano.nearData.get(i).panorama);
			}
		}
	}

	private void printPanoramaList(ArrayList<Panorama> list){
		for(int i=0;i<list.size();i++){
			System.out.println("["+list.get(i).getFileName()+"]");
			for(int j=0;j<list.get(i).getPanoLink().size();j++){
				nearPanoramaData nearPano=list.get(i).nearData.get(j);
				//System.out.println(list.get(i).getPanoLink().get(j).getBasePanoID()+":"+list.get(i).getPanoLink().get(j).getTargetPanoID());
				System.out.println(" -> "+nearPano.panorama.getFileName()+" : "+360.0*(double)nearPano.direction/(double)list.get(i).getImageWidth());
			}
		}
	}
	
	
	
	
	
	/**
	 * 2�̃p�m���}�̍��W��Base->Target���������ƂɁABase�̖k�̈ʒu��
	 * �ǂ��ɂȂ邩���v�Z
	 * @param xB
	 * @param yB
	 * @param xT
	 * @param yT
	 * @param pixelDirBtoT
	 * @param widthBase
	 * @return
	 */
	private int calculatePixelNorth(double xB,double yB,double xT,double yT,int pixelDirBtoT,int widthBase){
		double diff_x = xT - xB;
		double diff_y = yT - yB;
		double theta_rad = Math.atan2(diff_y,diff_x);
		double theta = theta_rad / Math.PI * 180;
		double dirBtoT = (360.0 + 90.0 - theta)%360.0;
		double diffDir = (360.0 - dirBtoT)%360.0; //Base����Target�ւ̕����Ɩk�����̍��������߂�
		double diffRate = diffDir / 360.0;
		int pixelNorth = (widthBase + pixelDirBtoT +  (int)Math.round(diffRate*widthBase))%widthBase;
		return pixelNorth;
	}
	
	
	public ArrayList<Panorama>	getPanoramaList(){
		return panoramaList;
	}
	
	
	
	
	public void setDebug(String savePath){
		this.debug = true;
		this.savePath = savePath;
	}
	
	public void saveImage_test(BufferedImage image,String fileNameA,String fileNameB,String savePlace){
		//�t�@�C���ւ̏����o��
		try{
			////�e�X�g�p�̏���
			String[] temp = fileNameA.split("\\\\");
			String fileA = temp[temp.length-1];
			if(fileA.indexOf(".jpg") != -1){
				temp = fileA.split("\\.");
				fileA = temp[0];
				if(fileA.indexOf("_")!=-1){
					temp = fileA.split("_");
					fileA = temp[1];
				}
			}
			
			temp = fileNameB.split("\\\\");
			String fileB = temp[temp.length-1];
			if(fileB.indexOf(".jpg") != -1){
				temp = fileB.split("\\.");
				fileB = temp[0];
				if(fileB.indexOf("_")!=-1){
					temp = fileB.split("_");
					fileB = temp[1];
				}

			}
			String writeFileName = savePlace + "\\" + fileA + "-" + fileB + ".jpg";
			////
			
			
			boolean result = ImageIO.write(image, "jpg", new File(writeFileName));
			if(!result){
				System.out.println("�t�@�C���ۑ��Ɏ��s");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
