package panoramaCompare;

import java.util.Comparator;

import mySurf.PairInterestPoints;


/**
 * 特徴度でソートするために作成したもの
 * @author hamano
 *
 */
public class MyComparator implements Comparator{
    private boolean up = true;  //昇順
    private boolean down = false;  //降順
	
    public void setUp(){
    	up = true;
    	down = false;
    }
    
    
    public void setDown(){
    	up = false;
    	down = true;
    }
    
    
    
    public int compare(Object a, Object b) {
    	
        PairInterestPoints pair1 = (PairInterestPoints) a;
        PairInterestPoints pair2 = (PairInterestPoints) b;
        
        double value1 = pair1.getValue();
        double value2 = pair2.getValue();
        
        if(up){
        	return Double.compare(value1, value2);
        }
        if(down){
        	return Double.compare(value2, value1);
        }
        
        return 0;
    }
    
    
    /*
	public int compare(Object a, Object b) {
    	
        SurmisePoint point1 = (SurmisePoint) a;
        SurmisePoint point2 = (SurmisePoint) b;
        
        double value1 = point1.getPair1().getValue() + point1.getPair2().getValue();
        double value2 = point2.getPair1().getValue() + point2.getPair2().getValue();
        
        if(up){
        	return Double.compare(value1, value2);
        }
        
        if(down){
        	return Double.compare(value2,value1);
        }
        
        return 0;
        
    }
    */
}
