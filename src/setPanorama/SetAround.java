package setPanorama;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import panoramaCompare.ComparePanorama;
import panoramaCompare.SurmiseRelationalDirection;

/**
 * 面的な相対位置方位推定を行うクラス
 * @author hamano
 *
 */
public class SetAround {	
	
	public static int dist_standard = 300;  //
	private ArrayList<Panorama> panoramaList;
	
	
	public SetAround(String[] fileArray){
		
		ArrayList<Panorama> definedList = new ArrayList<Panorama>();
		ArrayList<Panorama> undefinedList = new ArrayList<Panorama>();
		
		//パノラマ一覧の作成
		Panorama[] panoArray = new Panorama[fileArray.length];
		for(int i=0;i<fileArray.length;i++){
			panoArray[i] = new Panorama(fileArray[i]);
			panoArray[i].setID("pano"+i);
			undefinedList.add(panoArray[i]);
		}
		
		//それぞれの方向関係を推定
		double bestValue = 0;  //最も高い信頼度
		Panorama pano1_best=null,pano2_best=null;  //bestValueのときのパノラマ2つ
		int pixelDirPano1ToPano2 = 0;
		for(int i=0;i<fileArray.length;i++){
			Panorama panoA = panoArray[i];
			for(int j=i+1;j<fileArray.length;j++){
				Panorama panoB = panoArray[j];
				
				//パノラマ比較
				ComparePanorama compare = new ComparePanorama(panoA.getFilePath(),panoB.getFilePath());
				SurmiseRelationalDirection resultA = compare.getSurmisedDir_A();
				SurmiseRelationalDirection resultB = compare.getSurmisedDir_B();
				
				//方向関係の結果を画像(画像中に黄色の縦線で示す)として保存
				//saveImage_test(compare.getCompareImage_AtoB(), panoA.getFilePath(), panoB.getFilePath(), savePath);
				//saveImage_test(compare.getCompareImage_BtoA(), panoB.getFilePath(), panoA.getFilePath(), savePath);
				
				//panoAの設定
				PanoramaLink aLink = new PanoramaLink(panoA.getID(),panoB.getID(),resultA.getPixelForward_BtoT());
				aLink.setValue(compare.getValue());
				panoA.addPanoLink(aLink);
				
				//panoBの設定
				PanoramaLink bLink = new PanoramaLink(panoB.getID(),panoA.getID(),resultB.getPixelForward_BtoT());
				bLink.setValue(compare.getValue());
				panoB.addPanoLink(bLink);
				
				//最も高い信頼度となるパノラマ2つの取得
				if(bestValue < compare.getValue()){
					bestValue = compare.getValue();
					pano1_best = panoA;
					pano2_best = panoB;
					pixelDirPano1ToPano2 = resultA.getPixelForward_BtoT();
				}
			}
		}
		
		
		
		//基準となるパノラマ(pano1_best,pano2_best)の設定
		////pano1_bestの設定
		pano1_best.setLocation(0, 0);
		pano1_best.setPixelNorth(0);
		definedList.add(pano1_best);
		undefinedList.remove(pano1_best);
		////pano2_bestの設定
		double dirPano1ToPano2 =(360.0 + (double)(pixelDirPano1ToPano2 - pano1_best.getPixelNorth())/(double)pano1_best.getImageWidth()*360.0)%360.0;
		double angle1 = (360.0 - dirPano1ToPano2 + 90.0)%360.0;
		angle1 = angle1 / 180.0 * Math.PI; //ラジアンに変換
		double x_pano2 = pano1_best.getX() + dist_standard * Math.cos(angle1);
		double y_pano2 = pano1_best.getY() + dist_standard * Math.sin(angle1);
		int pixelDir2to1 = pano2_best.dirPixelTo(pano1_best.getID());
		int pxNorth_pano2 = calculatePixelNorth(x_pano2, y_pano2, 0, 0, pixelDir2to1 ,pano2_best.getImageWidth()); 
		pano2_best.setLocation(x_pano2, y_pano2);
		pano2_best.setPixelNorth(pxNorth_pano2);
		definedList.add(pano2_best);
		undefinedList.remove(pano2_best);
		
				
		//全てのパノラマの位置が決定するまでループ
		while(undefinedList.size() != 0){
			//位置の推定を行うパノラマを決定する
			double bestCompareValue = 0;
			Panorama nextDefinePano = null;
			Point2D point_nextDefinePano = null;
			PanoramaLink trustLink1 = null;
			PanoramaLink trustLink2 = null;
			for(int i=0;i<undefinedList.size();i++){
				Panorama tempPano = undefinedList.get(i);
				
				//信頼度が高い2つのPanoramaLinkを探索。位置推定に用いる2つのパノラマを決定する
				ArrayList<PanoramaLink> tempLinkList = tempPano.getPanoLink();
				for(int j=0;j<tempLinkList.size();j++){
					PanoramaLink tempLink1 = tempLinkList.get(j);
					for(int k=j+1;k<tempLinkList.size();k++){
						PanoramaLink tempLink2 = tempLinkList.get(k);

						//tempLink1,tempLink2が相対位置決定済みパノラマとのリンクであるかどうか
						if(contain(definedList,tempLink1.getTargetPanoID()) && contain(definedList,tempLink2.getTargetPanoID())){
							//三角測量により位置推定ができるパノラマであるかチェック
							Panorama temp1 = idToPanorama(tempLink1.getTargetPanoID(), panoArray);
							Panorama temp2 = idToPanorama(tempLink2.getTargetPanoID(), panoArray);							
							Point2D point_temp = calculatePosition(temp1, temp2, tempPano);
							if(point_temp != null){
								//信頼度が最も高い2つのPanoramaLinkを持つパノラマに更新
								double compareValue = tempLink1.getValue() + tempLink2.getValue();				
								if(bestCompareValue < compareValue){
									bestCompareValue = compareValue;
									nextDefinePano = tempPano;
									point_nextDefinePano = point_temp;
									trustLink1 = tempLink1;
									trustLink2 = tempLink2;
								}
							}
						}
					}
				}

			}
			
			
			//相対位置の決定
			nextDefinePano.setLocation(point_nextDefinePano.getX(), point_nextDefinePano.getY());
			System.out.println(nextDefinePano.getFileName() + "   " + nextDefinePano.getX() + "  " + nextDefinePano.getX());
			System.out.println(point_nextDefinePano.getX() + "    " + point_nextDefinePano.getY());
			////北の位置をセット
			Panorama pano1 = idToPanorama(trustLink1.getTargetPanoID(), panoArray);
			Panorama pano2 = idToPanorama(trustLink2.getTargetPanoID(), panoArray);
			int pixelNorth = calculatePixelNorth(nextDefinePano.getX(), nextDefinePano.getY(), pano1.getX(), pano1.getY(), trustLink1.getPixelDirToTarget(), nextDefinePano.getImageWidth());
			nextDefinePano.setPixelNorth(pixelNorth);
			
			
			//更新
			definedList.add(nextDefinePano);
			undefinedList.remove(nextDefinePano);
		}
		
		
		panoramaList = definedList;
	}
	
	
	/**
	 * 
	 * @param panoList
	 * @param panoID
	 * @return
	 */
	private boolean contain(ArrayList<Panorama> panoList, String panoID){
		for(int i=0;i<panoList.size();i++){
			Panorama pano = panoList.get(i);
			if(pano.getID().equals(panoID)){
				return true;
			}
		}
		
		return false;
	}
	
	
	
