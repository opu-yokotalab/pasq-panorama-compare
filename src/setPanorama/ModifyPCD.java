package setPanorama;

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

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

/**
 * PCD���C���������
 * (����ł́A�摜�T�C�Y��ύX���郁�\�b�h�������݂��Ȃ�)
 * @author hamano
 *
 */
public class ModifyPCD {
	
	Map<String, Element> panoID;
	Document pcd;
	
	ModifyPCD(Document doc){
		pcd = doc;
	}
	ModifyPCD(String filePath){
		pcd = readXML(filePath);
	}
	

	
	
	/**
	 * �p�m���}�摜�̃T�C�Y��ύX
	 */
	public void modifyImageSize(int width,int height){
		Element root = pcd.getDocumentElement();
		NodeList panoramaList = root.getElementsByTagName("Panorama");
		
		for(int i=0;i<panoramaList.getLength();i++){
			//�摜�̃T�C�Y��ύX
			Element panoE = (Element) panoramaList.item(i);
			Element imgE = (Element) panoE.getElementsByTagName("img").item(0);
			int imageWidth_before = Integer.valueOf( imgE.getAttribute("width") );
			////�摜�T�C�Y�ύX
			imgE.removeAttribute("height");
			imgE.removeAttribute("width");
			imgE.setAttribute("height", Integer.toString(height));
			imgE.setAttribute("width", Integer.toString(width));
			
			
			//�摜�̃T�C�Y�ɍ��킹�āA�k�̈ʒu���C��
			Element directionE = (Element) panoE.getElementsByTagName("direction").item(0);
			int north = Integer.valueOf(directionE.getAttribute("north"));
			int north_changed = (int) ((double)width / (double)imageWidth_before * (double)north);
			//�k�̕ύX
			directionE.removeAttribute("north");
			directionE.setAttribute("north", Integer.toString(north_changed));
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
		if(args.length == 4){
			String readFile = args[0];
			String writeFile = args[1];
			int width = Integer.valueOf(args[2]);
			int height = Integer.valueOf(args[3]);
			
			ModifyPCD mo = new ModifyPCD(readFile);
			mo.modifyImageSize(width, height);
			mo.writeXML(writeFile);
			
		}else{
			System.out.println("����������������܂���");
		}
	}
	
	
}
