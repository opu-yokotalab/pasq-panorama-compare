package attachNeighbor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import org.w3c.dom.Element;

import sun.awt.SunToolkit.InfiniteLoop;

/**
 * Panorama�v�f���I�u�W�F�N�g�ŕ\��������
 * changePanoList��chpano��\��
 * @author hamano
 *
 */
public class Panorama {
	public static double dist_threshold = 15.0;
	
	public String panoID;
	private String src;
	private int imageHeight;
	private int imageWidth;
	private double lat;
	private double lng;
	private int pixelNorth;
	public double pixelNorthFix;
	
	public ArrayList<Chpano> changePanoList;  //�ߖT�p�m���}���X�g
	
	
	Panorama(){
		init();
	}
	Panorama(Element element){
		init();
		set(element);
	}
	
	
	public void init(){
		panoID = null;
		src = null;
		lat = Double.NaN;
		lng = Double.NaN;
		pixelNorth = -1;
		imageWidth = -1;
		imageHeight = -1;
		changePanoList = new ArrayList<Chpano>();
	}
	
	/**
	 * �v���p�e�B��ݒ�
	 * @param panoramaElement
	 */
	public void set(Element panoramaElement){
		panoID = panoramaElement.getAttribute("panoid");
		
		Element imgElement = (Element)panoramaElement.getElementsByTagName("img").item(0);
		src = imgElement.getAttribute("src");
		imageWidth = Integer.parseInt(imgElement.getAttribute("width"));
		imageHeight = Integer.parseInt(imgElement.getAttribute("height"));
		
		Element coordsElement = (Element) panoramaElement.getElementsByTagName("coords").item(0);
		lat = Double.parseDouble(coordsElement.getAttribute("lat"));		
		lng = Double.parseDouble(coordsElement.getAttribute("lng"));
		
		Element directionElement = (Element) panoramaElement.getElementsByTagName("direction").item(0);
		pixelNorth = Integer.parseInt(directionElement.getAttribute("north"));
	}
	
	
	
	
	
	/*
	 * �ߖT�p�m���}�̑I��
	 * �I�����ꂽ�ߖT�p�m���}�����ɐ͈ؑ֔͂����蓖�Ă�
	 * 
	 * 
	 */
	
	
	
