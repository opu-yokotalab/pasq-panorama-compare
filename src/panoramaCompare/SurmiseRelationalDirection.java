package panoramaCompare;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import mySurf.PairInterestPoints;

/**
 * �����֌W����
 * @author hamano
 *
 */
public class SurmiseRelationalDirection{
	private Integer pixelForward_BtoT=null,pixelBack_BtoT=null; //Base����Target�ւ̕����Ƌt�����̃s�N�Z��
	private Integer pixelForward_TtoB=null,pixelBack_TtoB=null; //Target����Base�ւ̕����Ƌt�����̃s�N�Z��
	private double[][] valueArray;
	private int num_cPair; //�����𖞂��������_�̐�
	
	private ArrayList<PairInterestPoints> allPairList; //�S�Ă̑Ή��t����������_
	private ArrayList<PairInterestPoints> rPairList;  //���Ή�������̑Ή��t����������_
	private int width_base,height_base; //base�摜�̕��ƍ���
	private int width_target,height_target; //target�摜�̕��ƍ���
	
	
	
	/**
	 * �R���X�g���N�^
	 * @param pairList
	 * @param anotherPairList
	 * @param imageBase
	 * @param imageTarget
	 */
	SurmiseRelationalDirection(ArrayList<PairInterestPoints> pairList,ArrayList<PairInterestPoints> anotherPairList,BufferedImage imageBase,BufferedImage imageTarget){
		this.width_base = imageBase.getWidth();
		this.height_base = imageBase.getHeight();
		this.width_target = imageTarget.getWidth();
		this.height_target = imageTarget.getHeight();
		this.allPairList = pairList;
		this.rPairList = new RemoveErrorMatching(allPairList,anotherPairList,imageBase,imageTarget).getPairList();
		surmiseRelation_fast();
		writefile();
	}
	
	
	
