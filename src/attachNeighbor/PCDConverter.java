package attachNeighbor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;


public class PCDConverter {
	
	static double fovFBase=100;
	static double fovFNext = 105;
	static String resourceFolder = "c:\\study\\resource";
	
	private String maptype;
	private String startdir;
	private String startpano;
//	private ArrayList<Panorama> panoramaList;
	private ArrayList<Chpano> tmMissList = new ArrayList<Chpano>();
	
	public Document convert(Document inputPCD){
		//Panoramas�v�f�̑����擾
		Element root = inputPCD.getDocumentElement();
		Element panoramas = (Element) root.getElementsByTagName("Panoramas").item(0);
		maptype = panoramas.getAttribute("maptype");
		startdir = panoramas.getAttribute("startdir");
		startpano = panoramas.getAttribute("startpano");
		
		
		//�ǂݎ����pcd����Panorama�I�u�W�F�N�g����
		ArrayList<Panorama> panoramaList = xmlToObject(inputPCD);
				
		//�ߖT�p�m���}�̕t�^
		panoramaList = attachChangePanorama(panoramaList);
		
		//XML�̍쐬
		Document doc = objectToXML(panoramaList);
		
		return doc;
	}
	
	
	
	/**
	 * �w���panoid����ɑΉ�����Panorama�I�u�W�F�N�g���擾����
	 * @param panoid
	 * @param panoList
	 * @return
	 */
	public Panorama panoIDtoPanorama(String panoid,ArrayList<Panorama> panoList){
		Iterator iterator = panoList.iterator();
		while(iterator.hasNext()){
			Panorama pano = (Panorama) iterator.next();
			//pano��panoID��panoid�������Ȃ�
			if( pano.getPanoID().equals(panoid) ){
				return pano;
			}
		}	
		return null;
	}
	
	
	
	
	/**
	 * xml��ǂݍ���ŁA�I�u�W�F�N�g������
	 * @param inputPCD
	 * @return
	 */
	public ArrayList<Panorama> xmlToObject(Document inputPCD){
		//���[�g�v�f���擾
		Element root = inputPCD.getDocumentElement();
		
		//Panorama�v�f�̃��X�g���擾
		NodeList panoList = root.getElementsByTagName("Panorama");
		
		//Panorama�v�f�̐��������[�v���APanorama�̃��X�g���쐬
		ArrayList<Panorama> panoramaList = new ArrayList<Panorama>();
		for(int i=0;i<panoList.getLength();i++){
			panoramaList.add(new Panorama((Element) panoList.item(i) ) );
		}
		
		return panoramaList;
	}
	
	
	/**
	 * �ߖT���̕t�^
	 * @param panoList
	 * @return
	 */
	public ArrayList<Panorama> attachChangePanorama(ArrayList<Panorama> panoList){
		ListIterator iterator = panoList.listIterator();
		while(iterator.hasNext()){
			Panorama temp = (Panorama) iterator.next();
			temp.setChangePano(panoList);
		}	
		return panoList;
	}
	
	
	/**
	 * �쐬�����I�u�W�F�N�g�����xml�𐶐�����
	 * @param panoList
	 * @return
	 */
	public Document objectToXML(ArrayList<Panorama> panoList){
		try {
			//Document�̐���
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbuiler = dbfactory.newDocumentBuilder();
			Document doc = dbuiler.newDocument();  
			
			//���[�g�v�f�̍쐬
			Element pcd = doc.createElement("PCD");
			doc.appendChild(pcd);
			//pcd.appendChild(doc.createTextNode("\n"));
			
			//Panoramas�v�f�̍쐬
			Element panoramas = doc.createElement("Panoramas");
			panoramas.setAttribute("startpano", startpano);
			panoramas.setAttribute("startdir", startdir);
			panoramas.setAttribute("maptype",maptype);
			pcd.appendChild(panoramas);
			//pcd.appendChild(doc.createTextNode("\n"));
			
			//Panorama�v�f�̍쐬
			Iterator panoIte = panoList.iterator();
			while(panoIte.hasNext()){
				Panorama panoObj = (Panorama) panoIte.next();
				Element panorama = doc.createElement("Panorama");
				panorama.setAttribute("panoid", panoObj.getPanoID());
				panoramas.appendChild(panorama);
				
				Element img = doc.createElement("img");
				img.setAttribute("height", Integer.toString(panoObj.getHeight()));
				img.setAttribute("width", Integer.toString(panoObj.getWidth()));
				img.setAttribute("src", panoObj.getSrc());
				panorama.appendChild(img);
				
				Element coords = doc.createElement("coords");
				coords.setAttribute("lat", Double.toString(panoObj.getLat()));
				coords.setAttribute("lng", Double.toString(panoObj.getLng()));
				panorama.appendChild(coords);
				
				Element direction = doc.createElement("direction");
				direction.setAttribute("north", Integer.toString(panoObj.getNorth()));
				panorama.appendChild(direction);
				
				Element chpanos = doc.createElement("chpanos");
				panorama.appendChild(chpanos);
				
				Iterator tpanoIte = panoObj.getChpano().iterator();
				while(tpanoIte.hasNext()){
					Chpano tpanoObj = (Chpano) tpanoIte.next();
					Element chpano = doc.createElement("chpano");
					chpano.setAttribute("panoid", tpanoObj.target.getPanoID());
					chpanos.appendChild(chpano);
					
					Element range = doc.createElement("range");
					range.setAttribute("end", Integer.toString(tpanoObj.getDirEnd()));
					range.setAttribute("start", Integer.toString(tpanoObj.getDirStart()));
					chpano.appendChild(range);
					
					Element fovF = doc.createElement("fov");
					fovF.setAttribute("base", Double.toString(tpanoObj.getFovFBase()));
					fovF.setAttribute("next", Double.toString(tpanoObj.getFovFNext()));
					chpano.appendChild(fovF);
					
					
					Element fovB = doc.createElement("fovB");
					fovB.setAttribute("base", Double.toString(tpanoObj.getFovBBase()));
					fovB.setAttribute("next", Double.toString(tpanoObj.getFovBNext()));
					chpano.appendChild(fovB);
					
					
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
	
	
	public static void main(String[] args){
		if(args.length == 2){
			String readFile = args[0];
			String writeFile = args[1];
			
//			String readFile = "XML\\cnv_pcd_opu.xml";
//			String writeFile = "XML\\cnv_pcd_opu.xml";
			
			try {
				// �h�L�������g�r���_�[�t�@�N�g���𐶐�
				DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
				// �h�L�������g�r���_�[�𐶐�
				DocumentBuilder builder = dbfactory.newDocumentBuilder();
				// �p�[�X�����s����Document�I�u�W�F�N�g���擾
				Document pcd = builder.parse(new File(readFile));
				
				//PCD���R���o�[�g
				PCDConverter converter = new PCDConverter();
				Document convertedPCD = converter.convert(pcd);
				converter.writeXMLFile(writeFile,convertedPCD);
				
				System.out.println("Finish");
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			System.out.println("�Ǎ��t�@�C���Əo�̓t�@�C������͂��Ă�������");
		}

	}
}
