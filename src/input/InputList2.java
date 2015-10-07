package input;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import panoramaCompare.*;
import setPanorama.CreatePCD;
import setPanorama.Panorama;
import setPanorama.SetAround;
import setPanorama.SetConsecutive;

/**
 * SetAroundを用いた構築
 * @author hamano
 *
 */
public class InputList2 {
	
	public InputList2(String[] list,String savePlace){
		input(list,savePlace);
	}
	
	public InputList2(String directory,String saveDirectory){
		File dir = new File(directory);
	    //File[] files = dir.listFiles();
	    File[] jpgFiles = listJPGFile(dir);
	    String[] fileArray = new String[jpgFiles.length];
	    for (int i = 0; i < jpgFiles.length; i++) {
	        fileArray[i] = jpgFiles[i].toString();
	    }
	    
	    input(fileArray,saveDirectory);
	}
	
	
	
	public void input(String[] fileList,String savePlace){
		/*
		for(int i=0;i<fileList.length-2;i++){
			String panoA = fileList[i];
			String panoB = fileList[i+1];
			ComparePanorama comparePano = new ComparePanorama(panoA,panoB);
			
			BufferedImage temp = comparePano.getCompareImage_AtoB();
			saveImage_test(temp,panoA,panoB,savePlace);
			
			temp = comparePano.getCompareImage_BtoA();
			saveImage_test(temp,panoB,panoA,savePlace);
		}
		*/
		
		long start = System.currentTimeMillis();
		SetAround sa = new SetAround(fileList);

		long stop = System.currentTimeMillis();
		System.out.println("全体の実行にかかった時間は " + (stop - start) + " ミリ秒です。");
		
		
		//PCD作成
		Panorama[] panoArray = sa.getPanoramaList().toArray(new Panorama[0]);
		CreatePCD cPCD = new CreatePCD(panoArray,savePlace+"\\pcd.xml");
		
		
	}
	
	
	
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
	
	
	
	public void saveImage_test(BufferedImage image,String fileNameA,String fileNameB,String savePlace){
		//ファイルへの書き出し
		try{
			////テスト用の処理
			String[] temp = fileNameA.split("\\\\");
			String fileA = temp[temp.length-1];
			if(fileA.indexOf(".jpg") != -1){
				temp = fileA.split("\\.");
				fileA = temp[0];
				if(fileA.indexOf("_")!=-1){
					temp = fileA.split("_");
					fileA = temp[1];
				}
			}
			
			temp = fileNameB.split("\\\\");
			String fileB = temp[temp.length-1];
			if(fileB.indexOf(".jpg") != -1){
				temp = fileB.split("\\.");
				fileB = temp[0];
				if(fileB.indexOf("_")!=-1){
					temp = fileB.split("_");
					fileB = temp[1];
				}

			}
			String writeFileName = savePlace + "\\" + fileA + "-" + fileB + ".jpg";
			////
			
			
			boolean result = ImageIO.write(image, "jpg", new File(writeFileName));
			if(!result){
				System.out.println("ファイル保存に失敗");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String args[]){
		if(args.length == 2){
				InputList2 input = new InputList2(args[0],args[1]);
		}else{
			System.out.println("引数が正しくありません");
		}

	}
}