	/**
	 * �����֌W����
	 * surmiseRelation_faset ���v�Z�ʂ������B
	 * ���ʂ�surmiseRelation_fast�ƕς��Ȃ����߁A�v�Z�R�X�g���ႢsurmiseRelation_fast�𗘗p
	 * �폜���ėǂ�
	private void surmiseRelation(){
		valueArray = new double[720][720];
		int interval_base = width_base / 720;
		if(interval_base == 0) interval_base = 1;
		int interval_target = width_target / 720;
		if(interval_target == 0) interval_target = 1;
		
		
		int bestXF_base = 0,bestXB_base=0;
		int bestXF_target = 0,bestXB_target = 0;
		double bestRate = 0,bestDiff = 0 , bestValue = 0;
		int range=0;
		
		for(int i=0;i<width_base;i+=interval_base){
			int xF_BtoT = i;
			int xB_BtoT = (i + width_base/2)%width_base;
			
			for(int j=0;j<width_target;j+=interval_target){
				int xB_TtoB = j;
				int xF_TtoB = (j + width_target/2)%width_target;
				
				
				int numF=0,numB=0; //�O�i�����̈�E��ޕ����̈�ɂ���y�A��
				int numLarger=0,numSmaller=0;  //�Ԋu�������Ȃ��Ă���y�A���A�Ԋu���L���Ȃ��Ă���y�A��
				double sumDiff_F=0,sumDiff_B=0;
				
				//�O�i�����̈�ƌ�ޕ����̈�ɕ���
				for(int a=0;a<rPairList.size();a++){
					PairInterestPoints pair = rPairList.get(a);
					double x_base = pair.getPointBase().getX();
					double x_target = pair.getPointTarget().getX();
					
					//�O�i�����ƌ�ޕ����܂ł̍��������߂�
					double diffF_base = diffRate(xF_BtoT,x_base,width_base);
					double diffB_base = diffRate(xB_BtoT,x_base,width_base);
					
					//�O�i�����̈�ɂ���
					if(Math.abs(diffF_base) <= 0.25){
						numF++;
						double 	diffF_target = diffRate(xB_TtoB,x_target,width_target);
						if(Math.abs(diffF_base) <= Math.abs(diffF_target)){
							if( (diffF_base>=0 && diffF_target>=0) || (diffF_base<=0 && diffF_target<=0 ) ){
								numLarger++;
								sumDiff_F += Math.abs(diffF_target) - Math.abs(diffF_base);
							}
						}
					}
					//��ޕ����̈�ɂ���
					else if(Math.abs(diffB_base) <= 0.25){
						numB++;
						double diffB_target = diffRate(xF_TtoB,x_target,width_target);
						if(Math.abs(diffB_base) >= Math.abs(diffB_target)){
							if( (diffB_base>=0 && diffB_target>=0) || (diffB_base<=0 && diffB_target<=0 ) ){
								numSmaller++;
								sumDiff_B += Math.abs(diffB_target) - Math.abs(diffB_base);
							}

						}
					}else{
						//System.out.println("�z��O");
					}
				}
				
				
				//�őP�̒l��ێ�
				//double rate = (double)numLarger / (double)numF * (double)numSmaller / (double)numB;
				double rate = (double)(numLarger + numSmaller) / (double)(numF + numB);
				double diff = sumDiff_F - sumDiff_B;
				//double value = (double)numLarger/(double)numF * sumDiff_F + (double)numSmaller/(double)numB * Math.abs(sumDiff_B);
				double value = rate;
				//valueArray[i][j] = value;
				if(bestValue < value){
					bestValue = value;
					bestXF_base = xF_BtoT;
					bestXB_base = xB_BtoT;
					bestXB_target = xB_TtoB;
					bestXF_target = xF_TtoB;
					range=0;
				}
				else if(bestValue == value){
					range+=interval_base;
				}
				
			}
		}
		
		
		this.pixelForward_BtoT = bestXF_base;
		this.pixelBack_BtoT = bestXB_base;
		this.pixelForward_TtoB = bestXF_target;
		this.pixelBack_TtoB = bestXB_target;
	}
	*/
	
	
	
	
	
	
	private void surmiseRelation_fast(){
		int shiftMatch = shiftPixelForMatch(rPairList);
		int range=0;
		double rangeThreshold=0.25;
		
		double rate; //�����𖞂��������_�̊���
		int num_pair=0; //�����𖞂��������_�̐�
		
		int bestXF_base = 0,bestXF_target = 0;
		int bestXB_base = 0,bestXB_target = 0;
		double bestRate = 0,bestDiff = 0 , bestValue = 0;
		
		for(int i=0;i<360;i++){
			int xF_BtoT = (int) ((double)i/180.0*width_base)%width_base;
			int xB_BtoT = (xF_BtoT + width_base/2)%width_base;
			int xB_TtoB = (int) ((double)i/180.0*width_target + width_target + shiftMatch)%width_target;
			int xF_TtoB = (xB_TtoB + width_target/2)%width_target;

			
			int numF=0,numB=0; //�O�i�����̈�E��ޕ����̈�ɂ���y�A��
			int numLarger=0,numSmaller=0;  //�Ԋu�������Ȃ��Ă���y�A���A�Ԋu���L���Ȃ��Ă���y�A��
			double sumDiff_F=0,sumDiff_B=0;
			
			//�O�i�����̈�ƌ�ޕ����̈�ɕ���
			for(int a=0;a<rPairList.size();a++){
				PairInterestPoints pair = rPairList.get(a);
				double x_base = pair.getPointBase().getX();
				double x_target = pair.getPointTarget().getX();
				
				//�O�i�����ƌ�ޕ����܂ł̍��������߂�
				double diffF_base = diffRate(xF_BtoT,x_base,width_base);
				double diffB_base = diffRate(xB_BtoT,x_base,width_base);
				
				//�O�i�����̈�ɂ���
				if(Math.abs(diffF_base) <= rangeThreshold){
					numF++;
					double 	diffF_target = diffRate(xB_TtoB,x_target,width_target);
					if(Math.abs(diffF_base) <= Math.abs(diffF_target)){
						if( (diffF_base>=0 && diffF_target>=0) || (diffF_base<=0 && diffF_target<=0 ) ){
							numLarger++;
							sumDiff_F += Math.abs(diffF_target) - Math.abs(diffF_base);
						}
					}
				}
				//��ޕ����̈�ɂ���
				else if(Math.abs(diffB_base) <= rangeThreshold){
					numB++;
					double diffB_target = diffRate(xF_TtoB,x_target,width_target);
					if(Math.abs(diffB_base) >= Math.abs(diffB_target)){
						if( (diffB_base>=0 && diffB_target>=0) || (diffB_base<=0 && diffB_target<=0 ) ){
							numSmaller++;
							sumDiff_B += Math.abs(diffB_target) - Math.abs(diffB_base);
						}
					}
				}else{
					//System.out.println("�z��O");
				}
				
				
				//�őP�̒l��ێ�
				rate = (double)(numLarger + numSmaller) / (double)(rPairList.size());
				if(bestRate < rate){
					bestRate = rate;
					range=0;
					bestXF_base = xF_BtoT;
					bestXF_target = xF_TtoB;
					num_pair = numLarger + numSmaller;
				}
				else if(bestRate == rate){
					range++;
				}
			}
		}
				
		
		int interval_base = width_base / 720;
		if(interval_base == 0) interval_base = 1;
		int interval_target = width_target / 720;
		if(interval_target == 0) interval_target = 1;
		range = 0;
		
		int startXF_base = (int) ((width_base + bestXF_base - 15.0/360.0*width_base)%width_base);
		int startXF_target = (int) ((width_target + bestXF_target - 15.0/360.0*width_target)%width_target);
		for(int i=0;i<60;i++){
			int xF_BtoT = (startXF_base+interval_base*i)%width_base;
			int xB_BtoT = (xF_BtoT + width_base/2)%width_base;
			
			for(int j=0;j<60;j++){
				int xF_TtoB = (startXF_target+interval_target*j)%width_target;
				int xB_TtoB = (xF_TtoB + width_target/2)%width_target;

							
				int numF=0,numB=0; //�O�i�����̈�E��ޕ����̈�ɂ���y�A��
				int numLarger=0,numSmaller=0;  //�Ԋu�������Ȃ��Ă���y�A���A�Ԋu���L���Ȃ��Ă���y�A��
				double sumDiff_F=0,sumDiff_B=0;
				
				//�O�i�����̈�ƌ�ޕ����̈�ɕ���
				for(int a=0;a<rPairList.size();a++){
					PairInterestPoints pair = rPairList.get(a);
					double x_base = pair.getPointBase().getX();
					double x_target = pair.getPointTarget().getX();
					
					//�O�i�����ƌ�ޕ����܂ł̍��������߂�
					double diffF_base = diffRate(xF_BtoT,x_base,width_base);
					double diffB_base = diffRate(xB_BtoT,x_base,width_base);
					
					//�O�i�����̈�ɂ���
					if(Math.abs(diffF_base) <= rangeThreshold){
						numF++;
						double 	diffF_target = diffRate(xB_TtoB,x_target,width_target);
						if(Math.abs(diffF_base) <= Math.abs(diffF_target)){
							if( (diffF_base>=0 && diffF_target>=0) || (diffF_base<=0 && diffF_target<=0 ) ){
								numLarger++;
								sumDiff_F += Math.abs(diffF_target) - Math.abs(diffF_base);
							}
						}
					}
					//��ޕ����̈�ɂ���
					else if(Math.abs(diffB_base) <= rangeThreshold){
						numB++;
						double diffB_target = diffRate(xF_TtoB,x_target,width_target);
						if(Math.abs(diffB_base) >= Math.abs(diffB_target)){
							if( (diffB_base>=0 && diffB_target>=0) || (diffB_base<=0 && diffB_target<=0 ) ){
								numSmaller++;
								sumDiff_B += Math.abs(diffB_target) - Math.abs(diffB_base);
							}

						}
					}else{
						//System.out.println("�z��O");
					}
				}
				
				
				//�őP�̒l��ێ�
				rate = (double)(numLarger + numSmaller) / (double)(rPairList.size());
				double value = rate;
				//valueArray[i][j] = value;
				if(bestValue < value){
					bestValue = value;
					bestXF_base = xF_BtoT;
					bestXB_base = xB_BtoT;
					bestXB_target = xB_TtoB;
					bestXF_target = xF_TtoB;
					range=0;
					num_pair = numLarger + numSmaller;
				}
				else if(bestValue == value){
					range+=interval_base;
				}
				
			}
		}
		
		
		this.pixelForward_BtoT = bestXF_base;
		this.pixelBack_BtoT = bestXB_base;
		this.pixelForward_TtoB = bestXF_target;
		this.pixelBack_TtoB = bestXB_target;
		this.num_cPair = num_pair;
		System.out.println("value:"+bestValue+ "   FB:" + bestXF_base + "   FT:" + bestXF_target);
	}
	
