package operate;

import java.util.ArrayList;

import panoramaCompare.ComparePanorama;

public class PanoramaGraph {
	
	public String BasePanorama;
	public ArrayList<PanoramaDirection> TargetPanorama;
	
	public PanoramaGraph(String base){
		BasePanorama=base;
		TargetPanorama=new ArrayList<PanoramaDirection>();
	}
	
	public int judgeDirection(String basePanorama,String targetPanorama){
		ComparePanorama judgePanoramas=new ComparePanorama(basePanorama,targetPanorama);
		System.out.println(basePanorama+","+targetPanorama+":"+judgePanoramas.surmisedDirA.getPixelForward_BtoT());
		return judgePanoramas.surmisedDirA.getPixelForward_BtoT();
	}
	
	public void judgeAllDirection(){
		for(int i=0;i<TargetPanorama.size();i++){
			if(TargetPanorama.get(i).Direction==-1){
				TargetPanorama.get(i).Direction=judgeDirection(BasePanorama,TargetPanorama.get(i).Panorama.BasePanorama);
			}
		}
	}
	
	public ArrayList<PanoramaGraph> getNearList(){
		ArrayList<PanoramaGraph> returnList=new ArrayList<PanoramaGraph>();
		for(int i=0;i<TargetPanorama.size();i++){
			returnList.add(TargetPanorama.get(i).Panorama);
		}
		return returnList;
	}

}