	/**
	 * �ؑւ̐ݒ�
	 * @param list
	 */
	public void setChangePano(ArrayList<Panorama> list){
		//�ߖT�p�m���}�̑I��
		Chpano allTarget[] = new Chpano[list.size()];
		ArrayList<Chpano> allTargetList = new ArrayList<Chpano>();
		
		
		//�����Ń\�[�g�ł���悤�ɂ���
		ListIterator iterator = list.listIterator();
		for(int i=0;i<list.size();i++){
			Panorama pano = list.get(i);
			double latTarget = pano.getLat();
			double lngTarget = pano.getLng();
			
			//360�x�@���烉�W�A���ɕϊ�
			double latrad = this.lat * Math.PI / 180;
			double lngrad = this.lng * Math.PI / 180;
			double latTargetrad = latTarget * Math.PI / 180;
			double lngTargetrad = lngTarget * Math.PI / 180;
			
			
			double dx = 6378137 * (lngTargetrad - lngrad) * Math.cos(latrad);
			double dy = 6378137 * (latTargetrad - latrad);
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			double dir = Math.atan2(dx,dy);
			dir = dir * 180 / Math.PI; //���W�A������360�x�@��
			dir = (360+dir)%360; //0-360�x��
			
			// id�A�����A���ʂ����X�g��
			Chpano target = new Chpano(this,pano,dist,dir);
			allTarget[i] = target;
		}
		TheComparator comparator = new TheComparator();
		Arrays.sort(allTarget,comparator);
        
		
		//�͈ؑ֔͂̊��蓖��
		attachChangeInfo(allTarget);
		
	}
	
	
	//�ߖT�p�m���}�̑I��
	public void chooseNeighbor(Chpano allTarget[]){
	}
	//�ߖT�p�m���}����ɁA�͈ؑ֔͂����蓖�Ă�
	public void allotChangeDir(){
	}
	
	
	/**
	 * convertPCD �̈ꕔ�R�s�[
	 * @param allTarget
	 */
	public void attachChangeInfo(Chpano allTarget[]){
		Chpano arLink[] = new Chpano[360];
    	for(int j = 1; j<allTarget.length; j++){ // ���݂̃p�m���}�摜���܂ނ��� j=1 ����
    		//������������l�ȓ��ł���΁A�ߖT�p�m���}�Ƃ��Đؑ֗L�ɂ���
    		double distToTarget = allTarget[j].getDist();
    		if(distToTarget < dist_threshold){
    			if((arLink[(int)Math.floor(allTarget[j].getDir())] == null)){
        			
        			// �͈ؑ֔͂̊����v�Z(�v�ύX)
    	    		double tmp = Math.pow(0.95, allTarget[j].getDist()) * 100 + 20;
    	    		int offd = (int)Math.floor(tmp / 2);
    	    		
    	    		int sd = (int)Math.floor((360 + allTarget[j].getDir() - offd) % 360);
    	    		int ed = (int)Math.floor((360 + allTarget[j].getDir() + offd) % 360);
    	    		if(sd > ed){ // 0�x���܂����Ƃ�
    	    			for(int k=sd; k<=359; k++){
    	    				if(arLink[k] == null) arLink[k] = allTarget[j];
    	    			}
    	    			for(int k=0; k<=ed; k++){
    	    				if(arLink[k] == null) arLink[k] = allTarget[j];
    	    			}
    	    		}else{
    	    			for(int k=sd; k<=ed; k++){
    	    				if(arLink[k] == null) arLink[k] = allTarget[j];
    	    			}
    	    		}
        		}
    		}
    		
    	}
    	
    	
    	// �����ʒu
    	int startDir = 0;
    	int k=0;
    	
		// �͈ؑ֔͂�0�����܂����ꍇ���� for���̊O�ŏ���
		if((arLink[0] != null) && (arLink[0].equals(arLink[359]))){
			// �����ʒu����ѐ͈ؑ֔͂̊J�n�ʒu
			startDir = 359;
			while(arLink[startDir].equals(arLink[startDir-1])){
				startDir--;
			}
			
			// �͈ؑ֔͂̏I���ʒu
			k = startDir;
			while((arLink[(k+1)%360] != null) && (arLink[k].equals(arLink[(k+1)%360]))){
				k = (k + 1) % 360;
			}
			
			arLink[k].setChangeDir(startDir, k);
			changePanoList.add(arLink[k]);
			k = (k+1)%360;
			
		}
		
		// ���̑��͈̐ؑ֔͂̏���
		do{
			if(arLink[k] != null){
				int tmp = k;
				while(arLink[k].equals(arLink[(k+1)%360])){
					k = (k + 1) % 360;
				}
				
				// �͈ؑ֔͂̐ݒ� (tmp -> k)
				arLink[k].setChangeDir(tmp, k);
				changePanoList.add(arLink[k]);
			}
			k = (k + 1) % 360;
		}while(k != startDir);
		
	}
	
	
	public double getFixedNorth(){
		return this.pixelNorthFix;
	}
	
	public ArrayList<Chpano> getChpano(){
		return this.changePanoList;
	}
	
	public String getPanoID(){
		return this.panoID;
	}
	public String getSrc(){
		return this.src;
	}
	public int getWidth(){
		return this.imageWidth;
	}
	public int getHeight(){
		return this.imageHeight;
	}
	public double getLat(){
		return this.lat;
	}
	public double getLng(){
		return this.lng;
	}
	public int getNorth(){
		return this.pixelNorth;
	}
	
}