	/*-- matsuba --*/
	private void surmiseRelation_fast2(){
		int shiftMatch=shiftPixelForMatch(rPairList);
		int bestForward=0;
		int bestBack=0;
		int numPair=0;
		double BestRate=0.0;
		for(int i=0;i<360;i++){
			int numLarge=0;
			int numSmall=0;
			double rate;
			int bf_range=(int)((double)i/360.0*width_base)%width_base;
			int bb_range=(bf_range+width_base/2)%width_base;
			int tf_range=(int)((double)i/360.0*width_target+shiftMatch)%width_target;
			int tb_range=(tf_range+width_target/2)%width_target;
			//System.out.println(i+" - "+bf_range+" - "+bb_range+" - "+tf_range+" - "+tb_range);
			for(int pairs=0;pairs<rPairList.size();pairs++){
				PairInterestPoints pair=rPairList.get(pairs);
				double x_base=pair.getPointBase().getX();
				double x_target=pair.getPointTarget().getX();
				double y_base=pair.getPointBase().getY();
				double y_target=pair.getPointTarget().getY();
				double bf_diff=diffRate(bf_range,x_base,width_base);
				double tb_diff=diffRate(tb_range,x_target,width_target);
				if(Math.abs(bf_diff)<0.25){
					double tf_diff=diffRate(tf_range,x_target,width_target);
					if(Math.abs(bf_diff)<Math.abs(tf_diff)){
						if(Math.abs(y_base-height_base/2)<Math.abs(y_target-height_target/2)){
							if(bf_diff*tf_diff>=0.0){
								numLarge++;
							}
						}
					}
				}
				if(Math.abs(tb_diff)<0.25){
					double bb_diff=diffRate(bb_range,x_base,width_base);
					if(Math.abs(tb_diff)<Math.abs(bb_diff)){
						if(Math.abs(y_target-height_target/2)<Math.abs(y_base-height_base/2)){
							if(tb_diff*bb_diff>=0.0){
								numSmall++;
							}
						}
					}
				}
				//System.out.println(rate+" - "+BestRate);
				rate=(double)(numLarge+numSmall)/(double)(rPairList.size());
				if(rate>BestRate){
					BestRate=rate;
					bestForward=bf_range;
					bestBack=bb_range;
					numPair=numLarge+numSmall;
				}
			}
		}
		this.pixelForward_BtoT = bestForward;
		this.pixelBack_BtoT = bestBack;
		this.pixelForward_TtoB = bestBack;
		this.pixelBack_TtoB = bestForward;
		this.num_cPair = numPair;
	}
	
