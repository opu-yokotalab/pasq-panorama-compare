package attachNeighbor;
/**
 * パノラマBaseからパノラマTargetへの関係を定義したもの　　　のはず
 * PCDにおけるchpanoを定義しようとしたもの
 * @author hamano
 *
 */
public class Chpano{
	
	public Panorama target;  //切替後のPanoramaオブジェクト
	public Panorama base;  //切替前のPanoramaオブジェクト
	
	private double dist;
	private double dir;
	
	private int changeDirStart;
	private int changeDirEnd;
	private double fovFBase;//切替視野角(前進)
	private double fovFNext;//切替後視野角(前進)
	private double fovBBase;//切替視野角(後退)
	private double fovBNext;//切替後視野角(後退)
	
	
	Chpano(Panorama base,Panorama target,double dist,double dir){
		init();
		this.dist = dist;
		this.dir = dir;
		this.base = base;
		this.target = target;
	}
	
	public void init(){
		target = null;
		base = null;
		dist = -1;
		dir = -1;
		changeDirStart = -1;
		changeDirEnd = -1;
		fovFBase = PCDConverter.fovFBase;
		fovFNext = PCDConverter.fovFNext;
		fovBBase = PCDConverter.fovFNext;
		fovBNext = PCDConverter.fovFBase;
	}
	
	
	public void setChangeDir(int startDir,int endDir){
		this.changeDirStart = startDir;
		this.changeDirEnd = endDir;
	}
	

	/**
	 * 指定方位値がtargetの画像においてどこを示すかを返す(x座標)
	 * @return
	 */
	public int getDirPxInTarget(double dir){
		int widthTarget = target.getWidth();
		int pxDir = Math.round(  (float)((target.getNorth() + dir*widthTarget/360.0)%widthTarget) );
		//System.out.println("px:" + (target.getNorth() + dir*widthTarget/360.0)%widthTarget +"  round:"+pxDir);
		
		return pxDir;
	}
	
	
	/**
	 * 指定方位値がbaseの画像においてどこを示すかを返す(x座標)
	 * @param dir
	 * @return
	 */
	public int getDirPxInBase(double dir){
		int widthBase = base.getWidth();
		int pxDir = Math.round(  (float)((base.getFixedNorth() + dir*widthBase/360.0)%widthBase) );
//		System.out.println("px:" + (float)((base.getFixedNorth() + dir*widthBase/360.0)%widthBase) +"  round:"+pxDir);
		return pxDir;
	}
	
	
	
	
	public double getDist(){
		return dist;
	}
	public double getDir(){
		return dir;
	}
	public String getTargetSrc(){
		return target.getSrc();
	}
	public String getBaseSrc(){
		return base.getSrc();
	}
	public int getDirStart(){
		return this.changeDirStart;
	}
	public int getDirEnd(){
		return this.changeDirEnd;
	}
	public double getFovFBase(){
		return this.fovFBase;
	}
	public double getFovFNext(){
		return this.fovFNext;
	}
	public double getFovBNext(){
		return this.fovBNext;
	}
	public double getFovBBase(){
		return this.fovBBase;
	}

}
