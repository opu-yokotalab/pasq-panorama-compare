package operate;

public class PanoramaDirection {
	
	public PanoramaGraph Panorama;
	public int Direction;
	
	public PanoramaDirection(PanoramaGraph panorama){
		Panorama=panorama;
		Direction=-1;
	}
	
	public PanoramaDirection(PanoramaGraph panorama,int direction){
		Panorama=panorama;
		Direction=direction;
	}

}
