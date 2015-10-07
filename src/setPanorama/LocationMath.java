package setPanorama;

import java.awt.geom.Point2D;

/**
 * ���W�֌W�̌v�Z���܂Ƃ߂�����
 * @author hamano
 *
 */
public class LocationMath {
	
	/**
	 * base�̈ʒu����target�̈ʒu�ւ̕���(y�����̌������)�����߂�
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
		angle = angle * 180 / Math.PI; //���W�A������360�x�@��
		double dir = (360.0 - angle + 90.0)%360.0;
		
		return dir;
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
	 * �O�p����
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
		
		//base1����base2�ւ̕��ʂ����߂āAbase2-base1-target�łł���p�xthetaA�����߂�
		double dirBase1toBase2 = direction(x_base1,y_base1,x_base2,y_base2);
		double thetaA = theta(dirBase1toBase2,dirBase1toTarget);
		
		//base2����base1�ւ̕��ʂ����߂āAbase1-base2-target�łł���p�xthetaB�����߂�
		double dirBase2toBase1 = direction(x_base2, y_base2, x_base1, y_base1);
		double thetaB = theta(dirBase2toBase1,dirBase2toTarget);
		
		
		System.out.println(dirBase1toBase2 + "       " + dirBase2toBase1);
		
		//base1-target-base2�łł���p�xthetaC�����߂�
		double thetaC = 180.0 - thetaA - thetaB;
		
		//�ǂꂩ�̊p�x��0�ȉ��ŁA�O�p���ʂ��ł��Ȃ��Ƃ��Anull��Ԃ�
		if(thetaA <= 0 || thetaB<=0 || thetaC<=0){
			System.out.println("�O�p���ʂ��ł��Ȃ�  " + thetaA + " , " + thetaB + " , " + thetaC);
			return null;
		}

		if(thetaA < 25 || thetaB < 25 || thetaC < 25 ){
			return null;
		}
		
		
		
		//�p�x�����W�A����
		thetaA = thetaA * Math.PI/180.0;
		thetaB = thetaB * Math.PI/180.0;
		thetaC = thetaC * Math.PI/180.0;
		
		
		//���ꂼ��̓_�Ԃ̋���		
		double distBase1Base2 = Math.sqrt( (x_base1-x_base2)*(x_base1-x_base2) + (y_base1-y_base2)*(y_base1-y_base2) );
		double distBase2Target = distBase1Base2 * Math.sin(thetaA)/Math.sin(thetaC);
		double distBase1Target = distBase1Base2 * Math.sin(thetaB)/Math.sin(thetaC);
		
		//base1��target�Ԃ̋�����base1�̈ʒu�����target�̈ʒu�����߂�
		double angle = (360 + 90 - dirBase1toTarget)%360;  //���ʂ��p�x�ɕϊ�
		angle = angle * Math.PI/180.0; //�p�x�����W�A���ɕϊ�
		double x_target = x_base1 + (distBase1Target * Math.cos(angle));
		double y_target = y_base1 + (distBase1Target * Math.sin(angle));
		
		Point2D target = new Point2D.Double(); 
		target.setLocation(x_target, y_target);
		System.out.println(x_target + "  ,  " + y_target);
		return target;
	}
	
	
	
}
