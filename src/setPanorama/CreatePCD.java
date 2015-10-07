package setPanorama;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

public class CreatePCD {
	
	public static double StartDir = 0.0;
	public static String MapType = "Google";
	
	private Map<String,Panorama> panoramaMap_filePath;
	
	public CreatePCD(Panorama[] panoramaArray,String savePath){
		//�p�m���}�摜�̃t�@�C���p�X��Panorama�I�u�W�F�N�g���֘A�t����}�b�v���쐻
		setPanoidMap(panoramaArray);
		
		//�^����ꂽPanorama�I�u�W�F�N�g�̔z�񂩂�APCD���쐬
		Document doc = convertXMLobj(panoramaArray);
		//���ʂ�xml�ŕۑ�
		writeXMLFile(savePath, doc);
	}

	
	/**
	 * �p�m���}�摜�̃t�@�C���p�X��Panorama�I�u�W�F�N�g���֘A�t����}�b�v���쐬
	 * @param panoramaArray
	 */
	private void setPanoidMap(Panorama[] panoramaArray){
		panoramaMap_filePath = new HashMap<String,Panorama>();
		for(int i=0;i<panoramaArray.length;i++){
			panoramaMap_filePath.put(panoramaArray[i].getFilePath(),panoramaArray[i]);
		}
	}
	
	
	
	public Document convertXMLobj(Panorama[] panoArray){
		try {
			//Document�̐���
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbuiler;
			dbuiler = dbfactory.newDocumentBuilder();
			Document doc = dbuiler.newDocument();  
			
			//���[�g�v�f�̍쐬
			Element pcd = doc.createElement("PCD");
			doc.appendChild(pcd);
			//pcd.appendChild(doc.createTextNode("\n"));
			
			//Panoramas�v�f�̍쐬
			Element panoramas = doc.createElement("Panoramas");
			panoramas.setAttribute("startpano", panoArray[0].getID());
			panoramas.setAttribute("startdir", Double.toString(StartDir));
			panoramas.setAttribute("maptype",MapType);
			pcd.appendChild(panoramas);
			
			//Panorama�v�f�̍쐬
			for(int i=0;i<panoArray.length;i++){
				Element panorama = doc.createElement("Panorama");
				panorama.setAttribute("panoid", panoArray[i].getID());
				panoramas.appendChild(panorama);
				
				Element img = doc.createElement("img");
				img.setAttribute("height", Integer.toString(panoArray[i].getImageHeight()));
				img.setAttribute("width", Integer.toString(panoArray[i].getImageWidth()));
				img.setAttribute("src", panoArray[i].getFileName());
				panorama.appendChild(img);
				
				Element coords = doc.createElement("coords");
				coords.setAttribute("lat", Double.toString(panoArray[i].getAboutY()));
				coords.setAttribute("lng", Double.toString(panoArray[i].getAboutX()));
				panorama.appendChild(coords);
				
				Element direction = doc.createElement("direction");
				direction.setAttribute("north", Integer.toString(panoArray[i].getPixelNorth()));
				panorama.appendChild(direction);
				
				
				//chpanos�v�f�̍쐬
				Element chpanos = doc.createElement("chpanos");
				panorama.appendChild(chpanos);
				//chpano�v�f�̍쐬
				ArrayList<PanoramaLink> panoLinkList = panoArray[i].getPanoLink();
				for(int j=0;j<panoLinkList.size();j++){
					//�ؑ֐�panoid
					String chpanoid = panoLinkList.get(j).getTargetPanoID();
					Panorama chpanorama = getPanoramaByID(panoArray, chpanoid);
					Element chpano = doc.createElement("chpano");
					chpano.setAttribute("panoid", chpanoid);
					chpanos.appendChild(chpano);
					
					//�͈ؑ֔�(�͈͂�60�x�Ō��ߑł�)
					double x_base = panoArray[i].getAboutX();
					double y_base = panoArray[i].getAboutY();
					double x_target = chpanorama.getAboutX();
					double y_target = chpanorama.getAboutY();
					double diff_x = x_target - x_base;
					double diff_y = y_target - y_base;
					double angleTarget = Math.atan2(diff_y, diff_x)/Math.PI*180.0;
					double dirTarget = (360.0 + 90.0 - angleTarget)%360.0;
					int range_start = (int) ((360+dirTarget-30)%360);
					int range_end = (int)( (360+dirTarget+30)%360 );
					Element range = doc.createElement("range");
					range.setAttribute("start", Integer.toString(range_start));
					range.setAttribute("end", Integer.toString(range_end));
					chpano.appendChild(range);
					
					
					//�ؑ֎���p�E�ؑ֌㎋��p(�l�͌��ߑł�)
					Element fov = doc.createElement("fov");
					fov.setAttribute("base" , "100.0");
					fov.setAttribute("next", "105.0");
					chpano.appendChild(fov);
					
					//�␳(�l�͌��ߑł��B���̗v�f�͍���PasQ�ɕK�v�Ȃ̂��H)
					Element correct = doc.createElement("correct");
					correct.setAttribute("pan", "0.0");
					correct.setAttribute("tilt", "0.0");
					chpano.appendChild(correct);
				}
				
				
			}
			
			
			return doc;
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private Panorama getPanoramaByID(Panorama[] panoArray,String panoID){
		for(int i=0;i<panoArray.length;i++){
			if(panoArray[i].getID().equals(panoID)){
				return panoArray[i];
			}
		}
		return null;
	}
	
	
	/**
	 * �w���Document���w��t�@�C���ɏ����o��
	 * @param writeFileName
	 * @param doc
	 */
	public void writeXMLFile(String writeFileName , Document doc){
		try {
			TransformerFactory  tfactory = TransformerFactory.newInstance();
			Transformer tf = tfactory.newTransformer();
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			tf.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
			File outputFile = new File(writeFileName);
			tf.transform(new DOMSource(doc), new StreamResult(outputFile));
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
