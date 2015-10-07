package panoramaCompare;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mySurf.PairInterestPoints;

/**
 * パノラマ画像の比較において、誤り対応、もしくはその可能性がある対応付き特徴点を削除する
 * @author hamano
 *
 */
public class RemoveErrorMatching {
	private int width_base,height_base;
	private int width_target,height_target;
	private ArrayList<PairInterestPoints> list_base;
	private ArrayList<PairInterestPoints> list_target;



	public RemoveErrorMatching(ArrayList<PairInterestPoints> ipList , int width_base,int height_base,int width_target,int height_target ){
		this.list_base = ipList;
		this.width_base = width_base;
		this.height_base = height_base;
		this.width_target = width_target;
		this.height_target = height_target;
		removeError();
	}

	public RemoveErrorMatching(ArrayList<PairInterestPoints> ipList,BufferedImage imageBase,BufferedImage imageTarget) {
		this.list_base = ipList;
		this.width_base = imageBase.getWidth();
		this.height_base = imageBase.getHeight();
		this.width_target = imageTarget.getWidth();
		this.height_target = imageTarget.getHeight();
		removeError();
	}

	public RemoveErrorMatching(ArrayList<PairInterestPoints> ipList_base,ArrayList<PairInterestPoints> ipList_target,BufferedImage imageBase, BufferedImage imageTarget) {
		this.list_base = ipList_base;
		this.list_target = ipList_target;
		this.width_base = imageBase.getWidth();
		this.height_base = imageBase.getHeight();
		this.width_target = imageTarget.getWidth();
		this.height_target = imageTarget.getHeight();
		removeError();
	}



	private void removeError(){

		//共通する特徴点だけを残す
		//removeUncommonPair();

		//大きく外れた変化をしている特徴点を除去
		//removeLargeShift();

		//交差する特徴点を除去
		//removeCross();

		//交差する特徴点を多いものから順に交差がなくなるまで除去 removeCrossとほぼ同じ
		//by matuba
		//removeAllCross(0);
		
		//遠方であると思われる特徴点を除去
		//removeFarawayMatching();

		//ただのソート(margeじゃない)and表示
		//by matsuba
		//MargeList(list_base);
		//removeAbnomalSlope(list_base);
		
		//RANdom SAmple Consensus を用いた除去
		//by matsuba
		RANSAC();
		
		//最小メジアン法を用いた除去
		//by matsuba
		//LMedS();

	}



	/**
	 * 共通する特徴点だけを残す
	 */
    public void removeUncommonPair(){
    	ArrayList<PairInterestPoints> commonPairList = new ArrayList<PairInterestPoints>();
    	for(int i=0;i<list_base.size();i++){
    		for(int j=0;j<list_target.size();j++){
    			double x1_A = list_base.get(i).getPointBase().getX();
    			double y1_A = list_base.get(i).getPointBase().getY();
    			double x1_B = list_target.get(j).getPointTarget().getX();
    			double y1_B = list_target.get(j).getPointTarget().getY();

    			double x2_A = list_base.get(i).getPointTarget().getX();
    			double y2_A = list_base.get(i).getPointTarget().getY();
    			double x2_B = list_target.get(j).getPointBase().getX();
    			double y2_B = list_target.get(j).getPointBase().getY();


    			if(x1_A == x1_B && y1_A == y1_B && x2_A == x2_B && y2_A == y2_B){
    				commonPairList.add(list_base.get(i));
    			}

    		}
    	}

    	this.list_base =  commonPairList;
    }



	/**
	 * 明らかに誤り対応なものを削除する
	 * 特徴点のシフトが最小のとき、パノラマ画像の幅の4分の1を超えるシフトはありえないので、4分の1を超えるシフトをするペアを削除
	 */
	private void removeLargeShift(){
		double threshold = 0.25;  //本当は0.25であるべきだが、0.20超えている時点で推測に適さないため、0.20にしている
		int shift = matchShift();
		ArrayList<PairInterestPoints> tempList = new ArrayList<PairInterestPoints>(list_base);

		for(PairInterestPoints pair : list_base){
			double xr_base = pair.getPointBase().getX() / width_base;
			double x_target = (width_target + pair.getPointTarget().getX() - shift)%width_target;
			double xr_target = x_target / width_target;

			double diffRate = PanoramaImage.diffRate(xr_base, xr_target);
			if(Math.abs(diffRate) > threshold){
				tempList.remove(pair);
			}
		}

		this.list_base = tempList;
	}




