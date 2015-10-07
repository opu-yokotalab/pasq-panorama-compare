package panoramaCompare;

import mySurf.PairInterestPoints;

/**
 * 一度、整理が必要。
 * @author hamano
 *
 */
public class CouplePairs {
	private PairInterestPoints pair1;
	private PairInterestPoints pair2;
	
	
	CouplePairs(PairInterestPoints pair1,PairInterestPoints pair2){
		this.pair1 = pair1;
		this.pair2 = pair2;
	}
	

	
	
	public boolean cross(int width_base, int width_target){
		double xr1_base = pair1.getPointBase().getX() / (double)width_base;
		double xr2_base = pair2.getPointBase().getX() / (double)width_base;
		double xr1_target = pair1.getPointTarget().getX() / (double)width_target;
		double xr2_target = pair2.getPointTarget().getX() / (double)width_target;
		
		double diff_base = PanoramaImage.diffRate(xr1_base, xr2_base);
		double diff_target = PanoramaImage.diffRate(xr1_target, xr2_target);
		
		
		
		
		if(diff_base > 0 && diff_target > 0){
			return false;
		}
		else if(diff_base < 0 && diff_target < 0){
			return false;
		}
		else{
			//要修正 以下の条件  差分が画像幅の半分に近いと、誤り判定してしまうので以下の条件を追加したが、条件内容は適当
			if(Math.abs(diff_base) > 0.40 && Math.abs(diff_target) > 0.40){
				return false;
			}else{
				return true;
			}
		}
	}
	
	
	public boolean cross(int width_base, int width_target, int height_base, int height_target){
		double pair1base_x=pair1.getPointBase().getX()/(double)width_base;
		double pair1base_y=pair1.getPointBase().getY()/(double)height_base+1.0;
		double pair1target_x=pair1.getPointTarget().getX()/(double)width_target;
		double pair1target_y=pair1.getPointTarget().getY()/(double)height_target;
		double pair2base_x=pair2.getPointBase().getX()/(double)width_base;
		double pair2base_y=pair2.getPointBase().getY()/(double)height_base+1.0;
		double pair2target_x=pair2.getPointTarget().getX()/(double)width_target;
		double pair2target_y=pair2.getPointTarget().getY()/(double)height_target;
		if(Math.abs(pair1base_x-pair1target_x)>0.5){
			if(pair1base_x<0.5){
				pair1target_x-=1.0;
			}
			else{
				pair1base_x-=1.0;
			}
		}
		if(Math.abs(pair2base_x-pair2target_x)>0.5){
			if(pair2base_x<0.5){
				pair2target_x-=1.0;
			}
			else{
				pair2base_x-=1.0;
			}
		}
		double rc=(pair2target_y-pair2base_y)*(pair2base_x-pair1base_x)-(pair2target_x-pair2base_x)*(pair2base_y-pair1base_y);
		double rm=(pair1target_x-pair1base_x)*(pair2target_y-pair2base_y)-(pair1target_y-pair1base_y)*(pair2target_x-pair2base_x);
		double sc=(pair1target_y-pair1base_y)*(pair2base_x-pair1base_x)-(pair1target_x-pair1base_x)*(pair2base_y-pair1base_y);
		double sm=(pair1target_x-pair1base_x)*(pair2target_y-pair2base_y)-(pair1target_y-pair1base_y)*(pair2target_x-pair2base_x);
		if(rm==0){
			return false;
		}
		double r=rc/rm;
		double s=sc/sm;
		if(r>0.0&&r<1.0&&s>0.0&&s<1.0){
			return true;
		}
		else{
			return false;
		}
	}
	
	
	
	
	
	public boolean flagReverse_x(){
		double dx_base = getDx_base();
		double dx_target = getDx_target();
		if(dx_base > 0){
			if(dx_target < 0){
				return true;
			}
		}
		if(dx_base < 0){
			if(dx_target > 0){
				return true;
			}
		}	
		return false;
	}
	
	
	public boolean flagReverse_y(){
		double dy_base = getDy_base();
		double dy_target = getDy_target();
		if(dy_base > 0){
			if(dy_target < 0){
				return true;
			}
		}
		if(dy_base < 0){
			if(dy_target > 0){
				return true;
			}
		}	
		return false;
	}
	

	
	
	
	
	public double getScale_x(){
		double dx_base = Math.round(  Math.abs(getDx_base() ) );
		double dx_target = Math.round( Math.abs(getDx_target()) );
		if(dx_base == 0 ){
			return 0;
		}
		return dx_target/dx_base;
	}
	
	
	public double getScale_y(){
		double dy_base = Math.round(  Math.abs(getDy_base() ) );
		double dy_target = Math.round( Math.abs(getDy_target()) );
		if(dy_base == 0 ){
			return 0;
		}
		return dy_target/dy_base;
	}
	
	public boolean contain(PairInterestPoints pair){
		if(pair == pair1 || pair == pair2){
			return true;
		}
		return false;
	}
	
	
	public double getDx_base(){
		return pair2.getPointBase().getX() - pair1.getPointBase().getX();
	}
	
	public double getDx_target(){
		return pair2.getPointTarget().getX() - pair1.getPointTarget().getX();
	}
	
	public double getDy_base(){
		return pair2.getPointBase().getY() - pair1.getPointBase().getY();
	}
	
	public double getDy_target(){
		return pair2.getPointTarget().getY() - pair1.getPointTarget().getY();
	}
	
	public PairInterestPoints getPair1(){
		return this.pair1;
	}
	
	public PairInterestPoints getPair2(){
		return this.pair2;
	}
	
}
