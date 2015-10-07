package setPanorama;

/**
 * �����֌W��\���N���X
 * @author hamano
 *
 */
public class PanoramaLink {
	private String basePanoID,targetPanoID;
	
	private int pixelDirToTarget;
	private double value;//�M���x
	
	PanoramaLink(String basePanoID,String targetPanoID,int pixelDirToTarget){
		this.basePanoID = basePanoID;
		this.targetPanoID = targetPanoID;
		this.pixelDirToTarget = pixelDirToTarget;
	}
	
	PanoramaLink(String basePanoID,String targetPanoID,int pixelDirToTarget,double value){
		this.basePanoID = basePanoID;
		this.targetPanoID = targetPanoID;
		this.pixelDirToTarget = pixelDirToTarget;
		this.value = value;
	}
	
	
	public void setValue(double value){
		this.value = value;
	}
	public double getValue(){
		return this.value;
	}
	
	
	public String getBasePanoID(){
		return this.basePanoID;
	}
	
	public String getTargetPanoID(){
		return this.targetPanoID;
	}
	
	public int getPixelDirToTarget(){
		return this.pixelDirToTarget;
	}
	
}