	/**
	 * どれだけtarget側をシフトさせれば、比較しやすい状態(baseとtargetの差が最小になる状態)になるかを求める
	 * @param pairList
	 * @param x
	 * @param width
	 * @return
	 */
	private int matchShift(){
		int interval = width_target / 720;
		if(interval==0) interval = 1;

		int shift_best = 0;
		double sum_min = Double.MAX_VALUE;
		for(int s=0;s<width_target;s+=interval){
			int shift = (width_target + s)%width_target;
			double sum = 0;
			for(int i=0;i<list_base.size();i++){
				PairInterestPoints pair = list_base.get(i);
				double x_base = pair.getPointBase().getX();
				double x_target = (width_target + pair.getPointTarget().getX() - (double)shift)%(double)width_target;


				double xr_base = x_base / (double)width_base;
				double xr_target = x_target / (double)width_target;
				double diff = PanoramaImage.diffRate(xr_base, xr_target);

				sum += Math.abs(diff);
			}

			//最善の値を保持
			if(sum < sum_min){
				sum_min = sum;
				shift_best = shift;
			}
		}

		return shift_best;
	}




	/**
	 * 他の対応付き特徴点と交差する対応付き特徴点は、推測に不適当として除去する
	 */
	private void removeCross(){
		ArrayList<PairInterestPoints> removedList = new ArrayList<PairInterestPoints>(list_base);

		//全ての組み合わせを見て、比較元と対象画像でそれぞれの特徴点がクロスしているものを探す
		ArrayList<CouplePairs> errorCoupleList = new ArrayList<CouplePairs>();
		for(int i=0;i<list_base.size();i++){
			for(int j=i+1;j<list_base.size();j++){
				//特徴点の組み合わせ作成
				PairInterestPoints pair1 = list_base.get(i);
				PairInterestPoints pair2 = list_base.get(j);
				CouplePairs cp = new CouplePairs(pair1,pair2);

				//if(cp.flagReverse_x()){
				if(cp.cross(width_base,width_target) ){
					errorCoupleList.add(cp);
				}
			}
		}


		// 比較元と対象画像で特徴点がクロスしている組み合わせの中で、
		//  多く含まれるペアは誤り対応であるとし、除去する
		while(errorCoupleList.size() > 0){
			//組み合わせに含まれるペアをカウント@
			Map<PairInterestPoints, Integer> map = new HashMap<PairInterestPoints, Integer>();
			for(CouplePairs errorCouple : errorCoupleList){
				PairInterestPoints pair1 = errorCouple.getPair1();
				PairInterestPoints pair2 = errorCouple.getPair2();
				if(map.containsKey(pair1)){
					Integer count = (Integer) map.get(pair1) + 1 ;
					map.put(pair1, count);
				}else{
					map.put(pair1, 1);
				}

				if(map.containsKey(pair2)){
					Integer count = (Integer) map.get(pair2) + 1 ;
					map.put(pair2,count);
				}else{
					map.put(pair2, 1);
				}
			}

			//組み合わせに多く含まれるペアを取得
			Iterator<PairInterestPoints> iterator = map.keySet().iterator();
			PairInterestPoints pair;
			int count_most = 0;
			PairInterestPoints pair_most = null;
			while(iterator.hasNext()){
				 pair = iterator.next();
				 int count = map.get(pair);
				 if(count > count_most){
					 count_most = count;
					 pair_most = pair;
				 }
			}

			// ペアを削除
			if(count_most > 1){
				//除去するペアを含む組み合わせを除去
				ArrayList<CouplePairs> errorCoupleList_temp = new ArrayList<CouplePairs>(errorCoupleList);
				for(CouplePairs couple:errorCoupleList){
					if(couple.contain(pair_most)){
						errorCoupleList_temp.remove(couple);
					}
				}
				errorCoupleList = errorCoupleList_temp;

				//誤り対応のペアを削除
				if(removedList.contains(pair_most)){
					removedList.remove(pair_most);
				}
			}
			//count_mostが1、つまり交差するのが1つの組み合わせでしかないなら、両方のペアを誤りとする
			else{
				ArrayList<CouplePairs> errorCoupleList_temp = new ArrayList<CouplePairs>(errorCoupleList);
				for(CouplePairs couple : errorCoupleList){
					if(couple.contain(pair_most)){
						//差があまりに小さいときは、誤りとしない
						if(Math.abs(couple.getDx_base()/(double)width_base) < 0.005 && Math.abs(couple.getDx_target()/(double)width_target) < 0.005){
							errorCoupleList_temp.remove(couple);
						}
						else{
							PairInterestPoints pair1 = couple.getPair1();
							PairInterestPoints pair2 = couple.getPair2();
							removedList.remove(pair1);
							removedList.remove(pair2);
							errorCoupleList_temp.remove(couple);
						}

					}
				}
				errorCoupleList = errorCoupleList_temp;
			}
		}

		this.list_base = removedList;
	}

