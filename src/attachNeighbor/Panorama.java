package attachNeighbor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;

import org.w3c.dom.Element;

import sun.awt.SunToolkit.InfiniteLoop;

/**
 * Panorama要素をオブジェクトで表したもの
 * changePanoListはchpanoを表す
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
	
	public ArrayList<Chpano> changePanoList;  //近傍パノラマリスト
	
	
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
	 * プロパティを設定
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
	 * 近傍パノラマの選択
	 * 選択された近傍パノラマを元に切替範囲を割り当てる
	 * 
	 * 
	 */
	
	
	
	/**
	 * 切替の設定
	 * @param list
	 */
	public void setChangePano(ArrayList<Panorama> list){
		//近傍パノラマの選択
		Chpano allTarget[] = new Chpano[list.size()];
		ArrayList<Chpano> allTargetList = new ArrayList<Chpano>();
		
		
		//距離でソートできるようにする
		ListIterator iterator = list.listIterator();
		for(int i=0;i<list.size();i++){
			Panorama pano = list.get(i);
			double latTarget = pano.getLat();
			double lngTarget = pano.getLng();
			
			//360度法からラジアンに変換
			double latrad = this.lat * Math.PI / 180;
			double lngrad = this.lng * Math.PI / 180;
			double latTargetrad = latTarget * Math.PI / 180;
			double lngTargetrad = lngTarget * Math.PI / 180;
			
			
			double dx = 6378137 * (lngTargetrad - lngrad) * Math.cos(latrad);
			double dy = 6378137 * (latTargetrad - latrad);
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			double dir = Math.atan2(dx,dy);
			dir = dir * 180 / Math.PI; //ラジアンから360度法へ
			dir = (360+dir)%360; //0-360度へ
			
			// id、距離、方位をリスト化
			Chpano target = new Chpano(this,pano,dist,dir);
			allTarget[i] = target;
		}
		TheComparator comparator = new TheComparator();
		Arrays.sort(allTarget,comparator);
        
		
		//切替範囲の割り当て
		attachChangeInfo(allTarget);
		
	}
	
	
	//近傍パノラマの選択
	public void chooseNeighbor(Chpano allTarget[]){
	}
	//近傍パノラマを基に、切替範囲を割り当てる
	public void allotChangeDir(){
	}
	
	
	/**
	 * convertPCD の一部コピー
	 * @param allTarget
	 */
	public void attachChangeInfo(Chpano allTarget[]){
		Chpano arLink[] = new Chpano[360];
    	for(int j = 1; j<allTarget.length; j++){ // 現在のパノラマ画像も含むため j=1 から
    		//距離がある一定値以内であれば、近傍パノラマとして切替有にする
    		double distToTarget = allTarget[j].getDist();
    		if(distToTarget < dist_threshold){
    			if((arLink[(int)Math.floor(allTarget[j].getDir())] == null)){
        			
        			// 切替範囲の割当計算(要変更)
    	    		double tmp = Math.pow(0.95, allTarget[j].getDist()) * 100 + 20;
    	    		int offd = (int)Math.floor(tmp / 2);
    	    		
    	    		int sd = (int)Math.floor((360 + allTarget[j].getDir() - offd) % 360);
    	    		int ed = (int)Math.floor((360 + allTarget[j].getDir() + offd) % 360);
    	    		if(sd > ed){ // 0度をまたぐとき
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
    	
    	
    	// 初期位置
    	int startDir = 0;
    	int k=0;
    	
		// 切替範囲が0°をまたぐ場合だけ for文の外で処理
		if((arLink[0] != null) && (arLink[0].equals(arLink[359]))){
			// 初期位置および切替範囲の開始位置
			startDir = 359;
			while(arLink[startDir].equals(arLink[startDir-1])){
				startDir--;
			}
			
			// 切替範囲の終了位置
			k = startDir;
			while((arLink[(k+1)%360] != null) && (arLink[k].equals(arLink[(k+1)%360]))){
				k = (k + 1) % 360;
			}
			
			arLink[k].setChangeDir(startDir, k);
			changePanoList.add(arLink[k]);
			k = (k+1)%360;
			
		}
		
		// その他の切替範囲の処理
		do{
			if(arLink[k] != null){
				int tmp = k;
				while(arLink[k].equals(arLink[(k+1)%360])){
					k = (k + 1) % 360;
				}
				
				// 切替範囲の設定 (tmp -> k)
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
