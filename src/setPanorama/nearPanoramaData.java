package setPanorama;

public class nearPanoramaData {
	
	public Panorama panorama;
	public int direction;
	public double similarity;
	public boolean judgeCalDir;
	
	public nearPanoramaData(Panorama pano){
		panorama=pano;
		direction=-1;
		similarity=-1.0;
		judgeCalDir=false;
	}

}