	/*---------------------- ------- ----------------------*/
	/*---------------------- matsuba ----------------------*/
	/*---------------------- ------- ----------------------*/

	private int numMax(int[] numlist,int listsize){
		int max=0;
		for(int i=0;i<listsize;i++){
			if(max<numlist[i]){
				max=numlist[i];
			}
		}
		return max;
	}

	private double numMaxD(double[] numlist,int listsize){
		double max=0;
		for(int i=0;i<listsize;i++){
			if(max<numlist[i]){
				max=numlist[i];
			}
		}
		return max;
	}

	private void removeAllCross(int threshold){
		int[] numCross = new int[list_base.size()];
		ArrayList<PairInterestPoints> returnList = new ArrayList<PairInterestPoints>(list_base);
		ArrayList<CouplePairs> errorCoupleList = new ArrayList<CouplePairs>();
		for(int i=0;i<list_base.size();i++){
			numCross[i]=0;
		}
		for(int i=0;i<list_base.size();i++){
			PairInterestPoints pair1=list_base.get(i);
			for(int j=i+1;j<list_base.size();j++){
				PairInterestPoints pair2=list_base.get(j);
				CouplePairs pairs = new CouplePairs(pair1,pair2);
				if(pairs.cross(width_base,width_target/*,height_base,height_target*/)){
					numCross[i]++;
					numCross[j]++;
					errorCoupleList.add(pairs);
				}
			}
		}
		while(numMax(numCross,list_base.size())>threshold){
			int maxCross=numMax(numCross,list_base.size());
			for(int i=0;i<list_base.size();i++){
				if(numCross[i]==maxCross){
					for(int j=0;j<list_base.size();j++){
						PairInterestPoints pair1=list_base.get(i);
						PairInterestPoints pair2=list_base.get(j);
						if(i>j){
							pair1=list_base.get(j);
							pair2=list_base.get(i);
						}
						CouplePairs pairs = new CouplePairs(pair1,pair2);
						for(int k=0;k<errorCoupleList.size();k++){
							if(errorCoupleList.get(k).getPair1()==pair1&&errorCoupleList.get(k).getPair2()==pair2){
								numCross[j]--;
								errorCoupleList.remove(pairs);
							}
						}
					}
					numCross[i]=0;
					returnList.remove(list_base.get(i));
				}
			}
		}
		this.list_base = returnList;
	}

	private void MargeList(ArrayList<PairInterestPoints> mList){
		ArrayList<PairInterestPoints> returnList = new ArrayList<PairInterestPoints>();
		int size=mList.size();
		for(int i=0;i<size;i++){
			int numList=0;
			double min=mList.get(0).getPointBase().getX();
			for(int j=1;j<size-i;j++){
				double x=mList.get(j).getPointBase().getX();
				if(x<min){
					min=x;
					numList=j;
				}
			}
			returnList.add(mList.get(numList));
			mList.remove(numList);
		}
		//for(int i=0;i<size;i++){
		//	System.out.println(returnList.get(i).getPointBase().getX()+" "+slopeValue(returnList.get(i),0));
		//}
		//System.out.println(returnList.size()+" - "+list_base.size());
		this.list_base=returnList;
	}
	
	private void removeFarawayMatching(){
		ArrayList<PairInterestPoints> returnList = new ArrayList<PairInterestPoints>(list_base);
		double threshold=5.0;
		for(int i=0;i<list_base.size();i++){
			double base_y=list_base.get(i).getPointBase().getY();
			double target_y=list_base.get(i).getPointTarget().getY();
			if(Math.abs(base_y-target_y)<threshold){
				returnList.remove(list_base.get(i));
			}
		}
		this.list_base=returnList;
	}

	private double f(double x,double a,double b,double c){
		return a*Math.sin(Math.PI*2.0/(double)width_base*x+b)+c;
		//return a*Math.abs(1.0-2.0*Math.abs(x-b)/(double)width_base)+c;
	}

