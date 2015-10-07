・構成
attachNeighbor
PCDから緯度経度を読み込み、それに応じて近傍情報を付与する

input
入力をまとめたもの

panoramaCompare
方向関係推定に関する処理をまとめたもの

setPanorama
位置方位推定に関する処理をまとめたもの

lib
参照するライブラリを置く場所

build
ビルドしてjarファイル化したものを置く場所




・開発環境
Eclipse 3.4.2 

・参照ライブラリ
commons-math-1.2.jar , mySurf.jarは共にSURF処理に必要なライブラリ。mySurf.jarはjopensurfを基に濱野が手を加えたもの。mySurf.jarに関しては、別のプロジェクトを参照してください。




・PasQ構成の流れ
１．input/InputList.java or input/InputList2.java　により、相対的な空間(位置・方位のみ。近傍情報なし)を構築。
		java input/InputList.java パノラマ画像のあるフォルダ名　結果を出力するフォルダ名
		java input/InputList2.java パノラマ画像のあるフォルダ名 結果を出力するフォルダ名
			例. c:\study\kinojoA C:\study\kinojoA\result

２．(線的な相対位置方位推定のみ)方向関係推定に誤りがあるようであれば、setPanorama/ModifyLocation.java を用いて、修正する。
		java setPanorama/ModifyLocation.java 読み込む相対的PCD 修正した相対的PCDの出力先 [誤った方向関係推定結果の撮影順序における前のパノラマID 前パノラマ画像における正しい方向(ピクセル) 後パノラマ画像における正しい方向(ピクセル)]*n
			例. C:\kinojo2011\resource\C\pcd.xml C:\kinojo2011\resource\C\m_pcd.xml pano3 972 2940 pano4 900 3000 pano5 650 3200 pano6 760 3353 pano7 900 3160 pano13 900 3130

３．setPanorama/MappingLatLng.java を用いて、緯度経度へマッピングする。
		java setPanorama/MappingLatLng.java 読み込む相対的PCD　[マッピングしたPCDの出力先 パノラマID 緯度 経度]*n
			例.C:\kinojo2011\resource\J\m_pcd.xml C:\kinojo2011\resource\pcd_LatLng.xml pano0 34.72897349460525 133.7692630290985 pano62 34.72840476009677 133.77023935317993 pano127 34.727615579374 133.76919329166412

４．attachNeighbor/PCDConverer.java　を用いて、近傍情報を付与する。(attachNeighbor/PCDConverter.javaの近傍情報付与アルゴリズムは岸本先輩作成のものと同じはず)
		java attachNeighbor/PCDConveter.java 読み込むPCD 近傍情報付与したPCDの出力先
			例.C:\kinojo2011\resource\pcd_kinojo.xml C:\kinojo2011\resource\cnv_pcd_kinojo.xml
	




・大まかな処理の流れ(相対的PCD生成まで)
input/InputList で画像名の配列を作成。その画像名の配列をsetPanorama/setConsective or setPanorama/setAround に渡す。
setPanorama/setConsective or setPanorama/setAround では、受け取った配列に基づき、方向関係推定(panoramaCompare/ComparePanorama)を行う。そして、その結果を取得し、相対位置方位を推定する。
	
	
		



・修正が必要だと思っていること(実装に関すること)
attachNeighborパッケージは、近傍情報付与部分も修正(障害物判定などの追加)しようと思って作成した。しかし、そこまで手が回らず、読みにくいだけのコードになっってしまった。
障害物判定が必要ないなら、もっとわかりやすく書くことができる(以前の近傍情報付与プログラムと同じアルゴリズムなので、そっちを見る方がわかりやすいと思う) 


inputで作成される相対的PCDが適当なので、修正が必要かと。
相対的な位置なのに、PCDとまったく同じようにXMLを出力しているため、紛らわしい。
前後のパノラマ画像で切替が発生するように近傍情報を付与しているが、これはいらない。

