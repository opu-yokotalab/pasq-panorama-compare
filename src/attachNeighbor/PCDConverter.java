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
		//Panoramas要素の属性取得
		Element root = inputPCD.getDocumentElement();
		Element panoramas = (Element) root.getElementsByTagName("Panoramas").item(0);
		maptype = panoramas.getAttribute("maptype");
		startdir = panoramas.getAttribute("startdir");
		startpano = panoramas.getAttribute("startpano");
		
		
		//読み取ったpcdからPanoramaオブジェクト生成
		ArrayList<Panorama> panoramaList = xmlToObject(inputPCD);
				
		//近傍パノラマの付与
		panoramaList = attachChangePanorama(panoramaList);
		
		//XMLの作成
		Document doc = objectToXML(panoramaList);
		
		return doc;
	}
	
	
	
	/**
	 * 指定のpanoidを基に対応するPanoramaオブジェクトを取得する
	 * @param panoid
	 * @param panoList
	 * @return
	 */
	public Panorama panoIDtoPanorama(String panoid,ArrayList<Panorama> panoList){
		Iterator iterator = panoList.iterator();
		while(iterator.hasNext()){
			Panorama pano = (Panorama) iterator.next();
			//panoのpanoIDとpanoidが同じなら
			if( pano.getPanoID().equals(panoid) ){
				return pano;
			}
		}	
		return null;
	}
	
	
	
	
	/**
	 * xmlを読み込んで、オブジェクト化する
	 * @param inputPCD
	 * @return
	 */
	public ArrayList<Panorama> xmlToObject(Document inputPCD){
		//ルート要素を取得
		Element root = inputPCD.getDocumentElement();
		
		//Panorama要素のリストを取得
		NodeList panoList = root.getElementsByTagName("Panorama");
		
		//Panorama要素の数だけループし、Panoramaのリストを作成
		ArrayList<Panorama> panoramaList = new ArrayList<Panorama>();
		for(int i=0;i<panoList.getLength();i++){
			panoramaList.add(new Panorama((Element) panoList.item(i) ) );
		}
		
		return panoramaList;
	}
	
	
	/**
	 * 近傍情報の付与
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
	 * 作成したオブジェクトを基にxmlを生成する
	 * @param panoList
	 * @return
	 */
	public Document objectToXML(ArrayList<Panorama> panoList){
		try {
			//Documentの生成
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbuiler = dbfactory.newDocumentBuilder();
			Document doc = dbuiler.newDocument();  
			
			//ルート要素の作成
			Element pcd = doc.createElement("PCD");
			doc.appendChild(pcd);
			//pcd.appendChild(doc.createTextNode("\n"));
			
			//Panoramas要素の作成
			Element panoramas = doc.createElement("Panoramas");
			panoramas.setAttribute("startpano", startpano);
			panoramas.setAttribute("startdir", startdir);
			panoramas.setAttribute("maptype",maptype);
			pcd.appendChild(panoramas);
			//pcd.appendChild(doc.createTextNode("\n"));
			
			//Panorama要素の作成
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
	 * 指定のDocumentを指定ファイルに書き出す
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
				// ドキュメントビルダーファクトリを生成
				DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
				// ドキュメントビルダーを生成
				DocumentBuilder builder = dbfactory.newDocumentBuilder();
				// パースを実行してDocumentオブジェクトを取得
				Document pcd = builder.parse(new File(readFile));
				
				//PCDをコンバート
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
			System.out.println("読込ファイルと出力ファイルを入力してください");
		}

	}
}