	private double drda(double x,double a,double b,double c){
		return -1.0*Math.sin(Math.PI*2.0/(double)width_base*x+b);
		//return -1.0*Math.abs(1.0-2.0*Math.abs(x-b)/(double)width_base);
	}

	private double drdb(double x,double a,double b,double c){
		return -1.0*a*Math.cos(Math.PI*2.0/(double)width_base*x+b);
	/*	if(Math.abs(x-b)<(double)width_base/2.0){
			if(x>b){
				return -2.0*a/(double)width_base;
			}
			else{
				return 2.0*a/(double)width_base;
			}
		}
		else{
			if(x>b){
				return 2.0*a/(double)width_base;
			}
			else{
				return -2.0*a/(double)width_base;
			}
		}*/
	}

	private double drdc(double x,double a,double b,double c){
		return -1.0;
	}

	private double r(double x,double y,double a,double b,double c){
		return y-f(x,a,b,c);
	}

	private double[] initial(double x[],double y[],int size){
		int timesA=10;
		int timesB=10;
		int timesC=10;
		double initialA=0.1;
		double initialB=0.0;
		double initialC=-30.0;
		double maxA=6.0;
		double maxB=2.0*Math.PI;
		double maxC=30.0;
		double sumMin=1000.0;
		double[] returnABC=new double[3];
		returnABC[0]=initialA;
		returnABC[1]=initialB;
		returnABC[2]=initialC;
		for(int i=0;i<=timesA;i++){
			for(int j=0;j<=timesB;j++){
				for(int k=0;k<=timesC;k++){
					double sum=0.0;
					for(int l=0;l<size;l++){
						double thisr=r(x[l],y[l],initialA+(maxA-initialA)*((double)i/(double)timesA),initialB+(maxB-initialB)*((double)j/(double)timesB),initialC+(maxC-initialC)*((double)k/(double)timesC));
						sum+=thisr*thisr;
					}
					if(sum<sumMin){
						sumMin=sum;
						returnABC[0]=initialA+(maxA-initialA)*((double)i/(double)timesA);
						returnABC[1]=initialB+(maxB-initialB)*((double)j/(double)timesB);
						returnABC[2]=initialC+(maxC-initialC)*((double)k/(double)timesC);
					}
				}

			}
		}
		//System.out.println("end initial");
		return returnABC;
	}

	private double[] newton(double a,double b,double c,double x[],double y[],int size){
		int times=30;
		double[] returnABC=new double[3];
		returnABC[0]=a;
		returnABC[1]=b;
		returnABC[2]=c;
		for(int i=0;i<times;i++){
			double[] matrixX={0.0,0.0,0.0};
			double[] mY={0.0,0.0,0.0};
			double[][] m={{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}};
			for(int j=0;j<size;j++){
				double this_drda=drda(x[j],returnABC[0],returnABC[1],returnABC[2]);
				double this_drdb=drdb(x[j],returnABC[0],returnABC[1],returnABC[2]);
				double this_drdc=drdc(x[j],returnABC[0],returnABC[1],returnABC[2]);
				double this_r=r(x[j],y[j],returnABC[0],returnABC[1],returnABC[2]);
				m[0][0]+=this_drda*this_drda;
				m[0][1]+=this_drda*this_drdb;
				m[0][2]+=this_drda*this_drdc;
				m[1][0]+=this_drdb*this_drda;
				m[1][1]+=this_drdb*this_drdb;
				m[1][2]+=this_drdb*this_drdc;
				m[2][0]+=this_drdc*this_drda;
				m[2][1]+=this_drdc*this_drdb;
				m[2][2]+=this_drdc*this_drdc;
				mY[0]+=this_r*this_drda;
				mY[1]+=this_r*this_drdb;
				mY[2]+=this_r*this_drdc;
			}
			matrixX[0]=(mY[0]*m[1][1]*m[2][2]+mY[1]*m[1][2]*m[2][0]+mY[2]*m[1][0]*m[2][1]-mY[0]*m[1][2]*m[2][1]-mY[1]*m[1][0]*m[2][2]-mY[2]*m[1][1]*m[2][0]);
			matrixX[1]=(mY[1]*m[0][0]*m[2][2]+mY[2]*m[0][1]*m[2][0]+mY[0]*m[0][2]*m[2][1]-mY[2]*m[0][0]*m[2][1]-mY[0]*m[0][1]*m[2][2]-mY[1]*m[0][2]*m[2][0]);
			matrixX[2]=(mY[2]*m[0][0]*m[1][1]+mY[0]*m[0][1]*m[1][2]+mY[1]*m[0][2]*m[1][0]-mY[1]*m[0][0]*m[1][2]-mY[2]*m[0][1]*m[1][0]-mY[0]*m[0][2]*m[1][1]);
			matrixX[0]/=(m[0][0]*m[1][1]*m[2][2]+m[0][1]*m[1][2]*m[2][0]+m[0][2]*m[1][0]*m[2][1]-m[0][0]*m[1][2]*m[2][1]-m[0][1]*m[1][0]*m[2][2]-m[0][2]*m[1][1]*m[2][0]);
			matrixX[1]/=(m[0][0]*m[1][1]*m[2][2]+m[0][1]*m[1][2]*m[2][0]+m[0][2]*m[1][0]*m[2][1]-m[0][0]*m[1][2]*m[2][1]-m[0][1]*m[1][0]*m[2][2]-m[0][2]*m[1][1]*m[2][0]);
			matrixX[2]/=(m[0][0]*m[1][1]*m[2][2]+m[0][1]*m[1][2]*m[2][0]+m[0][2]*m[1][0]*m[2][1]-m[0][0]*m[1][2]*m[2][1]-m[0][1]*m[1][0]*m[2][2]-m[0][2]*m[1][1]*m[2][0]);
			//if(Math.abs(returnABC[0]-matrixX[0])>0.03){
				returnABC[0]-=matrixX[0];
			//}
			returnABC[1]-=matrixX[1];
			returnABC[2]-=matrixX[2];
		}
		//System.out.println("end newton");
		return returnABC;
	}

