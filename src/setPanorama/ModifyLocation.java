package setPanorama;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import panoramaCompare.ComparePanorama;
import panoramaCompare.SurmiseRelationalDirection;

import attachNeighbor.PCDConverter;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;


/**
 * �S�m��ɂ����āA�����֌W����̌��ʂ����܂�ɂ��������߁A
 * ���}���u�I�ɍ쐬�����N���X
 * @author hamano
 *
 */
public class ModifyLocation {
	
	Map<String, Element> panoID;
	Document pcd;
	
	ModifyLocation(Document doc){
		pcd = doc;
		initialize();
	}
	ModifyLocation(String filePath){
		pcd = readXML(filePath);
		initialize();
	}
	
	
	public static double Dist =  300;
	
	
	private void initialize(){
		Element root = pcd.getDocumentElement();
		NodeList panoramaList = root.getElementsByTagName("Panorama");
		
		//panoid�ƃp�m���}�v�f�̃e�[�u�����쐬
		panoID = new HashMap<String, Element>();
		for(int i=0;i<panoramaList.getLength();i++){
			Element panoramaE = (Element) panoramaList.item(i);
			String id = panoramaE.getAttribute("panoid");
			panoID.put(id,panoramaE);
		}
	}
	
	

	
	public void modifyDir(String[] panoid, int[] pixelToNextPano_input,int[] pixelToBackPano_input){
		Element root = pcd.getDocumentElement();
		NodeList panoramaList = root.getElementsByTagName("Panorama");
		
		
		int[] imageWidth = new int[panoramaList.getLength()];
		for(int i=0;i<panoramaList.getLength();i++){
			Element panoE = (Element) panoramaList.item(i);
			//////���ʏ��̎擾
			Element imgE =  (Element) panoE.getElementsByTagName("img").item(0);
			imageWidth[i] = Integer.valueOf(imgE.getAttribute("width"));
		}
		
		//�����֌W���ʂ����W���琄��
		double[] dist = new double[panoramaList.getLength()-1];
		int[] pxToNextPano = new int[panoramaList.getLength()-1];
		int[] pxToBackPano = new int[panoramaList.getLength()-1];
		for(int i=0;i<panoramaList.getLength()-1;i++){
			//�O��̃p�m���}(back,next)�̏��擾
			////�O�̃p�m���}(back)�̏��擾
			//////���W�擾
			Element panoE_back = (Element) panoramaList.item(i);
			Element coordsE_back = (Element) panoE_back.getElementsByTagName("coords").item(0);
			double x_back = Double.valueOf(coordsE_back.getAttribute("lng"));
			double y_back = Double.valueOf(coordsE_back.getAttribute("lat"));
			
			//////���ʏ��̎擾
			Element directionE_back = (Element) panoE_back.getElementsByTagName("direction").item(0);
			Element imgE_back =  (Element) panoE_back.getElementsByTagName("img").item(0);
			int pxNorth_back = Integer.valueOf(directionE_back.getAttribute("north"));
			int imageWidth_back = Integer.valueOf(imgE_back.getAttribute("width"));
			
			////��̃p�m���}(next)�̏��擾
			//////���W�擾
			Element panoE_next = (Element) panoramaList.item(i+1);
			Element coordsE_next = (Element) panoE_next.getElementsByTagName("coords").item(0);
			double x_next = Double.valueOf(coordsE_next.getAttribute("lng"));
			double y_next = Double.valueOf(coordsE_next.getAttribute("lat"));
			//////���ʏ��̎擾
			Element directionE_next = (Element) panoE_next.getElementsByTagName("direction").item(0);
			Element imgE_next =  (Element) panoE_next.getElementsByTagName("img").item(0);
			int pxNorth_next = Integer.valueOf(directionE_next.getAttribute("north"));
			int imageWidth_next = Integer.valueOf(imgE_next.getAttribute("width"));
			
			
			//�O��p�m���}�̋������v�Z
			double dist_x = x_next - x_back;
			double dist_y = y_next - y_back;
			dist[i] = Math.sqrt( dist_x*dist_x + dist_y*dist_y  );
			
			//�����֌W�̐���
			double dirBackToNext = LocationMath.direction(x_back, y_back, x_next, y_next);
			double temp = dirBackToNext/360.0*imageWidth_back;
			pxToNextPano[i] = (int) (( imageWidth_back + temp + pxNorth_back)%imageWidth_back);

			double dirNextToBack = LocationMath.direction(x_next, y_next, x_back, y_back);
			temp = dirNextToBack / 360.0 * imageWidth_next;
			pxToBackPano[i] =  (int) ((imageWidth_next + temp + pxNorth_next)%imageWidth_next);
		}
		
		
		
		
		//panoid�𐔒l�ɕϊ�(�^������panoid���B�e�����Ɋ�Â��A�Ԃł��邱�ƁApanoid��pano+�����ł��邱�Ƃ�z��)
		Element tempE = (Element) panoramaList.item(0);
		int numStart = Integer.valueOf(tempE.getAttribute("panoid").substring(4));
		//�����֌W���ʂ̏C��(pxToNextPano,pxToBackPano��u��������)
		for(int i=0;i<panoid.length;i++){
			int num = Integer.valueOf(panoid[i].substring(4));
			pxToBackPano[num-numStart] = pixelToBackPano_input[i];
			pxToNextPano[num-numStart] = pixelToNextPano_input[i];
		}
		
		
		
		//�����֌W����ɁA�ʒu�𐄒�
		Point2D[] modifiedLocation = new Point2D.Double[panoramaList.getLength()];
		int[] modifiedPxNorth = new int[panoramaList.getLength()];
		////�ŏ��̃p�m���}�̈ʒu�E����
		Element panoE = (Element) panoramaList.item(0);
		Element coordsE = (Element) panoE.getElementsByTagName("coords").item(0);
		double x = Double.valueOf(coordsE.getAttribute("lng"));
		double y = Double.valueOf(coordsE.getAttribute("lat"));
		Element directionE = (Element) panoE.getElementsByTagName("direction").item(0);
		modifiedLocation[0] = new Point2D.Double(x,y);
		modifiedPxNorth[0] = Integer.valueOf(directionE.getAttribute("north"));
		
		////�n�_�p�m���}���珇�X�Ƀp�m���}�̑��Έʒu�����肵�Ă���
		for(int i=1;i<panoramaList.getLength();i++){
			//back�̃p�m���}�̏��擾
			double x_back = modifiedLocation[i-1].getX();
			double y_back = modifiedLocation[i-1].getY();
			int pxNorth_back = modifiedPxNorth[i-1];

			
			//next�̑��Έʒu���v�Z
			double dirBackToNext = (360.0 + (double)(pxToNextPano[i-1] - pxNorth_back)/(double)imageWidth[i-1]*360.0)%360.0;
			double angleBackToNext =  (360.0 - dirBackToNext + 90.0)%360.0;
			double angleBackToNext_rad = angleBackToNext / 180.0 * Math.PI;
			x = x_back + dist[i-1] * Math.cos(angleBackToNext_rad);
			y = y_back + dist[i-1] * Math.sin(angleBackToNext_rad);
			modifiedLocation[i] = new Point2D.Double(x,y);
			
			//next�̕��ʏ����v�Z
			modifiedPxNorth[i] = calculatePixelNorth(x, y, x_back, y_back, pxToBackPano[i-1],imageWidth[i]); 
		}
		
		
		//XML��Document�X�V
		for(int i=0;i<panoramaList.getLength();i++){
			panoE = (Element) panoramaList.item(i);
			coordsE = (Element) panoE.getElementsByTagName("coords").item(0);
			directionE = (Element) panoE.getElementsByTagName("direction").item(0);
			
			//Document���X�V
			////�C���O��lat,lng�������폜
			coordsE.removeAttribute("lat");
			coordsE.removeAttribute("lng");
			////�C�����lat,lng������ǉ�
			coordsE.setAttribute("lat", Double.toString(modifiedLocation[i].getY()));
			coordsE.setAttribute("lng", Double.toString(modifiedLocation[i].getX()));
			////�C���O��north���폜
			directionE.removeAttribute("north");
			////�C�����north��ǉ�
			directionE.setAttribute("north", Integer.toString(modifiedPxNorth[i]));
		}
	}
	
	
	

	
	
	
	public Document getDoc(){
		return pcd;
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
	
	private Document readXML(String filePath){
		try {
			// �h�L�������g�r���_�[�t�@�N�g���𐶐�
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			// �h�L�������g�r���_�[�𐶐�
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			// �p�[�X�����s����Document�I�u�W�F�N�g���擾
			Document xmlDoc = builder.parse(new File(filePath));
			return xmlDoc;
		}catch (Exception e) {
			return null;
		}
	}
	
	
	public void writeXML(String filePath){
		try {
			TransformerFactory  tfactory = TransformerFactory.newInstance();
			Transformer tf = tfactory.newTransformer();
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
			File outputFile = new File(filePath);
			tf.transform(new DOMSource(pcd), new StreamResult(outputFile));
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args){
		if(args.length < 4){
			System.out.println("����������������܂���");
			return;
		}
		
		if( (args.length-2)%3 == 0){
			String readFile = args[0];
			String writeFile = args[1];
			String[] idArray = new String[ (args.length-2)/3 ];
			int pixelToNextPano[] = new int[(args.length-2)/3 ];
			int pixelToBackPano[] = new int[(args.length-2)/3];
			
			for(int i=0;i<(args.length-2)/3;i++){
				idArray[i] = args[2+i*3];
				pixelToNextPano[i] = Integer.valueOf(args[i*3+3]);
				pixelToBackPano[i] = Integer.valueOf(args[i*3+4]);
			}
			
			ModifyLocation temp = new ModifyLocation(readFile);
			temp.modifyDir(idArray, pixelToNextPano,pixelToBackPano);
			temp.writeXML(writeFile);

			
			
//			mapping.writeXML(writeFile);
		}
		else{
			System.out.println("����������������܂���");
		}
	}
	
	
}