	private void writefile(){
		String outfile="outfile.txt";
		try{
			FileWriter fw = new FileWriter(outfile,true);
			
			fw.write(this.getPixelForward_BtoT()+" "+this.getPixelForward_TtoB()+"\r\n");
			fw.close();
		}catch(IOException e){
			System.out.println("file error.\n");
			System.exit(0);
		}
	}
	
	/**
	 * ���ꂼ��̃p�m���}�摜�̎B�e�n�_�����񂾒�����̕�����
	 * �p�m���}�摜���ɂ����Ĉ�v������ɂ́A
	 * �ǂꂭ�炢�̃s�N�Z�����V�t�g������K�v�����邩���߂�
	 * @param list
	 * @return
	 */
	private int shiftPixelForMatch(ArrayList<PairInterestPoints> list){
		int interval = width_target / 360;
		if(interval==0) interval = 1;
		
		int shift_best = 0;
		double sum_min = Double.MAX_VALUE;
		for(int s=0;s<width_target;s+=interval){
			int shift = (width_target + s)%width_target;
			double sum = 0;
			for(int i=0;i<list.size();i++){
				PairInterestPoints pair = list.get(i);
				double x_base = pair.getPointBase().getX();
				double x_target = (width_target + pair.getPointTarget().getX() - (double)shift)%(double)width_target;
				
				
				double xr_base = x_base / (double)width_base;
				double xr_target = x_target / (double)width_target;
				double diff = PanoramaImage.diffRate(xr_base, xr_target);
				
				sum += Math.abs(diff);
			}
						
			//�őP�̒l��ێ�
			if(sum < sum_min){
				sum_min = sum;
				shift_best = shift;
			}
		}
		//System.out.println("shift:"+shift_best);
		return shift_best;
	}
	
	
	
