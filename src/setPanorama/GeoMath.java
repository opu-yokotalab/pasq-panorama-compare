package setPanorama;
import java.awt.geom.Point2D;

/**
 * 地理に関した計算をまとめたもの
 * @author hamano
 *
 */
public class GeoMath {
	
	/**
	 * 2点の緯度経度から、2点間の距離を求める
	 * @param lat_base
	 * @param lng_base
	 * @param lat_target
	 * @param lng_target
	 * @return
	 */
	public static double distance(double lat_base,double lng_base,double lat_target,double lng_target){
		//360度法からラジアンに変換
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
		
		
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		
		double dist = Math.sqrt(dx * dx + dy * dy);
		return dist;
	}
	
	
	/**
	 * 2点の緯度経度から、緯度方向の距離を求める
	 * @param lat_base
	 * @param lat_target
	 * @return
	 */
	public static double distance_lat(double lat_base,double lng_base,double lat_target,double lng_target){
		//360度法からラジアンに変換
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
		
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		return dy;
	}
	
	
	/**
	 * 2点の緯度経度から、経度方向の距離を求める
	 * @param lng_base
	 * @param lng_target
	 * @return
	 */
	public static double distance_lng(double lat_base,double lng_base,double lat_target,double lng_target){
		//360度法からラジアンに変換
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
		
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		return dx;
	}
	
	
		
	/**
	 * ある位置(base)から別の位置(target)への方位を求める
	 * @param lat_base
	 * @param lng_base
	 * @param lat_target
	 * @param lng_target
	 * @return
	 */
	public static double direction(double lat_base,double lng_base,double lat_target,double lng_target){
		//360度法からラジアンに変換
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
				
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		
		double dir = Math.atan2(dx,dy);
		dir = dir * 180 / Math.PI; //ラジアンから360度法へ
		dir = (360.0+dir)%360.0; //0-360度へ
		
		return dir;
	}
	
	/**
	 * A点、B点、C点でできる三角形のうち、三角測量によりC点の位置を求める
	 * @param latA A点の緯度
	 * @param lngA A点の経度
	 * @param dirAtoC A点からC点への方位
	 * @param latB B点の緯度
	 * @param lngB B点の経度
	 * @param dirBtoC B点からC点への方位
	 * @return Point2D(緯度,経度)
	 */
	public static Point2D triangulate(double latA,double lngA,double dirAtoC,double latB,double lngB,double dirBtoC){
		//点Aから点Bへの方位を求めて、B-A-Cでできる角度を求める
		double dirAtoB = direction(latA,lngA,latB,lngB);
		double thetaA = theta(dirAtoB,dirAtoC);
		
		//点Bから点Aへの方位を求めて、A-B-Cでできる角度を求める
		double dirBtoA = direction(latB,lngB,latA,lngA);
		double thetaB = theta(dirBtoA,dirBtoC);
		
		//A-C-Bでできる角度
		double thetaC = 180 - thetaA - thetaB;
		
		//どれかの角度が0以下で、三角測量ができないとき、nullを返す
		if(thetaA <= 0 || thetaB<=0 || thetaC<=0){
			return null;
		}
		
		
		//角度をラジアンへ
		thetaA = thetaA * Math.PI/180.0;
		thetaB = thetaB * Math.PI/180.0;
		thetaC = thetaC * Math.PI/180.0;
		
		//それぞれの点間の距離
		double distAB = distance(latA,lngA,latB,lngB);
		double distBC = distAB * Math.sin(thetaA)/Math.sin(thetaC);
		double distAC = distAB * Math.sin(thetaB)/Math.sin(thetaC);
		
		//AC間の距離と点Aの緯度経度を基に、点Cの緯度経度を求める
		double angleAtoC = (360 + 90 - dirAtoC)%360;  //方位を角度に変換
		angleAtoC = angleAtoC * Math.PI/180.0; //角度をラジアンに変換
		double latArad = latA * Math.PI/180.0; //計算のために緯度をラジアンに変換
		double latC = latA + (distAC*Math.sin(angleAtoC))/6378137 * 180/Math.PI;
		double lngC = lngA + (distAC*Math.cos(angleAtoC))/6378137/Math.cos(latArad) * 180/Math.PI;
		
		Point2D pointC = new Point2D.Double(); 
		pointC.setLocation(latC, lngC);
		return pointC;
	}
	
	
	
	
	
	/**
	 * 方位2つを与えて、その間にできる角度を求める
	 * @param dir1 方位1
	 * @param dir2 方位2
	 * @return 方位1と方位2の間にできる角度
	 */
	private static double theta(double dir1,double dir2){
		//差が180度以内
		if(Math.abs(dir1-dir2)<=180){
			return Math.abs(dir1-dir2);
		}
		//差が180度以上なら、0度をまたいでいる
		else{
			if(dir1 < dir2){
				return dir1+(360-dir2);
			}else{
				return dir2+(360-dir1);
			}
		}
	}
	
	
	
	/**
	 * 基準点から、方位何度に何メートル移動させた点の緯度経度を求める
	 * @param lat 基準となる緯度
	 * @param lng 基準となる経度
	 * @param dir 移動させる方位
	 * @param dist 移動させる距離(メートル)
	 * @return
	 */
	public static Point2D pointByDirDist(double lat,double lng,double dir,double dist){
		//北を0とする方位からxy座標の角度に変換
		double angle = (360.0 - dir + 90.0)%360.0;
		
		//x,y軸の変化量を求める
		double dx = dist * Math.cos(angle);
		double dy = dist * Math.sin(angle);
		
		return pointByDiff(lat,lng,dx,dy);
	}
	
	
	
	/**
	 * 基準点からx,y軸に移動させたときの緯度経度を求める
	 * @param lat 基準となる緯度
	 * @param lng 基準となる経度
	 * @param dx x軸の差分(メートル)
	 * @param dy y軸の差分(メートル)
	 * @return 点(緯度,経度)
	 */
	public static Point2D pointByDiff(double lat,double lng,double dx,double dy){
		//緯度経度を360度法からラジアンに変換
		double lat_rad = lat * Math.PI / 180.0;
		double lng_rad = lng * Math.PI / 180.0;
		
		
		//求める緯度経度 latT,lngT
		double lngT_rad = dx / Math.cos(lat_rad) / 6378137.0 + lng_rad;
		double latT_rad = dy / 6378137.0 + lat_rad;
		
		//ラジアンから360度法に変換
		double latT = latT_rad / Math.PI * 180.0;
		double lngT = lngT_rad / Math.PI * 180.0;
		
		return new Point2D.Double(latT,lngT);
	}
	
	

}
