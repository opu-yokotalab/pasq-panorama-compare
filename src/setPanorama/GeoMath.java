package setPanorama;
import java.awt.geom.Point2D;

/**
 * �n���Ɋւ����v�Z���܂Ƃ߂�����
 * @author hamano
 *
 */
public class GeoMath {
	
	/**
	 * 2�_�̈ܓx�o�x����A2�_�Ԃ̋��������߂�
	 * @param lat_base
	 * @param lng_base
	 * @param lat_target
	 * @param lng_target
	 * @return
	 */
	public static double distance(double lat_base,double lng_base,double lat_target,double lng_target){
		//360�x�@���烉�W�A���ɕϊ�
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
	 * 2�_�̈ܓx�o�x����A�ܓx�����̋��������߂�
	 * @param lat_base
	 * @param lat_target
	 * @return
	 */
	public static double distance_lat(double lat_base,double lng_base,double lat_target,double lng_target){
		//360�x�@���烉�W�A���ɕϊ�
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
		
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		return dy;
	}
	
	
	/**
	 * 2�_�̈ܓx�o�x����A�o�x�����̋��������߂�
	 * @param lng_base
	 * @param lng_target
	 * @return
	 */
	public static double distance_lng(double lat_base,double lng_base,double lat_target,double lng_target){
		//360�x�@���烉�W�A���ɕϊ�
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
		
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		return dx;
	}
	
	
		
	/**
	 * ����ʒu(base)����ʂ̈ʒu(target)�ւ̕��ʂ����߂�
	 * @param lat_base
	 * @param lng_base
	 * @param lat_target
	 * @param lng_target
	 * @return
	 */
	public static double direction(double lat_base,double lng_base,double lat_target,double lng_target){
		//360�x�@���烉�W�A���ɕϊ�
		double lat_base_rad = lat_base * Math.PI / 180;
		double lng_base_rad = lng_base * Math.PI / 180;
		double lat_target_rad = lat_target * Math.PI / 180;
		double lng_target_rad = lng_target * Math.PI / 180;
				
		double dx = 6378137 * (lng_target_rad - lng_base_rad) * Math.cos(lat_base_rad);
		double dy = 6378137 * (lat_target_rad - lat_base_rad);
		
		double dir = Math.atan2(dx,dy);
		dir = dir * 180 / Math.PI; //���W�A������360�x�@��
		dir = (360.0+dir)%360.0; //0-360�x��
		
		return dir;
	}
	
	/**
	 * A�_�AB�_�AC�_�łł���O�p�`�̂����A�O�p���ʂɂ��C�_�̈ʒu�����߂�
	 * @param latA A�_�̈ܓx
	 * @param lngA A�_�̌o�x
	 * @param dirAtoC A�_����C�_�ւ̕���
	 * @param latB B�_�̈ܓx
	 * @param lngB B�_�̌o�x
	 * @param dirBtoC B�_����C�_�ւ̕���
	 * @return Point2D(�ܓx,�o�x)
	 */
	public static Point2D triangulate(double latA,double lngA,double dirAtoC,double latB,double lngB,double dirBtoC){
		//�_A����_B�ւ̕��ʂ����߂āAB-A-C�łł���p�x�����߂�
		double dirAtoB = direction(latA,lngA,latB,lngB);
		double thetaA = theta(dirAtoB,dirAtoC);
		
		//�_B����_A�ւ̕��ʂ����߂āAA-B-C�łł���p�x�����߂�
		double dirBtoA = direction(latB,lngB,latA,lngA);
		double thetaB = theta(dirBtoA,dirBtoC);
		
		//A-C-B�łł���p�x
		double thetaC = 180 - thetaA - thetaB;
		
		//�ǂꂩ�̊p�x��0�ȉ��ŁA�O�p���ʂ��ł��Ȃ��Ƃ��Anull��Ԃ�
		if(thetaA <= 0 || thetaB<=0 || thetaC<=0){
			return null;
		}
		
		
		//�p�x�����W�A����
		thetaA = thetaA * Math.PI/180.0;
		thetaB = thetaB * Math.PI/180.0;
		thetaC = thetaC * Math.PI/180.0;
		
		//���ꂼ��̓_�Ԃ̋���
		double distAB = distance(latA,lngA,latB,lngB);
		double distBC = distAB * Math.sin(thetaA)/Math.sin(thetaC);
		double distAC = distAB * Math.sin(thetaB)/Math.sin(thetaC);
		
		//AC�Ԃ̋����Ɠ_A�̈ܓx�o�x����ɁA�_C�̈ܓx�o�x�����߂�
		double angleAtoC = (360 + 90 - dirAtoC)%360;  //���ʂ��p�x�ɕϊ�
		angleAtoC = angleAtoC * Math.PI/180.0; //�p�x�����W�A���ɕϊ�
		double latArad = latA * Math.PI/180.0; //�v�Z�̂��߂Ɉܓx�����W�A���ɕϊ�
		double latC = latA + (distAC*Math.sin(angleAtoC))/6378137 * 180/Math.PI;
		double lngC = lngA + (distAC*Math.cos(angleAtoC))/6378137/Math.cos(latArad) * 180/Math.PI;
		
		Point2D pointC = new Point2D.Double(); 
		pointC.setLocation(latC, lngC);
		return pointC;
	}
	
	
	
	
	
	/**
	 * ����2��^���āA���̊Ԃɂł���p�x�����߂�
	 * @param dir1 ����1
	 * @param dir2 ����2
	 * @return ����1�ƕ���2�̊Ԃɂł���p�x
	 */
	private static double theta(double dir1,double dir2){
		//����180�x�ȓ�
		if(Math.abs(dir1-dir2)<=180){
			return Math.abs(dir1-dir2);
		}
		//����180�x�ȏ�Ȃ�A0�x���܂����ł���
		else{
			if(dir1 < dir2){
				return dir1+(360-dir2);
			}else{
				return dir2+(360-dir1);
			}
		}
	}
	
	
	
	/**
	 * ��_����A���ʉ��x�ɉ����[�g���ړ��������_�̈ܓx�o�x�����߂�
	 * @param lat ��ƂȂ�ܓx
	 * @param lng ��ƂȂ�o�x
	 * @param dir �ړ����������
	 * @param dist �ړ������鋗��(���[�g��)
	 * @return
	 */
	public static Point2D pointByDirDist(double lat,double lng,double dir,double dist){
		//�k��0�Ƃ�����ʂ���xy���W�̊p�x�ɕϊ�
		double angle = (360.0 - dir + 90.0)%360.0;
		
		//x,y���̕ω��ʂ����߂�
		double dx = dist * Math.cos(angle);
		double dy = dist * Math.sin(angle);
		
		return pointByDiff(lat,lng,dx,dy);
	}
	
	
	
	/**
	 * ��_����x,y���Ɉړ��������Ƃ��̈ܓx�o�x�����߂�
	 * @param lat ��ƂȂ�ܓx
	 * @param lng ��ƂȂ�o�x
	 * @param dx x���̍���(���[�g��)
	 * @param dy y���̍���(���[�g��)
	 * @return �_(�ܓx,�o�x)
	 */
	public static Point2D pointByDiff(double lat,double lng,double dx,double dy){
		//�ܓx�o�x��360�x�@���烉�W�A���ɕϊ�
		double lat_rad = lat * Math.PI / 180.0;
		double lng_rad = lng * Math.PI / 180.0;
		
		
		//���߂�ܓx�o�x latT,lngT
		double lngT_rad = dx / Math.cos(lat_rad) / 6378137.0 + lng_rad;
		double latT_rad = dy / 6378137.0 + lat_rad;
		
		//���W�A������360�x�@�ɕϊ�
		double latT = latT_rad / Math.PI * 180.0;
		double lngT = lngT_rad / Math.PI * 180.0;
		
		return new Point2D.Double(latT,lngT);
	}
	
	

}