	int getNumAroundDirPair(){
		double threshold_base = 0.11d;
		double threshold_target = 0.09d;
		//imageBase�ɂ�����base����target�����̎��͂̓����_���擾
		ArrayList<PairInterestPoints> aroundBtoT = new ArrayList<PairInterestPoints>();
		//imageTarget�ɂ�����target����base�����̎��͂̓����_���擾
		ArrayList<PairInterestPoints> aroundTtoB = new ArrayList<PairInterestPoints>();
		for(int i=0;i<rPairList.size();i++){
			PairInterestPoints pair = rPairList.get(i);
			double x_base = pair.getPointBase().getX();
			double x_target = pair.getPointTarget().getX();
			
			////imageBase�ɂ����āAbase����target�����̎��͂̓����_���擾
			if( Math.abs(diffRate(pixelForward_BtoT,x_base,width_base)) < threshold_base ){
				aroundBtoT.add(pair);
			}
			////imageTarget�ɂ����āAtarget����base�����̎��͂̓����_���擾
			else if( Math.abs(diffRate(pixelForward_TtoB,x_target,width_target)) < threshold_target ){
				aroundTtoB.add(pair);
			}
		}
		
		return aroundBtoT.size()+aroundTtoB.size();
	}
	
	
	public int getNumAllPair(){
		return rPairList.size();
	}
		
	
	public int getNumFillConditionPair(){
		return num_cPair;
	}
	
	
	/**
	 * �p�m���}�摜�ɂ�����2�_��x���W�̍��������߁A�����猩���䗦�ŕ\���B
	 * �p�m���}�摜�ł��邽�߁A�[���܂������ꍇ�Ƃ����łȂ��ꍇ���r���A�߂�������̍����̃p�m���}�摜�S�̂��猩���䗦��Ԃ��B
	 * @param x_base ��ƂȂ�x���W
	 * @param x_target ��r�ƂȂ�x���W
	 * @param width_image �p�m���}�摜�̕�
	 * @return ����(x_base��荶���ł���Ε��Ax_base���E���ł���ΐ�)
	 */
	private double diffRate(double x_base, double x_target,int width_image){
		double diff;			
		////�[���܂����Ȃ�����
		double diff1 = x_base - x_target;
		////�[���܂�������
		double diff2;
		if(x_base > x_target){
			diff2 = width_image - x_base + x_target;
		}else{
			diff2 = -(width_image - x_target + x_base);
		}
		
		if(Math.abs(diff1) < Math.abs(diff2)) diff = diff1;
		else diff = diff2;
		
		return diff/(double)width_image;
	}
	

	
	
	
	
	public int getPixelForward_BtoT(){
		return this.pixelForward_BtoT;
	}
	public int getPixelBack_BtoT(){
		return this.pixelBack_BtoT;
	}
	public int getPixelForward_TtoB(){
		return this.pixelForward_TtoB;
	}
	public int getPixelBack_TtoB(){
		return this.pixelBack_TtoB;
	}
	/**
	 * base����target�ւ̕��ʂ�Ԃ�(�摜�̍��[��0�A�E�[��360�Ƃ��鎞�v���̕���)
	 * @return
	 */
	public double getDirBtoT(){
		return (double)pixelForward_BtoT/(double)width_base*360.0;
	}
	/**
	 * target����base�ւ̕��ʂ�Ԃ�(�摜�̍��[��0�A�E�[��360�Ƃ��鎞�v���̕���)
	 * @return
	 */
	public double getDirTtoB(){
		return (double)pixelForward_TtoB/(double)width_target*360.0;
	}
	public ArrayList<PairInterestPoints> getPairList(){
		return this.rPairList;
	}
	public ArrayList<PairInterestPoints> getAllPairList(){
		return this.allPairList;
	}
}
