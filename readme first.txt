SVG Map Tools�ɂ���

SVG Map Tools�́AShapefile��ܓx�o�x��������CSV�t�@�C������SVGMap�R���e���c�𐶐�����c�[���ł��B
Java�ō\�z����Ă���AJava(Oracle��)�����삷������K�v�ł��B���Windows�����CentOS�œ���m�F���Ă��܂��B

�R���e���c�́A�f�[�^�̖��x�ɉ����āA���X�^�[�ƃx�N�^�[�����������l���؃^�C���Ƃ��ĂƂ��Đ������邱�Ƃ��ł��܂��B
����ɂ��A���S�����ȏ�̃f�[�^�̒n�}�R���e���c���\�z�ł��܂��B


���݂̂Ƃ���ȉ��̂S�̃��W���[���ō\������Ă��܂��B���̂���Shape2SVGMap��Shape2ImageSVGMap����{�ƂȂ郂�W���[���ł��B

SVGMap�̊T�v�E�ǂ̂悤�ȃR���e���c���\���ł��邩�́Ahttp://svgmap.org/ ���Q�Ƃ��������B

Shape2SVGMap: CSV��������Shapefile�`���̃f�[�^���A�x�N�g���f�[�^��SVGMap�R���e���c�ɕϊ����܂��B

Shape2ImageSVGMap: CSV��������Shapefile�`���̃f�[�^���A�r�b�g�C���[�W��SVGMap�R���e���c�ɕϊ����܂��B

Shape2WGS84:  ���n�n�ϊ��⑮���ɉ������t�@�C�������AShapefile<>CSV�ϊ��Ȃǂ��s���v���v���Z�b�T

HyperBuilder: ������SVGMap���C���[�𓝍�����R���e�i�f�[�^�𐶐�

�悸�Atutorial�f�B���N�g���̓��e��p���āA��{���W���[���iShape2SVGMap�AShape2ImageSVGMap�j�̊�{������w�K���A�����̗���𗝉����Ă���g�p���Ă��������B�Ȃ��Atutorials�̓��e��Windows�悤�ɒ�������Ă��܂��BLinux�ȂǂŎg���ꍇ�͓K�X�|�󂵂ė��p���Ă��������B

���̐ݒ�
���̃t�@�C��������f�B���N�g�����J�����g�f�B���N�g���Ƃ��A���̔z����libs�f�B���N�g���ɁA�{�c�[�����g�p����N���X���C�u�����Ageotools2.7.5 *1 �� javacsv2.1 *2�@��jar�t�@�C���Q��z�u����K�v������܂��Bjavacsv�͈��jar�t�@�C���Ageotools�͑�ʂ�jar�t�@�C������\������Ă��܂��B��������I�[�v���\�[�X�\�t�g�E�F�A�ł��B�ȉ����炻�ꂼ��_�E�����[�h�ł��܂��B
*1: https://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/2.7.5/
*2: https://sourceforge.net/projects/javacsv/

Shape2WGS84��p���āA���{���n�n(TOKYO Datum)���琢�E���n�n�ւ̑��n�n�ϊ����s�������ꍇ�A�J�����g�f�B���N�g���ɍ��y�n���@�����J����ϊ��p�����[�^�f�[�^"TKY2JGD.par"(Vwer2.1.2) *3�@�����̃t�@�C�����Ŕz�u���Ă��������B�ȉ�����_�E�����[�h�ł��܂��B
*3: http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.htm

l
�c�[���̋N�����@(Windows)
�c�[���̓J�����g�f�B���N�g���ňȉ��̃R�}���h�ɂ��N���ł��܂��B�I�v�V�����Ȃ��ŋN������ƃw���v���\������܂��B

java -classpath lib\*;shape2svgmap.jar Shape2SVGMap (options)

java -classpath lib\*;shape2svgmap.jar Shape2ImageSVGMap (options)

java -classpath lib\*;shape2svgmap.jar Shape2WGS84 (options)

java -classpath lib\*;shape2svgmap.jar HyperBuilder (options)


���C�Z���X�ɂ���
�{�c�[����GPL Ver.3�Ɋ�Â��I�[�v���\�[�X�\�t�g�E�F�A�ł��B
