�E�\��
attachNeighbor
PCD����ܓx�o�x��ǂݍ��݁A����ɉ����ċߖT����t�^����

input
���͂��܂Ƃ߂�����

panoramaCompare
�����֌W����Ɋւ��鏈�����܂Ƃ߂�����

setPanorama
�ʒu���ʐ���Ɋւ��鏈�����܂Ƃ߂�����

lib
�Q�Ƃ��郉�C�u������u���ꏊ

build
�r���h����jar�t�@�C�����������̂�u���ꏊ




�E�J����
Eclipse 3.4.2 

�E�Q�ƃ��C�u����
commons-math-1.2.jar , mySurf.jar�͋���SURF�����ɕK�v�ȃ��C�u�����BmySurf.jar��jopensurf������_�삪������������́BmySurf.jar�Ɋւ��ẮA�ʂ̃v���W�F�N�g���Q�Ƃ��Ă��������B




�EPasQ�\���̗���
�P�Dinput/InputList.java or input/InputList2.java�@�ɂ��A���ΓI�ȋ��(�ʒu�E���ʂ̂݁B�ߖT���Ȃ�)���\�z�B
		java input/InputList.java �p�m���}�摜�̂���t�H���_���@���ʂ��o�͂���t�H���_��
		java input/InputList2.java �p�m���}�摜�̂���t�H���_�� ���ʂ��o�͂���t�H���_��
			��. c:\study\kinojoA C:\study\kinojoA\result

�Q�D(���I�ȑ��Έʒu���ʐ���̂�)�����֌W����Ɍ�肪����悤�ł���΁AsetPanorama/ModifyLocation.java ��p���āA�C������B
		java setPanorama/ModifyLocation.java �ǂݍ��ޑ��ΓIPCD �C���������ΓIPCD�̏o�͐� [����������֌W���茋�ʂ̎B�e�����ɂ�����O�̃p�m���}ID �O�p�m���}�摜�ɂ����鐳��������(�s�N�Z��) ��p�m���}�摜�ɂ����鐳��������(�s�N�Z��)]*n
			��. C:\kinojo2011\resource\C\pcd.xml C:\kinojo2011\resource\C\m_pcd.xml pano3 972 2940 pano4 900 3000 pano5 650 3200 pano6 760 3353 pano7 900 3160 pano13 900 3130

�R�DsetPanorama/MappingLatLng.java ��p���āA�ܓx�o�x�փ}�b�s���O����B
		java setPanorama/MappingLatLng.java �ǂݍ��ޑ��ΓIPCD�@[�}�b�s���O����PCD�̏o�͐� �p�m���}ID �ܓx �o�x]*n
			��.C:\kinojo2011\resource\J\m_pcd.xml C:\kinojo2011\resource\pcd_LatLng.xml pano0 34.72897349460525 133.7692630290985 pano62 34.72840476009677 133.77023935317993 pano127 34.727615579374 133.76919329166412

�S�DattachNeighbor/PCDConverer.java�@��p���āA�ߖT����t�^����B(attachNeighbor/PCDConverter.java�̋ߖT���t�^�A���S���Y���͊ݖ{��y�쐬�̂��̂Ɠ����͂�)
		java attachNeighbor/PCDConveter.java �ǂݍ���PCD �ߖT���t�^����PCD�̏o�͐�
			��.C:\kinojo2011\resource\pcd_kinojo.xml C:\kinojo2011\resource\cnv_pcd_kinojo.xml
	




�E��܂��ȏ����̗���(���ΓIPCD�����܂�)
input/InputList �ŉ摜���̔z����쐬�B���̉摜���̔z���setPanorama/setConsective or setPanorama/setAround �ɓn���B
setPanorama/setConsective or setPanorama/setAround �ł́A�󂯎�����z��Ɋ�Â��A�����֌W����(panoramaCompare/ComparePanorama)���s���B�����āA���̌��ʂ��擾���A���Έʒu���ʂ𐄒肷��B
	
	
		



�E�C�����K�v���Ǝv���Ă��邱��(�����Ɋւ��邱��)
attachNeighbor�p�b�P�[�W�́A�ߖT���t�^�������C��(��Q������Ȃǂ̒ǉ�)���悤�Ǝv���č쐬�����B�������A�����܂Ŏ肪��炸�A�ǂ݂ɂ��������̃R�[�h�ɂȂ����Ă��܂����B
��Q�����肪�K�v�Ȃ��Ȃ�A�����Ƃ킩��₷���������Ƃ��ł���(�ȑO�̋ߖT���t�^�v���O�����Ɠ����A���S���Y���Ȃ̂ŁA����������������킩��₷���Ǝv��) 


input�ō쐬����鑊�ΓIPCD���K���Ȃ̂ŁA�C�����K�v���ƁB
���ΓI�Ȉʒu�Ȃ̂ɁAPCD�Ƃ܂����������悤��XML���o�͂��Ă��邽�߁A����킵���B
�O��̃p�m���}�摜�Őؑւ���������悤�ɋߖT����t�^���Ă��邪�A����͂���Ȃ��B