	/**
	 * パノラマオブジェクトの配列を与えて、指定IDのパノラマオブジェクトを探す
	 * @param panoID
	 * @param panoArray
	 * @return
	 */
	private Panorama idToPanorama(String panoID,Panorama[] panoArray){
		for(int i=0;i<panoArray.length;i++){
			Panorama pano = panoArray[i];
			if(pano.getID().equals(panoID)){
				return pano;
			}
		}
		return null;
	}
	
	
	
	private Point2D calculatePosition(Panorama basePano1,Panorama basePano2,Panorama targetPano){
		//basePano1からtargetPanoへの方位を求める
		double dirPano1toTarget = basePano1.directionTo(targetPano.getID());
		//basePano2からtargetPanoへの方位を求める
		double dirPano2toTarget = basePano2.directionTo(targetPano.getID());
		
		
		//basePano1,basePano2がtargetPanoへのPanoramaLinkを持っていないとき
		if(dirPano1toTarget == Double.NaN || dirPano2toTarget == Double.NaN) return null;
		
		//basePano1,basePano2の位置を取得
		double x_pano1 = basePano1.getX();
		double y_pano1 = basePano1.getY();
		double x_pano2 = basePano2.getX();
		double y_pano2 = basePano2.getY();
		
		//三角測量
		Point2D loc_target = LocationMath.triangulate(x_pano1, y_pano1, dirPano1toTarget, x_pano2, y_pano2, dirPano2toTarget);		
		return loc_target;
	}
	
	
	


	
	
	
	/**
	 * 2つのパノラマの座標とBase->Target方向をもとに、Baseの北の位置が
	 * どこになるかを計算
	 * @param xB
	 * @param yB
	 * @param xT
	 * @param yT
	 * @param pixelDirBtoT
	 * @param widthBase
	 * @return
	 */
	private int calculatePixelNorth(double xB,double yB,double xT,double yT,int pixelDirBtoT,int widthBase){
		double diff_x = xT - xB;
		double diff_y = yT - yB;
		double theta_rad = Math.atan2(diff_y,diff_x);
		double theta = theta_rad / Math.PI * 180;
		double dirBtoT = (360.0 + 90.0 - theta)%360.0;
		double diffDir = (360.0 - dirBtoT)%360.0;
		double diffRate = diffDir / 360.0;
		int pixelNorth = (widthBase + pixelDirBtoT +  (int)Math.round(diffRate*widthBase))%widthBase;
		return pixelNorth;
	}
	

	
	
	public ArrayList<Panorama> getPanoramaList(){
		return panoramaList;
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
			boolean result = ImageIO.write(image, "jpg", new File(writeFileName));
			if(!result){
				System.out.println("ファイル保存に失敗");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
