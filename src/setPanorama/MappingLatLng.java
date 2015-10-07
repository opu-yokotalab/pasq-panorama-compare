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

import attachNeighbor.PCDConverter;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

/**
 * ���Έʒu�E���ʂō\�������PasQ��Ԃ��ܓx�o�x�փ}�b�s���O����N���X
 * @author hamano
 *
 */
public class MappingLatLng {
	Map<String, Element> panoID;
	Document pcd;
	
	MappingLatLng(Document doc){
		pcd = doc;
		initialize();
	}
	MappingLatLng(String filePath){
		pcd = readXML(filePath);
		initialize();
	}
	
	
	
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
	
	
	
	
	
	
	
	public void setPointLatLng(String[] idArray , Point2D[] pointArray){
		//�G���[����(idArray��pointArray�̔z�񐔂͈�v���Ă��Ȃ���΂Ȃ�Ȃ�)
		if(pointArray.length != idArray.length) return;
		
		int temp = Integer.valueOf(idArray[0].substring(4));
		Element root = pcd.getDocumentElement();
		NodeList panoramaList = root.getElementsByTagName("Panorama");
		for(int i=0;i<pointArray.length-1;i++){
			//�p�m���}ID�擾
			String panoid1 = idArray[i];
			String panoid2 = idArray[i+1];
			//�p�m���}�̈ܓx�o�x�擾
			Point2D point1 = pointArray[i];
			Point2D point2 = pointArray[i+1];
			
			
			//�p�m���}ID�ɑΉ�����p�m���}��xy���W�̈ʒu�擾
			////panoid1
			Element panoE1 = panoID.get(panoid1);
			Element coords1 = (Element) panoE1.getElementsByTagName("coords").item(0);
			double x_panoid1 = Double.valueOf(coords1.getAttribute("lng"));
			double y_panoid1 =  Double.valueOf(coords1.getAttribute("lat"));
			////panoid2
			Element panoE2 = panoID.get(panoid2);
			Element coords2 = (Element) panoE2.getElementsByTagName("coords").item(0);
			double x_panoid2 = Double.valueOf(coords2.getAttribute("lng"));
			double y_panoid2 =  Double.valueOf(coords2.getAttribute("lat"));
			
			
			double diff_x = x_panoid2 - x_panoid1;
			double diff_y = y_panoid2 - y_panoid1;
			double angle_xy = Math.atan2(diff_y, diff_x);
			double dist_xy = Math.sqrt(diff_x*diff_x + diff_y*diff_y);
			
			double angle_LatLng = Math.atan2( GeoMath.distance_lat(point1.getX(), point1.getY(), point2.getX(), point2.getY()) ,  GeoMath.distance_lng(point1.getX(), point1.getY(), point2.getX(), point2.getY()));	
			double diffAngle_rad = angle_LatLng - angle_xy;
			double dist_LatLng = Math.sqrt( (point1.getX()-point2.getX())*(point1.getX()-point2.getX()) + (point1.getY()-point2.getY())*(point1.getY()-point2.getY())  );			
			double distGeo_LatLng = GeoMath.distance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
			
			double distRate = dist_LatLng / dist_xy;
			double xyRate = distGeo_LatLng / dist_xy; //xy���W�̋������烁�[�g���֕ϊ�����Ƃ��̔䗦(���m�ɂ͈قȂ�)
			
			//
			int numPano1 = Integer.valueOf(panoid1.substring(4));
			int numPano2 = Integer.valueOf(panoid2.substring(4));
			
			
			
			
			for(int j=numPano1;j<=numPano2;j++){
				if(j==numPano2 && j-temp!=panoramaList.getLength()-1){
					break;
				}
				
				
				//xy���W�̈ʒu���擾
				Element panoE = (Element) panoramaList.item(j-temp);
				Element coords = (Element) panoE.getElementsByTagName("coords").item(0);
				double x = Double.valueOf(coords.getAttribute("lng"));
				double y = Double.valueOf(coords.getAttribute("lat"));
				
				//xy���W��ł̉�]
				double xR = x_panoid1 + (x-x_panoid1)*Math.cos(diffAngle_rad) - (y-y_panoid1)*Math.sin(diffAngle_rad); 
				double yR = y_panoid1 + (x-x_panoid1)*Math.sin(diffAngle_rad) + (y-y_panoid1)*Math.cos(diffAngle_rad);				
				double xR_panoid2 = x_panoid1 + (x_panoid2-x_panoid1)*Math.cos(diffAngle_rad) - (y_panoid2-y_panoid1)*Math.sin(diffAngle_rad); 
				double yR_panoid2 = y_panoid1 + (x_panoid2-x_panoid1)*Math.sin(diffAngle_rad) + (y_panoid2-y_panoid1)*Math.cos(diffAngle_rad);				
				
				
				//xy���W�̈ʒu���ܓx�o�x�ɕϊ�
				//double lat = point1.getX() + (yR-y_panoid1) * Math.abs((point2.getX() - point1.getX())) / Math.abs((yR_panoid2 - y_panoid1)); 
				//double lng = point1.getY() +  (xR - x_panoid1) * Math.abs((point2.getY() - point1.getY())) / Math.abs((xR_panoid2 - x_panoid1));
				Point2D target = GeoMath.pointByDiff(point1.getX(), point1.getY(), (xR-x_panoid1)*xyRate, (yR-y_panoid1)*xyRate);
				double lat = target.getX();
				double lng = target.getY();
				
				
				
				//���ʏ��̍X�V
				////���ʏ��̎擾
				Element direction = (Element) panoE.getElementsByTagName("direction").item(0);
				Element img =  (Element) panoE.getElementsByTagName("img").item(0);
				int pxNorth = Integer.valueOf(direction.getAttribute("north"));
				int imageWidth = Integer.valueOf(img.getAttribute("width"));
				int pxNorth_changed = (imageWidth + pxNorth + (int)(diffAngle_rad/Math.PI*180.0/360.0 * imageWidth))%imageWidth;
				
				
				//Document���X�V
				/////�ϊ��O��lat,lng�������폜
				coords.removeAttribute("lat");
				coords.removeAttribute("lng");
				////�ϊ����lat,lng������ǉ�
				coords.setAttribute("lat", Double.toString(lat));
				coords.setAttribute("lng", Double.toString(lng));
				////�ϊ��O��north���폜
				direction.removeAttribute("north");
				////�ϊ����north��ǉ�
				direction.setAttribute("north", Integer.toString(pxNorth_changed));
			}
		}
		
	}
	
	
	
	
	public Document getDoc(){
		return pcd;
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
		if(args.length < 8){
			System.out.println("����������������܂���");
			return;
		}
		
		if( (args.length-2)%3 == 0){
			String readFile = args[0];
			String writeFile = args[1];
			String[] idArray = new String[ (args.length-2)/3 ];
			Point2D[] pointArray = new Point2D[ (args.length-2)/3 ];
			
			for(int i=0;i<(args.length-2)/3;i++){
				idArray[i] = args[2+i*3];
				pointArray[i] = new Point2D.Double( Double.valueOf(args[i*3+3]) , Double.valueOf(args[i*3+4]));
			}
			
			MappingLatLng mapping = new MappingLatLng(readFile);
			mapping.setPointLatLng(idArray, pointArray);
			
			//�p�m���}�摜�̃T�C�Y�����T�C�Y�������̂ɍ��킹��
			ModifyPCD mo = new ModifyPCD(mapping.getDoc());
			mo.modifyImageSize(1500, 456);
			
			
			PCDConverter converter = new PCDConverter();
			Document convertedPCD = converter.convert(mo.getDoc());	
			converter.writeXMLFile(writeFile, convertedPCD);
			//converter.writeXMLFile("C:\\xampp\\public_html\\ForKinojo2010\\resource\\pcd.xml", convertedPCD);
			
			
//			mapping.writeXML(writeFile);
		}
	}
	
}
