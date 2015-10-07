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
 * 鬼ノ城において、方向関係推定の結果があまりにも悪いため、
 * 応急処置的に作成したクラス
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
		
		//panoidとパノラマ要素のテーブルを作成
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
			//////方位情報の取得
			Element imgE =  (Element) panoE.getElementsByTagName("img").item(0);
			imageWidth[i] = Integer.valueOf(imgE.getAttribute("width"));
		}
		
		//方向関係結果を座標から推測
		double[] dist = new double[panoramaList.getLength()-1];
		int[] pxToNextPano = new int[panoramaList.getLength()-1];
		int[] pxToBackPano = new int[panoramaList.getLength()-1];
		for(int i=0;i<panoramaList.getLength()-1;i++){
			//前後のパノラマ(back,next)の情報取得
			////前のパノラマ(back)の情報取得
			//////座標取得
			Element panoE_back = (Element) panoramaList.item(i);
			Element coordsE_back = (Element) panoE_back.getElementsByTagName("coords").item(0);
			double x_back = Double.valueOf(coordsE_back.getAttribute("lng"));
			double y_back = Double.valueOf(coordsE_back.getAttribute("lat"));
			
			//////方位情報の取得
			Element directionE_back = (Element) panoE_back.getElementsByTagName("direction").item(0);
			Element imgE_back =  (Element) panoE_back.getElementsByTagName("img").item(0);
			int pxNorth_back = Integer.valueOf(directionE_back.getAttribute("north"));
			int imageWidth_back = Integer.valueOf(imgE_back.getAttribute("width"));
			
			////後のパノラマ(next)の情報取得
			//////座標取得
			Element panoE_next = (Element) panoramaList.item(i+1);
			Element coordsE_next = (Element) panoE_next.getElementsByTagName("coords").item(0);
			double x_next = Double.valueOf(coordsE_next.getAttribute("lng"));
			double y_next = Double.valueOf(coordsE_next.getAttribute("lat"));
			//////方位情報の取得
			Element directionE_next = (Element) panoE_next.getElementsByTagName("direction").item(0);
			Element imgE_next =  (Element) panoE_next.getElementsByTagName("img").item(0);
			int pxNorth_next = Integer.valueOf(directionE_next.getAttribute("north"));
			int imageWidth_next = Integer.valueOf(imgE_next.getAttribute("width"));
			
			
			//前後パノラマの距離を計算
			double dist_x = x_next - x_back;
			double dist_y = y_next - y_back;
			dist[i] = Math.sqrt( dist_x*dist_x + dist_y*dist_y  );
			
			//方向関係の推測
			double dirBackToNext = LocationMath.direction(x_back, y_back, x_next, y_next);
			double temp = dirBackToNext/360.0*imageWidth_back;
			pxToNextPano[i] = (int) (( imageWidth_back + temp + pxNorth_back)%imageWidth_back);

			double dirNextToBack = LocationMath.direction(x_next, y_next, x_back, y_back);
			temp = dirNextToBack / 360.0 * imageWidth_next;
			pxToBackPano[i] =  (int) ((imageWidth_next + temp + pxNorth_next)%imageWidth_next);
		}
		
		
		
		
		//panoidを数値に変換(与えられるpanoidが撮影順序に基づき連番であること、panoidがpano+数字であることを想定)
		Element tempE = (Element) panoramaList.item(0);
		int numStart = Integer.valueOf(tempE.getAttribute("panoid").substring(4));
		//方向関係結果の修正(pxToNextPano,pxToBackPanoを置き換える)
		for(int i=0;i<panoid.length;i++){
			int num = Integer.valueOf(panoid[i].substring(4));
			pxToBackPano[num-numStart] = pixelToBackPano_input[i];
			pxToNextPano[num-numStart] = pixelToNextPano_input[i];
		}
		
		
		
		//方向関係を基に、位置を推定
		Point2D[] modifiedLocation = new Point2D.Double[panoramaList.getLength()];
		int[] modifiedPxNorth = new int[panoramaList.getLength()];
		////最初のパノラマの位置・方位
		Element panoE = (Element) panoramaList.item(0);
		Element coordsE = (Element) panoE.getElementsByTagName("coords").item(0);
		double x = Double.valueOf(coordsE.getAttribute("lng"));
		double y = Double.valueOf(coordsE.getAttribute("lat"));
		Element directionE = (Element) panoE.getElementsByTagName("direction").item(0);
		modifiedLocation[0] = new Point2D.Double(x,y);
		modifiedPxNorth[0] = Integer.valueOf(directionE.getAttribute("north"));
		
		////始点パノラマから順々にパノラマの相対位置を決定していく
		for(int i=1;i<panoramaList.getLength();i++){
			//backのパノラマの情報取得
			double x_back = modifiedLocation[i-1].getX();
			double y_back = modifiedLocation[i-1].getY();
			int pxNorth_back = modifiedPxNorth[i-1];

			
			//nextの相対位置を計算
			double dirBackToNext = (360.0 + (double)(pxToNextPano[i-1] - pxNorth_back)/(double)imageWidth[i-1]*360.0)%360.0;
			double angleBackToNext =  (360.0 - dirBackToNext + 90.0)%360.0;
			double angleBackToNext_rad = angleBackToNext / 180.0 * Math.PI;
			x = x_back + dist[i-1] * Math.cos(angleBackToNext_rad);
			y = y_back + dist[i-1] * Math.sin(angleBackToNext_rad);
			modifiedLocation[i] = new Point2D.Double(x,y);
			
			//nextの方位情報を計算
			modifiedPxNorth[i] = calculatePixelNorth(x, y, x_back, y_back, pxToBackPano[i-1],imageWidth[i]); 
		}
		
		
		//XMLのDocument更新
		for(int i=0;i<panoramaList.getLength();i++){
			panoE = (Element) panoramaList.item(i);
			coordsE = (Element) panoE.getElementsByTagName("coords").item(0);
			directionE = (Element) panoE.getElementsByTagName("direction").item(0);
			
			//Documentを更新
			////修正前のlat,lng属性を削除
			coordsE.removeAttribute("lat");
			coordsE.removeAttribute("lng");
			////修正後のlat,lng属性を追加
			coordsE.setAttribute("lat", Double.toString(modifiedLocation[i].getY()));
			coordsE.setAttribute("lng", Double.toString(modifiedLocation[i].getX()));
			////修正前のnorthを削除
			directionE.removeAttribute("north");
			////修正後のnorthを追加
			directionE.setAttribute("north", Integer.toString(modifiedPxNorth[i]));
		}
	}
	
	
	

	
	
	
	public Document getDoc(){
		return pcd;
	}
	
	
	/**
	 * 2つのパノラマの座標とBase->Target方向をもとに、Baseの北の位置が
	 * どこになるかを計算
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
		double diffDir = (360.0 - dirBtoT)%360.0; //BaseからTargetへの方向と北方向の差分を求める
		double diffRate = diffDir / 360.0;
		int pixelNorth = (widthBase + pixelDirBtoT +  (int)Math.round(diffRate*widthBase))%widthBase;
		return pixelNorth;
	}
	
	private Document readXML(String filePath){
		try {
			// ドキュメントビルダーファクトリを生成
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			// ドキュメントビルダーを生成
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			// パースを実行してDocumentオブジェクトを取得
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
			System.out.println("引数が正しくありません");
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
			System.out.println("引数が正しくありません");
		}
	}
	
	
}
