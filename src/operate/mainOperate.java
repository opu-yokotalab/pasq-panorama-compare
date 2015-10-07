package operate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class mainOperate {
	
	public File Directry;
	public ArrayList<String> PanoramaList;
	public ArrayList<PanoramaGraph> Graph;
	
	public File[] listJPGFile(File dir){
		File[] files = dir.listFiles();
		ArrayList<File> jpgFileList = new ArrayList<File>();
		for(int i=0;i<files.length;i++){
			String fileName = files[i].getName();
			String[] word = fileName.split("\\.");
			if(word[word.length-1].equals("jpg") || word[word.length-1].equals("JPG")){
				jpgFileList.add(files[i]);
			}
		}
		File[] jpgFiles = jpgFileList.toArray(new File[0]);
		return jpgFiles;
	}
	
	public boolean judgeNear(String basePanorama,String targetPanorama){
		//画像近隣判定部分
		//未完成
		PanoramaPair pair=new PanoramaPair(basePanorama,targetPanorama);
		double judge=1.0;
		judge=pair.getSimilarityUsingLuminance();
		//System.out.println(basePanorama+","+targetPanorama+":"+judge);
		double threshold=1.0;
		if(judge>threshold){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void openDirectry(String dir){
		Directry=new File(dir);
	}
	
	private void setPanoramaList(){
		PanoramaList=new ArrayList<String>();
		File[] jpgFiles = listJPGFile(Directry);
	    for (int i = 0; i < jpgFiles.length; i++) {
	        PanoramaList.add(jpgFiles[i].toString());
	    }
	}
	
	private void setGraph(){
		Graph=new ArrayList<PanoramaGraph>();
		for(int i=0;i<PanoramaList.size();i++){
			PanoramaGraph addPanorama=new PanoramaGraph(PanoramaList.get(i));
			Graph.add(addPanorama);
		}
	}
	
	private void makeGraph(){
		for(int i=0;i<PanoramaList.size()-1;i++){
			for(int j=i+1;j<PanoramaList.size();j++){
				if(judgeNear(PanoramaList.get(i),PanoramaList.get(j))){
					PanoramaDirection addPanorama1=new PanoramaDirection(Graph.get(i));
					PanoramaDirection addPanorama2=new PanoramaDirection(Graph.get(j));
					Graph.get(i).TargetPanorama.add(addPanorama2);
					Graph.get(j).TargetPanorama.add(addPanorama1);
				}
			}
		}
		for(int i=0;i<Graph.size();i++){
			Graph.get(i).judgeAllDirection();
		}
	}
	
	public mainOperate(String dir){
		openDirectry(dir);
		setPanoramaList();
		setGraph();
		makeGraph();
		//test_makeGraph();
	}
	
	private void test_makeGraph(){
		int base=PanoramaList.size()/2;
		double space=1.5;
		String file="test_output.txt";
		try{
			FileWriter fw=new FileWriter(file,true);
			for(int i=0;i<PanoramaList.size();i++){
				if(i!=base){
					PanoramaPair pair=new PanoramaPair(PanoramaList.get(base),PanoramaList.get(i));
					double sim=pair.getSimilarityUsingLuminance();
					System.out.println(space*(i-base)+" "+sim);
					//fw.write(space*(i-base)+" "+sim);
				}
			}
			fw.close();
		}catch(IOException e){
			System.out.println("file error.\n");
			System.exit(0);
		}
	}

}
