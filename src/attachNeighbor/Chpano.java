package attachNeighbor;
/**
 * �p�m���}Base����p�m���}Target�ւ̊֌W���`�������́@�@�@�̂͂�
 * PCD�ɂ�����chpano���`���悤�Ƃ�������
 * @author hamano
 *
 */
public class Chpano{
	
	public Panorama target;  //�ؑ֌��Panorama�I�u�W�F�N�g
	public Panorama base;  //�֑ؑO��Panorama�I�u�W�F�N�g
	
	private double dist;
	private double dir;
	
	private int changeDirStart;
	private int changeDirEnd;
	private double fovFBase;//�ؑ֎���p(�O�i)
	private double fovFNext;//�ؑ֌㎋��p(�O�i)
	private double fovBBase;//�ؑ֎���p(���)
	private double fovBNext;//�ؑ֌㎋��p(���)
	
	
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
	 * �w����ʒl��target�̉摜�ɂ����Ăǂ�����������Ԃ�(x���W)
	 * @return
	 */
	public int getDirPxInTarget(double dir){
		int widthTarget = target.getWidth();
		int pxDir = Math.round(  (float)((target.getNorth() + dir*widthTarget/360.0)%widthTarget) );
		//System.out.println("px:" + (target.getNorth() + dir*widthTarget/360.0)%widthTarget +"  round:"+pxDir);
		
		return pxDir;
	}
	
	
	/**
	 * �w����ʒl��base�̉摜�ɂ����Ăǂ�����������Ԃ�(x���W)
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
