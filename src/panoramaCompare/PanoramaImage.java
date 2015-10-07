package panoramaCompare;

import java.awt.image.BufferedImage;

public class PanoramaImage {
	
	/**
     * 指定範囲に基づいて、BuffredImageを生成
     * パノラマ画像対象のため、x+wが画像の幅を超えたとき、端と端を繋いだ画像を生成
     * @param imageInput
     * @param xS
     * @param xE
     * @return
     */
    public static  BufferedImage createPanoImage(BufferedImage imageInput,int x,int y, int w,int h){
    	//inputImageの情報取得
    	int inputWidth = imageInput.getWidth();
    	int inputHeight = imageInput.getHeight();
    	
    	//エラー処理
    	if(y+h > inputHeight){
    		System.out.println("高さの指定が正しくありません");
    		return null;
    	}
    	
    	//出力用のBufferedImage生成
    	BufferedImage imageOutput = new BufferedImage(w,h,BufferedImage.TYPE_INT_BGR);
    	
    	//範囲に基づいて、inputImageからoutputImageにコピー
    	//画像の終端を越える(端をまたぐ場合)
    	if(x+w > inputWidth){
    		//指定位置から右端までを生成
        	for(int i=0;i<inputWidth-x;i++){  //x座標
        		for(int j=0;j<h;j++){  //y座標
        			imageOutput.setRGB(i,j,imageInput.getRGB(x+i,y+j));
        		}
        	}
        	//左端から指定位置まで生成
        	int dx = inputWidth - x;
        	int dy = 0;
        	for(int i=0;i<w-dx;i++){
        		for(int j=0;j<h-dy;j++){
        			imageOutput.setRGB(i+dx,j+dy,imageInput.getRGB(i, y+j));
        		}
        	}
    	}
    	//画像の端をまたがない
    	else{
        	for(int i=0;i<w;i++){
        		for(int j=0;j<h;j++){
        			imageOutput.setRGB(i,j,imageInput.getRGB(x+i,y+j));
        		}
        	}
    	}
    	
    	return imageOutput;
    }
	
    
    
    public static double diffRate(int x_base,int width_base,int x_target,int width_target){
		double xr_base = (double)x_base / (double)width_base;
		double xr_target = (double)x_target / (double)width_target;
		return diffRate(xr_base,xr_target);
    }
    
    
    
    public static double diffRate(double xr_base,double xr_target){
    	//エラー処理
    	if(xr_base > 1.0 || xr_target > 1.0 || xr_base < 0 || xr_target < 0){
    		System.out.println("diffRateで不正の値を与えている");
    		return Double.NaN;
    	}
    	
    	double diffRate;
		////端をまたがない距離
		double diffRate1 = xr_target - xr_base;
		////端をまたぐ距離
		double diffRate2;
		if(xr_base > xr_target){
			diffRate2 = 1.0 - xr_base + xr_target;
		}else{
			diffRate2 = -(1.0 - xr_target + xr_base);
		}
		
		if(Math.abs(diffRate1) < Math.abs(diffRate2)) diffRate = diffRate1;
		else diffRate = diffRate2;
		
		return diffRate;
    }
    

}