	private void RANSAC(){
		ArrayList<PairInterestPoints> List = new ArrayList<PairInterestPoints>(list_base);
		int times=10000;
		int usingValueNum=4;
		int bestValue=0;
		double bestA=0.0;
		double bestB=0.0;
		double bestC=0.0;
		double judgeThreshold=0.9;
		double functionThreshold=0.9;
		double[] x=new double[usingValueNum];
		double[] y=new double[usingValueNum];
		ArrayList<Integer> numList=new ArrayList<Integer>();
		for(int i=0;i<list_base.size();i++){
			numList.add(i);
		}
		for(int i=0;i<times;i++){
			int count=0;
			double[] useABC={0.0,0.0,0.0};
			Collections.shuffle(numList);
			for(int j=0;j<usingValueNum;j++){
				x[j]=list_base.get(numList.get(j)).getPointBase().getX();
				y[j]=slopeValue(list_base.get(numList.get(j)),0);
			}
			useABC=initial(x,y,usingValueNum);
			useABC=newton(useABC[0],useABC[1],useABC[2],x,y,usingValueNum);
			for(int j=0;j<list_base.size();j++){
				double value=r(list_base.get(j).getPointBase().getX(),slopeValue(list_base.get(j),0),useABC[0],useABC[1],useABC[2]);
				if(Math.abs(value)<judgeThreshold){
					//if(Math.abs(value-(useABC[2]+useABC[0]/2.0))>0.0005)
						count++;
				}
			}
			if(count>bestValue){
				bestValue=count;
				bestA=useABC[0];
				bestB=useABC[1];
				bestC=useABC[2];
			}
			//System.out.println(i+":"+bestA+","+bestB+","+bestC);
			//System.out.println(i+":"+useABC[0]+","+useABC[1]+","+useABC[2]);
			//System.out.println("["+count+":"+bestValue+"]");
			//System.out.println(numList.get(0)+","+numList.get(1)+","+numList.get(2)+","+numList.get(3)+","+numList.get(4));
			//System.out.println(x[0]+","+x[1]+","+x[2]+","+x[3]+","+x[4]);
			//System.out.println(y[0]+","+y[1]+","+y[2]+","+y[3]+","+y[4]);
		}
		for(int i=0;i<list_base.size();i++){
			if(Math.abs(r(list_base.get(i).getPointBase().getX(),slopeValue(list_base.get(i),0),bestA,bestB,bestC))>functionThreshold){
			//if(slopeValue(list_base.get(i),0)>f(list_base.get(i).getPointBase().getX(),bestA+functionThreshold,bestB,bestC+functionThreshold)||slopeValue(list_base.get(i),0)<f(list_base.get(i).getPointBase().getX(),bestA+functionThreshold,bestB,bestC-functionThreshold)){
				List.remove(list_base.get(i));
			}
		}
		//System.out.println(bestA+","+bestB+","+bestC+":"+List.size());
		this.list_base=List;
	}
	
