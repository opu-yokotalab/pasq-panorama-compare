package setPanorama;

import java.awt.geom.Point2D;

/**
 * 座標関係の計算をまとめたもの
 * @author hamano
 *
 */
public class LocationMath {
	
	/**
	 * baseの位置からtargetの位置への方位(y軸正の向きが基準)を求める
	 * @param x_base
	 * @param y_base
	 * @param x_target
	 * @param y_target
	 * @return
	 */
	public static double direction(double x_base,double y_base,double x_target,double y_target){				
		double dx = x_target - x_base;
		double dy = y_target - y_base;
		
		double angle = Math.atan2(dy,dx);
		angle = angle * 180 / Math.PI; //ラジアンから360度法へ
		double dir = (360.0 - angle + 90.0)%360.0;
		
		return dir;
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
	 * 三角測量
	 * @param x_base1
	 * @param y_base1
	 * @param dirBase1toTarget
	 * @param x_base2
	 * @param y_base2
	 * @param dirBase2toTarget
	 * @return
	 */
	public static Point2D triangulate(double x_base1,double y_base1,double dirBase1toTarget,double x_base2,double y_base2,double dirBase2toTarget){
		System.out.println(x_base1 + " " + y_base1 + "  " + dirBase1toTarget + "      " + x_base2 + " " + y_base2 + "  " + dirBase2toTarget);
		
		//base1からbase2への方位を求めて、base2-base1-targetでできる角度thetaAを求める
		double dirBase1toBase2 = direction(x_base1,y_base1,x_base2,y_base2);
		double thetaA = theta(dirBase1toBase2,dirBase1toTarget);
		
		//base2からbase1への方位を求めて、base1-base2-targetでできる角度thetaBを求める
		double dirBase2toBase1 = direction(x_base2, y_base2, x_base1, y_base1);
		double thetaB = theta(dirBase2toBase1,dirBase2toTarget);
		
		
		System.out.println(dirBase1toBase2 + "       " + dirBase2toBase1);
		
		//base1-target-base2でできる角度thetaCを求める
		double thetaC = 180.0 - thetaA - thetaB;
		
		//どれかの角度が0以下で、三角測量ができないとき、nullを返す
		if(thetaA <= 0 || thetaB<=0 || thetaC<=0){
			System.out.println("三角測量ができない  " + thetaA + " , " + thetaB + " , " + thetaC);
			return null;
		}

		if(thetaA < 25 || thetaB < 25 || thetaC < 25 ){
			return null;
		}
		
		
		
		//角度をラジアンへ
		thetaA = thetaA * Math.PI/180.0;
		thetaB = thetaB * Math.PI/180.0;
		thetaC = thetaC * Math.PI/180.0;
		
		
		//それぞれの点間の距離		
		double distBase1Base2 = Math.sqrt( (x_base1-x_base2)*(x_base1-x_base2) + (y_base1-y_base2)*(y_base1-y_base2) );
		double distBase2Target = distBase1Base2 * Math.sin(thetaA)/Math.sin(thetaC);
		double distBase1Target = distBase1Base2 * Math.sin(thetaB)/Math.sin(thetaC);
		
		//base1とtarget間の距離とbase1の位置を基にtargetの位置を求める
		double angle = (360 + 90 - dirBase1toTarget)%360;  //方位を角度に変換
		angle = angle * Math.PI/180.0; //角度をラジアンに変換
		double x_target = x_base1 + (distBase1Target * Math.cos(angle));
		double y_target = y_base1 + (distBase1Target * Math.sin(angle));
		
		Point2D target = new Point2D.Double(); 
		target.setLocation(x_target, y_target);
		System.out.println(x_target + "  ,  " + y_target);
		return target;
	}
	
	
	
}
