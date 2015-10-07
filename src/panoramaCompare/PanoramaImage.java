package panoramaCompare;

import java.awt.image.BufferedImage;

public class PanoramaImage {
	
	/**
     * �w��͈͂Ɋ�Â��āABuffredImage�𐶐�
     * �p�m���}�摜�Ώۂ̂��߁Ax+w���摜�̕��𒴂����Ƃ��A�[�ƒ[���q�����摜�𐶐�
     * @param imageInput
     * @param xS
     * @param xE
     * @return
     */
    public static  BufferedImage createPanoImage(BufferedImage imageInput,int x,int y, int w,int h){
    	//inputImage�̏��擾
    	int inputWidth = imageInput.getWidth();
    	int inputHeight = imageInput.getHeight();
    	
    	//�G���[����
    	if(y+h > inputHeight){
    		System.out.println("�����̎w�肪����������܂���");
    		return null;
    	}
    	
    	//�o�͗p��BufferedImage����
    	BufferedImage imageOutput = new BufferedImage(w,h,BufferedImage.TYPE_INT_BGR);
    	
    	//�͈͂Ɋ�Â��āAinputImage����outputImage�ɃR�s�[
    	//�摜�̏I�[���z����(�[���܂����ꍇ)
    	if(x+w > inputWidth){
    		//�w��ʒu����E�[�܂ł𐶐�
        	for(int i=0;i<inputWidth-x;i++){  //x���W
        		for(int j=0;j<h;j++){  //y���W
        			imageOutput.setRGB(i,j,imageInput.getRGB(x+i,y+j));
        		}
        	}
        	//���[����w��ʒu�܂Ő���
        	int dx = inputWidth - x;
        	int dy = 0;
        	for(int i=0;i<w-dx;i++){
        		for(int j=0;j<h-dy;j++){
        			imageOutput.setRGB(i+dx,j+dy,imageInput.getRGB(i, y+j));
        		}
        	}
    	}
    	//�摜�̒[���܂����Ȃ�
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
    	//�G���[����
    	if(xr_base > 1.0 || xr_target > 1.0 || xr_base < 0 || xr_target < 0){
    		System.out.println("diffRate�ŕs���̒l��^���Ă���");
    		return Double.NaN;
    	}
    	
    	double diffRate;
		////�[���܂����Ȃ�����
		double diffRate1 = xr_target - xr_base;
		////�[���܂�������
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