	private void LMedS(){
		ArrayList<PairInterestPoints> List = new ArrayList<PairInterestPoints>(list_base);
		int times=10000;
		int usingValueNum=4;
		double bestValue=10000.0;
		double bestA=0.0;
		double bestB=0.0;
		double bestC=0.0;
		double judgeThreshold=0.0;
		double functionThreshold=0.015;
		double[] x=new double[usingValueNum];
		double[] y=new double[usingValueNum];
		ArrayList<Integer> numList=new ArrayList<Integer>();
		for(int i=0;i<list_base.size();i++){
			numList.add(i);
		}
		for(int i=0;i<times;i++){
			double[] useABC={0.0,0.0,0.0};
			ArrayList<Double> mseList=new ArrayList<Double>();
			Collections.shuffle(numList);
			for(int j=0;j<usingValueNum;j++){
				x[j]=list_base.get(numList.get(j)).getPointBase().getX();
				y[j]=slopeValue(list_base.get(numList.get(j)),0);
			}
			useABC=initial(x,y,usingValueNum);
			useABC=newton(useABC[0],useABC[1],useABC[2],x,y,usingValueNum);
			for(int j=0;j<list_base.size();j++){
				double value=r(list_base.get(j).getPointBase().getX(),slopeValue(list_base.get(j),0),useABC[0],useABC[1],useABC[2]);
				mseList.add(value*value);
			}
			Collections.sort(mseList);
			if(mseList.get(list_base.size()/2)<bestValue){
				bestValue=mseList.get(list_base.size()/2);
				bestA=useABC[0];
				bestB=useABC[1];
				bestC=useABC[2];
				judgeThreshold=Math.sqrt(mseList.get(list_base.size()*4/10));
			}
			//System.out.println(i+":"+bestA+","+bestB+","+bestC);
			//System.out.println(i+":"+useABC[0]+","+useABC[1]+","+useABC[2]);
			//System.out.println("["+count+":"+bestValue+"]");
			//System.out.println(numList.get(0)+","+numList.get(1)+","+numList.get(2)+","+numList.get(3)+","+numList.get(4));
			//System.out.println(x[0]+","+x[1]+","+x[2]+","+x[3]+","+x[4]);
			//System.out.println(y[0]+","+y[1]+","+y[2]+","+y[3]+","+y[4]);
		}
		for(int i=0;i<list_base.size();i++){
			//if(Math.abs(r(list_base.get(i).getPointBase().getX(),slopeValue(list_base.get(i),0),bestA,bestB,bestC))>functionThreshold){
			if(Math.abs(r(list_base.get(i).getPointBase().getX(),slopeValue(list_base.get(i),0),bestA,bestB,bestC))>judgeThreshold){
				List.remove(list_base.get(i));
			}
		}
		System.out.println(bestA+","+bestB+","+bestC);
		this.list_base=List;
	}

	private double slopeValue(PairInterestPoints pair,int shift){
		double base_x=pair.getPointBase().getX()/(double)width_base;
		double base_y=pair.getPointBase().getY()/(double)height_base+1.0;
		double target_x=((pair.getPointTarget().getX()+shift)%width_target)/(double)width_target;
		double target_y=pair.getPointTarget().getY()/(double)height_target;
		if(Math.abs(base_x-target_x)>0.5){
			if(base_x<0.5){
				target_x-=1.0;
			}
			else{
				base_x-=1.0;
			}
		}
		double value=Math.atan(-1.0*(base_x-target_x)/(base_y-target_y));
		value=(value+Math.PI/2.0)/Math.PI*180.0;
		return value;
	}

	private void removeAbnomalSlope(ArrayList<PairInterestPoints> list_all){
		for(int i=0;i<list_all.size();i++){
			System.out.println(list_all.get(i).getPointBase().getX()+" "+slopeValue(list_all.get(i),0));
		}
	}
	
	/*---------------------- ------- ----------------------*/
	/*---------------------- matsuba - end ----------------*/
	/*---------------------- ------- ----------------------*/

	public ArrayList<PairInterestPoints> getPairList(){
		return list_base;
	}

}



