package org.svgmap.shape2svgmap;

// SvgMap�̏W������Ȃ�^�C���̃N���X�ł�
// Rev19���畡���ɕ�������A������̓X���b�h�}�l�[�W�� SVGMapTileTM
//
// Copyright 2007 by Satoru Takagi
// 2007.5.10 : Ver.1
// 2010.08.19 geoTools 2.6.5 support
// 2013.08.06 �^�C�����O�̃V�X�e����傫�����X�g�������@�������A���̌��ʃ��A(���������ł��Ȃ�)�ȃo�O�������Ă���i�ڍׂ�Shp2SVGMap18�Q�Ɓj
// 2014.02.13 �q�[�v�}���̌����J�n
// 2014.02.14 [][]�z�񂪔���Ȃ��Ƃ������ł��邱�Ƃ������AHashSet�ɂ�鉼�z�z�񉻂Ɏ��g��(���W���[�o�[�W�����A�b�v�B�ύX�����͑����ł��I)
// 2014.03.27 �^�C������Ă��Ȃ��Ƃ��ɂ��������Ȃ�̂��C��
// 2014.05.01 setShadowGroup debug
// 2014.10.14 case '\\' debug
// 2015.04.17 �J���}�����^�f�[�^�ɓ����Ă���Ƃ���metaembed2�듮��h��
// 2015.09.04 pStep��ύX(����炵�� �R���e�i�傫���Ȃ肷����)�AtileAreaRect��<path>����<rect>�ɉ��P
//
// === ���t�@�N�^�����O �N���X�����ύX Shape2SVGMap19�p�̃��W���[��
//
// 2016.04.07 ��x�ڂ̃}���`�X���b�h���`�������W���J�n�@�����SVGMapTiles���x���Ń}���`�X���b�h���������Ă݂�B�e�R�}���h���A���\�b�h�Ɠ����̃C���i�[�N���X�Ƃ��ċK�肵�A�R�}���h�V�[�P���X���C���X�^���X�E�z�񉻂��ăo�b�t�@�����O���A����V�݂���SVGMapTilesThread�ɓ������郁�J�j�Y���̃A�[�L�e�N�`�����؂Ə����H���i���t�@�N�^�����O�j���J�n
// 2016.04.15 ��L�A�[�L�e�N�`���ł悤�₭���肵����
// 2016.05.12 �܂��X���b�h�Z�[�t�łȂ�����������A���C���B����outofsize���蕔�����Ђǂ��B
// 2016.10.31 CustomAttr��svgMapTiles�ł��g����悤��
//
// === ���t�@�N�^�����O �����\��
// 2017.04.19 PointGeom�ł�cliping���[�v�������BsubTile��ArrayList->HashSet���AThread��Executor�� (�Q�l:http://java-study.blog.jp/archives/1036862519.html)
// 2017.05.12 �񎟌��n�b�V���ɐ���ȃo�O�E�E�C��
// 2017.09.15 ���b�Z�[�W�̃}�C�i�[����(�C�ɂ��邱�Ƃ͉����Ȃ�)
// 2018.06.28 �o�O�C�� cliping���[�v�������[�`���ɖ��
// 2018.07.04 �I�[�o�[�t���[�����^�C���͑��X�ɃN���[�Y��File Open�����Ȃ�ׂ����炷�B

// ISSUES:
// putHeader�̓}���`�X���b�h���������ĂȂ��i���Ԃ�K�v�Ȃ��j�@���̂��߁A�t�@�C���`����comment�p�[�g�̓�������ς���Ă��܂��i�Ƃɂ����܂��̓w�b�_�܂ł͓��銴���j
// putComment�͂�����Ɖ���������������E�E(container�݂̂ɃR�����g������Ƃ�������)
// id������Ɣj���񂷂�B(�X���b�h�Ԃ�ID���d�����N�����E�E) -noid�I�v�V�������f�t�H���g�Ƃ��Ă������E�E�E



import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;
import com.vividsolutions.jts.geom.*;
import java.awt.geom.*;
import org.geotools.feature.*;
// import org.geotools.feature.type.*;

import org.opengis.feature.simple.*;

/**
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
**/

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class SvgMapTilesTM {
	public int pStep = 3; // �R���e�i�K�w���̃X�e�b�v���ł� �ő�4^step����image���R���e�i�ɐݒu (2015.9.4 �ύX)
	//SVG Map Tiles�́A�R���e�i�ƃ^�C������\������܂��B
	//�^�C�����P�����Ȃ��Ƃ��́A�R���e�i�ƃ^�C���͈�̂ƂȂ�܂��B
	SvgMap container;
//	SvgMap[][] tiles;
	HashMap<Long,SvgMap> tiles;
	double width , height;
	double minX , minY;
	int wPart , hPart;
	double meshWidth , meshHeight;
	public boolean tiled  = false; // �^�C����������Ă�����true
	boolean hasId = false; // RDFXML���^�f�[�^������Ƃ�
	boolean hasMicroMeta = false;
	Integer[] linkedMetaIndex;
	String[] linkedMetaName;
	boolean noMetaId = false;
	
	String subFile , href , dir;
	
	SvgMapAffineTransformer smat;
	
	int bitimageGlobalTileLevel = -1;
	
	// �X�̃^�C����BBOX
	
	// [tileCount][tileCount]�ɂ��I�[�o�[�t���[�h�~�̂��߃n�b�V����
	// http://www.atmarkit.co.jp/fjava/javatips/081java010.html
	// �����������̒l�́A�e�[�u���ɂ��Ă����قǂ̂��́H�ȒP�ɎZ�o�ł���C������ 2014.2.13
	HashMap<Long,Double> x0;
	HashMap<Long,Double> x1;
	HashMap<Long,Double> y0;
	HashMap<Long,Double> y1;
	
	
	// �n�����^�f�[�^�p
//	boolean[][] hasFigure;
	HashSet<Long> hasFigure; // default: false
	ArrayList<HashSet<Long>> elem = new ArrayList<HashSet<Long>>();
	int elementCount = 0;
	String idPrefix = "f";
	
	// 2007.07.23
	// �K�w�I�^�C�������A���S���Y���p
	// �t�@�C���T�C�Y�I�[�o�[���o
	public boolean outOfSize;
	public boolean allTilesOOS; // outOfSize��true�ŁA��ȏ�̃^�C���ŃT�C�Y�I�[�o�[���N���Ă���
//	public boolean[][] parentTileExistence; // �e�̃^�C���̑��݁i�������̂̂ݍ쐬����j���̔z��́A���̔z��̔����̑傫���i�g���Ƃ��͂Q�{�ɍL���Ďg���j
	HashSet<Long> parentTileExistence; // default: true
	
//	public boolean[][] thisTileExistence; // ���̃��x���̃^�C���̑��݁E�E�{���ɂ��̃t���O��true�̂��̑S�Ẵ^�C�����L��킯�ł͂Ȃ��A�e�̃^�C���ɖ����t���O���t���Ă�����̂��������ۂɑ��݂��Ă���B���Ȃ킿�A���̃t���O�������Ă�����̂́A���̐e�̃��x���ɑk���Ă����΂ǂ����ɑ�������^�C�������݂��Ă��邱�Ƃ��Ӗ�����B�@�t�Ɍ����΁A���̃t���O��false�̂��̂́A���̉��̃��x���Ƀ^�C�����L�邱�Ƃ������Ă���B
	HashSet<Long> thisTileExistence; // default: true�@�E�E�E�@���ǂ̂Ƃ���A���̃Z�b�g�ɓ����Ă�����̂̓I�[�o�[�t���[�����^�C���ԍ�(getHashKey( index1 , index2 ))�̃��X�g�ł��@2016.5.12
	
	//	public boolean[][] thisTileHasElements;// ���̃^�C���̒��ɗL�ӂȐ}�`�v�f���L����̂Ƀt���O������(�I�[�o�[�t���[���Ă�����A�J���̂��̂�false)
	HashSet<Long> thisTileHasElements; // default: false
	public int level = 0; // level = 0 : lvl = 1 , lvl = 2^level �ċA�^�C�������̃��x��
//	public vois set
//	boolean oos; // �O���[�o���ϐ��p�~ 2016.5
	String lvls = "";
	boolean hasContainer = false;
	boolean tileDebug = false;
	public boolean isSvgTL = true;
	
	NumberFormat nf;
	
	ExecutorService svgMapExecutorService; // for multi thread 2017.4.19
	ArrayList<SvgMapExporterRunnable> svgMapExporterRunnables;
	
	SvgMapTilesTM(){
		System.out.println("NULL SvgMapTilesTM instanciated");
	}
	
	SvgMapTilesTM(String svgFileName , NumberFormat nf , double xc0 , double yc0 , double w , double h , int wp , int hp , int lvl , HashSet<Long> parentTiles , SvgMapAffineTransformer st , vectorPoiShapes vps , boolean isSvgTLp , int maxThreadsp , ExecutorService svgMapExecutorServicep ) throws Exception{
		isSvgTL = isSvgTLp;
		maxThreads = maxThreadsp;
		svgMapExecutorService = svgMapExecutorServicep;
		
		SvgMapTilesBuilder( svgFileName , nf , xc0 , yc0 , w , h , wp , hp , lvl , parentTiles , st , vps);
	}
	
	// xc0,yc0: �R���e���c��bbox���_ w,h:�R���e���c��bbox�T�C�Y hp,wp:���������� lvl:�ċA�������x��(���������ƍ��킹���ۂ̕�������)
	private void SvgMapTilesBuilder(String svgFileName , NumberFormat nf0 , double xc0 , double yc0 , double w , double h , int wp , int hp , int lvl , HashSet<Long> parentTiles , SvgMapAffineTransformer st , vectorPoiShapes vps ) throws Exception{
//		System.out.println("NEW SvgMapTilesTM instanciated");
		
//		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
//		MemoryUsage usage = mbean.getHeapMemoryUsage();
//		System.out.printf("�ő�T�C�Y�F%10d%n", usage.getMax());
//		System.out.printf("�g�p�T�C�Y�F%10d%n", usage.getUsed());
		
		nf = nf0;
		smat = st;
		level = lvl;
		int mpl = 1;
		if ( level == 0 ){
			parentTileExistence = new HashSet<Long>();
			for ( int i = 0 ; i < (wp / 2) + 1 ; i++ ){
				for ( int j = 0 ; j < (hp / 2) + 1 ; j++ ){
					set2DHashB(parentTileExistence,i,j,false,true);
				}
			}
		} else {
			parentTileExistence = parentTiles;
		}
		for ( int i = 0 ; i < level ; i++ ){
			mpl = mpl * 2;
		}
		width = w;
		height = h;
		// wPart,hPart�̓��x�������Ǝw�蕪�������킹��������
		// wp,hp�͎w�蕪���ł̎w�萔
		wPart = wp * mpl;
		hPart = hp * mpl;
		minX = xc0;
		minY = yc0;
		outOfSize = false;
		allTilesOOS = false;
		meshWidth = width / wPart;
		meshHeight = height / hPart;
		
		
		
		tiles = new HashMap<Long,SvgMap>();
		
		thisTileExistence = new HashSet<Long>();
		thisTileHasElements = new HashSet<Long>();
		
		x0 = new HashMap<Long,Double>();
		x1 = new HashMap<Long,Double>();
		y0 = new HashMap<Long,Double>();
		y1 = new HashMap<Long,Double>();
		
		hasFigure = new HashSet<Long>();
		
		System.out.println("Lvl:" + level + " Part:h:" + hPart + " w:" + wPart );
		
		int[] tileIndex;
		if ( hPart == 1 && wPart == 1 ){ // �^�C������Ă��Ȃ�
			container = new SvgMap( svgFileName ,  nf , vps);
			container.isSvgTL = isSvgTL;
			container.appendableCustomAttr = false; // ���Ǐ��appendable�͂܂����E�E�E2017.6.30
			set2DHashSM( tiles, 0, 0, container);
			hasContainer = true; // �R���e�i���L��ƍ��̂��ăR���e�i���g�ɐ}�`��`�悵�Ă���E�E�g���b�L�[
			tiled = false;
			set2DHashD( x0 , 0 , 0 , minX );
			set2DHashD( y0 , 0 , 0 , minY );
			set2DHashD( x1 , 0 , 0 , minX + meshWidth );
			set2DHashD( y1 , 0 , 0 , minY + meshHeight );
			
			
			tileList = new ArrayList<Object>();
			tileIndex = new int[2];
			tileIndex[0]=0;
			tileIndex[1]=0;
			tileList.add(tileIndex);
			initialEffectiveTileCount = 1;
		} else { // �^�C������Ă���
			// ���x�����O�̏ꍇ(mesh�I�v�V�����ɂ��^�C����)�A�܂���level���P�ŁA������mesh�I�v�V�����Ń^�C��������Ă��Ȃ��ꍇ
			// ���̔��f���[�`�����A�ꕔ�Ӗ��Ȃ�����������(���x���P�ł�wp,hp>1�����Ȃ�����) 2013.8.6
			if ( level == 0 || ( level == 1 && wp == 1 && hp == 1 ) ){
				container = new SvgMap( svgFileName ,  nf , vps);
				container.isSvgTL = isSvgTL;
				hasContainer = true;
			} 
			if ( level > 0 ){
				lvls = "_l" + level;
			}
			tiled = true;
			buildTileList();
			subFile = svgFileName.substring(0 , svgFileName.indexOf(".svg"));
			System.out.println("Total Effective Tiles Count:" + tileList.size() );
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
//						System.out.println("create tile: j,i:" + j+","+i);
						SvgMap tileMap = new SvgMap( subFile + lvls + "_" + j + "-" + i + ".svg" , nf , vps);
						tileMap.appendableCustomAttr = false; // �^�C�����O�̏ꍇ��CustomAttr�́Aappendable�ł͂Ȃ�����B�i�ꔭ�����j
						set2DHashSM(tiles, j, i, tileMap ); // �����overFlow
						(get2DHashSM( tiles, j, i)).isSvgTL = isSvgTL;
						set2DHashD( x0 , j , i , minX + j * meshWidth );
						set2DHashD( y0 , j , i , minY + i * meshHeight );
						set2DHashD( x1 , j , i , minX + j * meshWidth + meshWidth );
						set2DHashD( y1 , j , i , minY + i * meshHeight+ meshHeight );
			}
		}
		buildSubTileSets();
//		svgMapThreads = new ArrayList<Thread>();
		svgMapExporterRunnables = new ArrayList<SvgMapExporterRunnable>();
	}
	
	// ��ł��I�u�W�F�N�g������f�[�^������out of size������ 2016.4.8
	public void setForceOos(boolean forceOos){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).forceOos = forceOos;
		}
	}
	
	public void setDefauleCustomAttr( boolean isDefaultCustomAttr ){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).isDefaultCustomAttr = isDefaultCustomAttr;
		}
	}
	
	ArrayList<Object> tileList; // tileList�́A(��̊K�w�܂ők���Ă�)��������Ă��Ȃ��^�C���̃��X�g
//	int[] tileIndex; // �O���[�o���ϐ����}���`�X���b�h���łЂǂ��o�O�������N�����Ă����E�E�E 2016.04
	long initialEffectiveTileCount;
	private void buildTileList(){
		int[] tileIndex;
		tileList = new ArrayList<Object>();
		
		// parentTileExistence �́@false�̂��̂������Ă���
		// ���Ȃ킿�AtileList�ɂ́A(��̊K�w�܂ők���Ă�)��������Ă��Ȃ��^�C���̃��X�g�����������B
		Iterator<Long> it = parentTileExistence.iterator();
		while( it.hasNext()){
			int[] idx = getIndex( it.next() );
			for ( int ii = 0 ; ii < 2 ; ii++ ){
				for ( int jj = 0 ; jj < 2 ; jj++ ){
					int i = idx[1] * 2 + ii;
					int j = idx[0] * 2 + jj;
					if ( i < hPart && j < wPart ){
						if ( get2DHashB(thisTileExistence, j, i, true) ){ // ���̔��f�͕K�v�H(2014.2.14)
							tileIndex = new int[2];
							tileIndex[0] = i;
							tileIndex[1] = j;
							
							tileList.add(tileIndex);
						}
					}
				}
			}
		}
		
//		printTileList();
		initialEffectiveTileCount = tileList.size();
	}
	
	private void printTileList(){
		for ( int i = 0 ; i < tileList.size() ; i++ ){
			int[] tileIndex = (int[])tileList.get(i);
			System.out.println(tileIndex[1] + ":" + tileIndex[0]);
		}

	}
	
	private class PutHeader extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).putHeader( minX + j * meshWidth ,minY + i * meshHeight , meshWidth , meshHeight );
			return k;
		}
	}
	public void putHeader( ) throws Exception{
		if ( hasContainer ){
			container.putHeader( minX ,minY , width , height );
		}
		if ( tiled ){
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).putHeader( minX + j * meshWidth ,minY + i * meshHeight , meshWidth , meshHeight );
			}
		}
	}
	
	
	String defaultFill = "";
	String tileRectFill = defaultFill;
	
	private class SetDefaultStyle extends svgMapCommands{
		String defFill;
		double defStrokeWidth;
		String defStroke;
		double defOpacity;
		boolean vectorEffect;
		SetDefaultStyle( String defFill , double defStrokeWidth , String defStroke , double defOpacity , boolean vectorEffect ){
			this.defFill = defFill;
			this.defStrokeWidth = defStrokeWidth;
			this.defStroke = defStroke;
			this.defOpacity = defOpacity;
			this.vectorEffect = vectorEffect;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setDefaultStyle( defFill , defStrokeWidth , defStroke , defOpacity , vectorEffect );
			return k;
		}
	}
	public void setDefaultStyle( String defFill , double defStrokeWidth , String defStroke , double defOpacity , boolean vectorEffect ) throws Exception{
		defaultFill = defFill;
		if ( tileRectFill.equals("") ){
			tileRectFill = defaultFill;
		}
		svgMapCommands pu = new SetDefaultStyle( defFill , defStrokeWidth , defStroke , defOpacity , vectorEffect );
		bufferedDraw( pu , false );
	}
	
	public void setTileRectFill( String tFill ){
		tileRectFill = tFill;
	}
	
	private class SetDefaultCaptionStyle extends svgMapCommands{
		double defaultFontSize;
		boolean strokeGroup;
		SetDefaultCaptionStyle( double defaultFontSize , boolean strokeGroup ){
			this.defaultFontSize = defaultFontSize;
			this.strokeGroup = strokeGroup;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setDefaultCaptionStyle( defaultFontSize , strokeGroup );
			return k;
		}
	}
	public void setDefaultCaptionStyle( double defaultFontSize , boolean strokeGroup ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setDefaultCaptionStyle( defaultFontSize , strokeGroup );
//					checkOutOfSize( oos , j , i );
		}
	}
	
	
	public void putFooter() throws Exception{
		putFooter( 0 , null );
	}
	
	public void putFooter( int densityControl ) throws Exception{
		putFooter( densityControl , null );
	}
	
	public HashMap<Long,Integer> divErr = null;
	
	public HashMap<Long,Integer> putFooter( int densityControl , HashMap<Long,Integer> parentDivErr ) throws Exception{
		// �����G���[�J�E���^�[��ǉ� 2014.2.17
		// �ԋp�l: �e���番�����ꂽ�S�̃^�C���̂����P�����^�C���������ł��Ȃ������ꍇ�A���̒l���C���N�������g����� Key�̓^�C���̔ԍ�
		// parentDivErr: �e�̕����G���[�J�E���^�[�i�Ăь�����^���Ďg���K�v������j�@�e�^�C��(hash)�ɑ΂���K�w�I�G���[�A����������
		
		// 2017.4.19 �{����limit�𒴂������ǂ����͊esvgmaptile��putfooter close()�����Ȃ��Ɣ������Ȃ��̂ŁA
		// �{���́A�܂��͊e�^�C����putfooter����������ɁAlimit�z�����ēx�m�F����thisTileHasElements�ݒ�Ȃǂ̏��������ׂ�
		
		bufferedDraw(null,true); // �`��R�}���h�o�b�t�@�̃t���b�V�� : �X���b�h����thisTileExistence(overflowlist)���Z�܂�(2016.4.8)
		
		divErr = null;
		
		if ( tiled ){
//			System.out.println("initialTileListSize:"+tileList.size());
			aggregateTileList(); // tileList�F���̎��_�ŃI�[�o�[�t���[���ĂȂ��^�C���̃��X�g�ɂȂ�i�������v�f�������^�C���͂܂����蓾��j
//			System.out.println("overflowedTileSize:"+thisTileExistence.size()+"   ReducedTilieListSize:"+tileList.size());
			
			if ( hasId ){
				elem.add(hasFigure);
			}
			int p = subFile.lastIndexOf("\\");
			if (p<0){
				p = subFile.lastIndexOf("/");
			}
			if (p<0){
				p=0;
			} else {
				p = p + 1;
			}
			href = subFile.substring(p);
			dir = subFile.substring(0,p);
			
			
			// ���̃^�C�����f�[�^�������Ă��邱�Ƃ������t���O��thisTileHasElements�ɗ��ĂĊ�������
			// �I�[�o�[�t���[�����Ă��Ȃ����A�J���ł��Ȃ��^�C��
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				if ( (get2DHashSM( tiles, j, i)).nElements > 0 ){
					set2DHashB( thisTileHasElements, j, i, true, false);
				}
			}
			
			
			if ( parentDivErr != null ){
				divErr = new HashMap<Long,Integer>();
				// �Z��̑��̎O�̃^�C���ɗv�f�����݂��Ă��Ȃ��ꍇ�A�����G���[
				Iterator<Long> it = parentTileExistence.iterator();
				while( it.hasNext()){
					int[] idx = getIndex( it.next() );
					int divc = 0;
					for ( int ii = 0 ; ii < 2 ; ii++ ){
						for ( int jj = 0 ; jj < 2 ; jj++ ){
							int i = idx[1] * 2 + ii;
							int j = idx[0] * 2 + jj;
							if ( i < hPart && j < wPart ){
								if ( get2DHashB( thisTileHasElements, j, i, false) || ! get2DHashB( thisTileExistence, j, i, true) ){ // ���̃^�C�����v�f�������A�I�[�o�[�t���[���Ă���ꍇ
									++ divc;
								}
							}
						}
					}
					for ( int ii = 0 ; ii < 2 ; ii++ ){
						for ( int jj = 0 ; jj < 2 ; jj++ ){
							int i = idx[1] * 2 + ii;
							int j = idx[0] * 2 + jj;
							if ( i < hPart && j < wPart ){
								if ( divc > 1 ){ // �Q�ȏ�̃^�C���ɃG�������g������ꍇ�̓G���[�N���A����
									set2DHashI( divErr, j, i, 0 );
								} else { // �����łȂ��ꍇ�A�e�̃G���[�J�E���g�{�P���ē����
									set2DHashI( divErr, j, i, get2DHashI( parentDivErr, idx[0], idx[1] ) + 1 );
								}
							}
						}
					}
				}
			}
			
			
			
			if ( hasId ){
				putMetadataToTiles(  );
			}
			
			
			// 
			// parentTileExistence �́@false�̂��̂������Ă���
			Iterator <Long> it = parentTileExistence.iterator();
			while( it.hasNext()){
				int[] idx = getIndex( it.next() );
				for ( int ii = 0 ; ii < 2 ; ii++ ){
					for ( int jj = 0 ; jj < 2 ; jj++ ){
						int i = idx[1] * 2 + ii;
						int j = idx[0] * 2 + jj;
						if ( i < hPart && j < wPart ){
							if ( get2DHashB(thisTileExistence, j, i, true) ){ // ���̔��f�͕K�v�H(2014.2.14)
//								System.out.print("sv File:"+ j + ","+i+"  :");
//								System.out.println (" : Size:"+(get2DHashSM( tiles, j, i)).svgFile.length()+"  limit:"+(get2DHashSM( tiles, j, i)).limit);
								
								(get2DHashSM( tiles, j, i)).putFooter();
							}
							if ( ! get2DHashB(thisTileExistence, j, i, true) ){
//								System.out.print("rm File:"+ j + ","+i+"  :");
//								System.out.println (" : Size:"+(get2DHashSM( tiles, j, i)).svgFile.length()+"  limit:"+(get2DHashSM( tiles, j, i)).limit);
								(get2DHashSM( tiles, j, i)).removeFile();
							}
						}
					}
				}
			}
			
		}
		
		if ( hPart * wPart == 1 ){ // �^�C������ĂȂ� ���else�ŗǂ��̂ł́H
			divErr = new HashMap<Long,Integer>();
			set2DHashI( divErr, 0, 0, 0 );
			set2DHashB( thisTileHasElements, 0, 0, true, false);
			container.putFooter();
		}
		
		
		System.out.println("TileCount: Effective:" + initialEffectiveTileCount + " Generated:" + thisTileHasElements.size()  + " Empty:" +  (tileList.size() - thisTileHasElements.size()) + " Overflowed:" + thisTileExistence.size() );
		return ( divErr );
	}
	
	String layerMetadata="";
	// ���C���[���[�g�R���e�i�ɑ΂���<metadata>�v�f���ɔC�ӂ̂߂��[�f�[�^�������ǉ�����@�\  2019/1/24
	// escape���K�v�Ȃ�΁ASvgMapTilesTM.htmlEscape()�Ȃǂ��g���āA�����ōs�������̂����Ă��������B
	// putCrs()�O�Ȃ炢�Ă�ł��ǂ�
	public void setLayerMetadata(String layerMetadata){
		this.layerMetadata = layerMetadata;
	}
	
	double commonA, commonB, commonC, commonD, commonE, commonF;
	public void putCrs( double a ,  double b , double c , double d , double e , double f ) throws Exception{
		commonA = a;
		commonB = b;
		commonC = c;
		commonD = d;
		commonE = e;
		commonF = f;
		if ( hasContainer ){
			if ( layerMetadata.length() > 0 ){
				container.setUserMetadata( layerMetadata );
			}
			container.putCrs(  a ,  b , c , d , e , f );
		}
		if ( tiled ){
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
						(get2DHashSM( tiles, j, i)).putCrs(  a ,  b , c , d , e , f );
//						checkOutOfSize( oos , j , i );
			}
		}
	}
	
	private class PutComment extends svgMapCommands{
		// �R���e�i�݂̂ɃR�����g������
		String comment;
		PutComment( String comment ){
			this.comment = comment;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( i == 0 && j == 0 ){
				(get2DHashSM( tiles, j, i)).putComment( comment );
			}
			return k;
		}
	}
	public void putComment( String comment ) throws Exception{
		if ( !tiled ){
			svgMapCommands pu = new PutComment( comment );
			bufferedDraw( pu , false );
		} else {
//		System.out.println("called putComment hasContainer?:"+hasContainer + "  val:"+comment);
			if ( hasContainer ){
				container.putComment( comment );
			}
		}
	}
	
	private class PutCommentToAll extends svgMapCommands{
		String comment;
		PutCommentToAll( String comment ){
			this.comment = comment;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).putComment( comment );
			return k;
		}
	}
	public void putCommentToAll( String comment ) throws Exception{
		svgMapCommands pu = new PutCommentToAll( comment );
		bufferedDraw( pu , false );
	}
	
	private class SetId extends svgMapCommands{
		String idNumb;
		SetId( String idNumb ){
			this.idNumb = idNumb;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setId( idNumb );
			return k;
		}
	}
	public void setId( ) throws Exception{
		if ( tiled ){
			if ( elementCount > 0 ){
				elem.add(hasFigure);
			} else {
				hasId = true;
			}
		}
		hasFigure = new HashSet<Long>();
		svgMapCommands pu = new SetId( idPrefix + elementCount );
		bufferedDraw( pu , false );
		++ elementCount;
	}
	
	private class SetAnchor extends svgMapCommands{
		String title;
		String link;
		SetAnchor(String title , String link){
			this.title = title;
			this.link = link;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setAnchor(title , link) ;
			return k;
		}
	}
	public void setAnchor(String title , String link) throws Exception{
		svgMapCommands pu = new SetAnchor( title , link);
		bufferedDraw( pu , false );
	}
	
	private class TermAnchor extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termAnchor() ;
			return k;
		}
	}
	public void termAnchor() throws Exception {
		svgMapCommands pu = new TermAnchor();
		bufferedDraw( pu , false );
	}
	
	
	private class PutPolyline extends svgMapCommands{
		Coordinate[] coord = null;
		String strokeColor;
		double strokeWidth;
		double opacity;
		
		PolygonDouble pol = null;
		Envelope env = null;
		
		PutPolyline( Coordinate[] coord , String strokeColor , double strokeWidth , double opacity ){
			this.coord = coord;
			this.strokeColor = strokeColor;
			this.strokeWidth = strokeWidth;
			this.opacity = opacity;
		}
		
		PutPolyline( PolygonDouble pol , String strokeColor , double strokeWidth , Envelope env , double opacity ){
			this.pol = pol;
			this.strokeColor = strokeColor;
			this.strokeWidth = strokeWidth;
			this.env = env;
			this.opacity = opacity;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer = null;
			if ( pol == null ){
				answer = clip( j , i , coord , false);
			} else {
				answer = clip( j , i , pol , false , env );
			}
			
			if (answer.npoints > 1){
				boolean oos = (get2DHashSM( tiles, j, i)).putPolyline( answer , strokeColor , strokeWidth , opacity );
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
	}
	
	public void putPolyline( Coordinate[] coord , String strokeColor , double strokeWidth , double opacity ) throws Exception{
		svgMapCommands pu = new PutPolyline( coord , strokeColor , strokeWidth , opacity );
		bufferedDraw( pu , false );
	}
	
	public void putPolyline( PolygonDouble pol , String strokeColor , double strokeWidth , Envelope env , double opacity ) throws Exception{
		svgMapCommands pu = new PutPolyline( pol , strokeColor , strokeWidth , env , opacity );
		bufferedDraw( pu , false );
	}
	
	private class SetInterior extends svgMapCommands{
		Coordinate[] coord = null;
		PolygonDouble pol = null;
		Envelope env = null;
		SetInterior( Coordinate[] coord ){
			this.coord = coord;
		}
		
		SetInterior( PolygonDouble pol , Envelope env ){
			this.pol = pol;
			this.env = env;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer;
			if ( coord == null ){
				answer= clip( j , i , pol , true , env );
			} else {
				answer = clip( j , i , coord , true);
			}
			if (answer.npoints > 1){
				(get2DHashSM( tiles, j, i)).setInterior( answer );
//						checkOutOfSize( oos , j , i );
			}
			return ( k );
		}
		
	}
	
	public void setInterior( Coordinate[] coord ) throws Exception{
		svgMapCommands pu = new SetInterior( coord );
		bufferedDraw( pu , false );
	}
	
	public void setInterior( PolygonDouble pol , Envelope env ) throws Exception{
		svgMapCommands pu = new SetInterior( pol , env );
		bufferedDraw( pu , false );
	}
	
	
	private class SetExterior extends svgMapCommands{
		Coordinate[] coord=null;
		PolygonDouble pol=null;
		Envelope env=null;
		SetExterior( Coordinate[] coord ){
			this.coord = coord;
		}
		
		SetExterior( PolygonDouble pol , Envelope env ){
			this.pol = pol;
			this.env = env;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer;
			if ( coord == null ){
				answer= clip( j , i , pol , true , env );
			} else {
				answer = clip( j , i , coord , true);
			}
			if (answer.npoints > 1){
				(get2DHashSM( tiles, j, i)).setExterior( answer );
//						checkOutOfSize( oos , j , i );
			}
			return ( k );
		}
	}
	
	public void setExterior( Coordinate[] coord ) throws Exception{
		svgMapCommands pu = new SetExterior( coord );
		bufferedDraw( pu , false );
	}
	
	public void setExterior( PolygonDouble pol , Envelope env ) throws Exception{
		svgMapCommands pu = new SetExterior( pol , env );
		bufferedDraw( pu , false );
	}
	
	private class PutPolygon extends svgMapCommands{
		String fillColor; 
		double strokeWidth;
		String strokeColor;
		double opacity;
		PutPolygon(String fillColor , double strokeWidth , String strokeColor , double opacity ){
			this.fillColor = fillColor;
			this.strokeWidth = strokeWidth;
			this.strokeColor = strokeColor;
			this.opacity = opacity;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			boolean oos = (get2DHashSM( tiles, j, i)).putPolygon( fillColor , strokeWidth , strokeColor , opacity );
			return( checkOutOfSize( oos , j , i , k , subTileInfo ));
		}
	}
	
	public void putPolygon(String fillColor , double strokeWidth , String strokeColor ) throws Exception {
		putPolygon( fillColor , strokeWidth , strokeColor , 1.0 );
	}
	
	public void putPolygon(String fillColor , double strokeWidth , String strokeColor , double opacity ) throws Exception {
		svgMapCommands pu = new PutPolygon( fillColor , strokeWidth , strokeColor , opacity );
		bufferedDraw( pu , false );
	}
	
	
	private class SetGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setGroup();
			return k;
		}
	}
	public void setGroup() throws Exception{
		svgMapCommands pu = new SetGroup();
		bufferedDraw( pu , false );
	}
	
	private class TermGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termGroup();
			return k;
		}
	}
	public void termGroup() throws Exception{
		svgMapCommands pu = new TermGroup();
		bufferedDraw( pu , false );
	}
	
	// geomertry collection(�{����Group)��metadata���ݒ肳��Ă���ƁA
	// ���ꂪ�ŏ���geometry�ɂ����ݒ肳��Ȃ����Ƃւ̑΍� 2014.5.1
	private class SetShadowGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setShadowGroup();
			return k;
		}
	}
	public void setShadowGroup()  throws Exception{
		svgMapCommands pu = new SetShadowGroup();
		bufferedDraw( pu , false );
	}
	private class TermShadowGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termShadowGroup();
			return k;
		}
	}
	public void termShadowGroup() throws Exception{
		svgMapCommands pu = new TermShadowGroup();
		bufferedDraw( pu , false );
	}
	
	public void putText( Coordinate coo , double size , String attr ) throws Exception{
		putText( coo , size , attr , false , 0 );
	}
	
	public void putText( Coordinate coo , double size , String attr , boolean abs ) throws Exception{
		putText( coo , size , attr , abs , 0 );
	}
	
	
	private class PutText extends svgMapCommands{
		Coordinate coo;
		double size;
		String attr;
		boolean abs;
		double textShift;
		PutText( Coordinate coo , double size , String attr , boolean abs , double textShift ){
			this.coo = coo;
			this.size = size;
			this.attr = attr;
			this.abs = abs;
			this.textShift = textShift;
			
			// point�W�I���g���������̂��߂̃q���g�ݒ� 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // �t�����E�E�E
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){ // k== -1 ��pointHintKey���A�Ӗ��Ȃ�isInclude�𓮂����Ȃ����߂̉��݂�Hack�ł� 2017.4.18
				boolean oos = (get2DHashSM( tiles, j, i)).putText( coo , size , attr , abs , textShift );
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
	}
	
	public void putText( Coordinate coo , double size , String attr , boolean abs , double textShift ) throws Exception{
		svgMapCommands pu = new PutText( coo , size , htmlEscape( attr ) , abs , textShift ); // 2016.10.17debug
		bufferedDraw( pu , false );
	}
	
	
	
	// poi�̃f�t�H���g�T�C�Y��ݒ肷��
	private class SetDefaultPoiSize extends svgMapCommands{
		double flag;
		SetDefaultPoiSize( double flag ){
			this.flag = flag;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).defaultPoiSize = flag;
			return ( k );
		}
	}
	
	public void setDefaultPoiSize( double flag ) throws Exception{
		svgMapCommands pu = new SetDefaultPoiSize( flag );
		bufferedDraw( pu , false );
	}
	
	
	private class PutUse extends svgMapCommands{
		Coordinate coo;
		String fillColor;
		boolean fixed;
		String symbolId = null;
		
		PutUse( Coordinate coo , String fillColor , boolean fixed , String symbolId ){
			this.coo = coo;
			this.fillColor = fillColor;
			this.fixed = fixed;
			this.symbolId = symbolId;
			
			// point�W�I���g���������̂��߂̃q���g�ݒ� 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // �t�����E�E�E
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){
				boolean oos;
				if ( fixed == false && symbolId==null ){
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor );
				} else if ( symbolId == null ){
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor , fixed );
				} else {
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor , fixed , symbolId );
				}
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
		
	}
	
	// 2013.10.21 add
	public void putUse( Coordinate coo , String fillColor , boolean fixed , String symbolId ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , fixed , symbolId );
		bufferedDraw( pu , false );
	}
	
	public void putUse( Coordinate coo , String fillColor , boolean fixed ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , fixed , null );
		bufferedDraw( pu , false );
	}
	
	public void putUse( Coordinate coo , String fillColor ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , false , null );
		bufferedDraw( pu , false );
	}
	
	public void putSymbol( String templateData ) throws Exception{ // added 2013.3.11 �J�X�^���V���{����`���e�^�C���ɐݒ�
		// putSymbol�̑O�ɌĂ΂Ȃ��ƈӖ����Ȃ�
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean success = (get2DHashSM( tiles, j, i)).putSymbol( templateData );
			if ( !success ){
				System.out.println("ERROR!!! : Can't set custom symbol definition. EXIT.");
				System.exit(0);
			}
		}
	}
	
	public void putSymbol(double size ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean oos = (get2DHashSM( tiles, j, i)).putSymbol( size );
		}
	}
	
	public void putSymbol(double size , double fixedStrokeWidth ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean oos = (get2DHashSM( tiles, j, i)).putSymbol( size , fixedStrokeWidth );
		}
	}
	
	// 2012.7.30 add (POI���C�ɔ���)
	private class PutPoiShape extends svgMapCommands{
		Coordinate coo;
		int type;
		double poiSize;
		String fillColor;
		double strokeWidth;
		String strokeColor;
		boolean nonScalingStroke;
		boolean nonScalingObj;
		PutPoiShape( Coordinate coo , int type , double poiSize , String fillColor , double strokeWidth , String strokeColor , boolean nonScalingStroke , boolean nonScalingObj ){
			this.coo = coo;
			this.type = type;
			this.poiSize = poiSize;
			this.fillColor = fillColor;
			this.strokeWidth = strokeWidth;
			this.strokeColor = strokeColor;
			this.nonScalingStroke = nonScalingStroke;
			this.nonScalingObj = nonScalingObj;
			
			// point�W�I���g���������̂��߂̃q���g�ݒ� 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // �t�����E�E�E
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){
				boolean oos = (get2DHashSM( tiles, j, i)).putPoiShape( coo , type , poiSize , fillColor , strokeWidth , strokeColor , nonScalingStroke , nonScalingObj );
				// ���~�b�^�[�𒴂�����Aoos��false��Ԃ�
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return k;
		}
	}
	public void putPoiShape( Coordinate coo , int type , double poiSize , String fillColor , double strokeWidth , String strokeColor , boolean nonScalingStroke , boolean nonScalingObj ) throws Exception{
		svgMapCommands pu = new PutPoiShape( coo , type , poiSize , fillColor , strokeWidth , strokeColor , nonScalingStroke , nonScalingObj );
		bufferedDraw( pu , false );
	}
	
	
	private boolean isInclude(int w , int h , Coordinate coo ){
		if (tiled){
			boolean ret = false;
//System.out.println("include:" +  coo.x + "," + coo.y + "  bb:" + x0[w][h] + "," + y0[w][h] + " " + x1[w][h] + "," + y1[w][h]);
//			if ( coo.x >= x0[w][h] && coo.x <= x1[w][h] && coo.y >= y0[w][h] && coo.y <= y1[w][h] ){}
//			if ( coo.x >= get2DHashD( x0, w, h ) && coo.x <= get2DHashD( x1, w, h ) && coo.y >= get2DHashD( y0, w, h ) && coo.y <= get2DHashD( y1, w, h ) ){} // �W���X�g�̒l�̎��ɁA�ׂ̃^�C���Əd�����Đ�������Ă��܂�����fix 2016.8.30
			if ( coo.x >= get2DHashD( x0, w, h ) && coo.x < get2DHashD( x1, w, h ) && coo.y >= get2DHashD( y0, w, h ) && coo.y < get2DHashD( y1, w, h ) ){
				ret = true;
				if ( hasId ){
					set2DHashB( hasFigure, w, h, true, false);
				}
			}
	//		System.out.println( x0[w][h] + "," + x1[w][h] + "," + y0[w][h] + "," + y1[w][h]);
	//		System.out.println( "w:" + w + " h:" + h + " coo:" + coo + " ret:" + ret );
		return ( ret );
		} else {
			return (true);
		}
	}
	
	// 2017.4.17 point�n�v�f�̏����̍������i���[�v�ȗ��j�̂��߁Apoint�̍��W�l����Y������^�C���ԍ��𒼎Z�o����
	// ToDo: BBOX(���E��)�ł��������ׂ�
	private int[] getIncludedTileIndex( Coordinate coo ){
		int[] ans = new int[2];
		
		ans[1] = (int)( ( coo.x - minX ) / meshWidth ); // for j
		ans[0] = (int)( ( coo.y - minY ) / meshHeight ); // for i
		
		if ( ans[1] >= wPart ){ // debug 2018/6/28 ��̎����ƍő�l���͂ݏo���Ⴂ�܂��E�E�E
//			System.out.println("ERR tile width number exceeds : "+coo.x);
			ans[1] = wPart -1;
		}
		if ( ans[0] >= hPart ){
//			System.out.println("ERR tile height number exceeds : "+coo.y);
			ans[0] = hPart -1;
		}
		
//		System.out.println( "getIncludedTileIndex" + ans[0]+","+ans[1]);
		
//		System.out.println( "giti:"+ coo.x +","+ minX +","+ meshWidth);
		
		return ( ans );
	}
	
	
	private PolygonDouble clip( int w , int h , Coordinate[] coord , boolean filled ){
//		Rectangle2D.Double rect = new Rectangle2D.Double( x0[w][h] , y0[w][h] , meshWidth , meshHeight );
		Rectangle2D.Double rect = new Rectangle2D.Double( get2DHashD( x0, w, h ) , get2DHashD( y0, w, h ) , meshWidth , meshHeight );
//		System.out.println("clip:Copy to PolugonDouble:" + coord.length);
		PolygonDouble pol= new PolygonDouble(coord.length);
		for ( int i = 0 ; i < coord.length ; i++ ){
			pol.addPoint( coord[i].x , coord[i].y );
		}
		if (! filled ){
		// fill=none�̃p�X�E�|�����C���́A�n�_�ƏI�_�͌���ł͂����Ȃ��̂ŁA�n�_���N���b�v���ꂽ�[�_�Ƃ��Ă���
			pol.clippedEdge[0]=true;
		}
//		System.out.println("clip:StartClipping");
		
		if ( tiled ){
			ClipPolygonDouble cp = new ClipPolygonDouble(pol , rect);
			if ( hasId && cp.lastClipped.npoints > 1 ){
				set2DHashB( hasFigure, w, h, true, false);
			}
			return ( cp.getClippedPolygon() );
		} else {
			return(pol);
		}
	}
	
	private PolygonDouble clip( int w , int h , PolygonDouble pol , boolean filled , Envelope env){
		// �����������������Ă���̂͂ǂ����ƁE�E�E
		PolygonDouble ans;
//		Rectangle2D.Double rect = new Rectangle2D.Double( x0[w][h] , y0[w][h] , meshWidth , meshHeight );
		Rectangle2D.Double rect = new Rectangle2D.Double( get2DHashD( x0, w, h ) , get2DHashD( y0, w, h ) , meshWidth , meshHeight );
		//		System.out.println("clip:Copy to PolugonDouble:" + coord.length);
		
		if (! filled ){
		// fill=none�̃p�X�E�|�����C���́A�n�_�ƏI�_�͌���ł͂����Ȃ��̂ŁA�n�_���N���b�v���ꂽ�[�_�Ƃ��Ă���
			pol.clippedEdge[0]=true;
		}
//		System.out.println("clip:StartClipping");
		
		if ( tiled ){
//			Envelope tileEnv = new Envelope( x0[w][h] , x0[w][h]+meshWidth , y0[w][h] , y0[w][h]+meshHeight );
			Envelope tileEnv = new Envelope( get2DHashD( x0, w, h ) , get2DHashD( x0, w, h )+meshWidth , get2DHashD( y0, w, h ) , get2DHashD( y0, w, h )+meshHeight );
			if (tileEnv.contains(env)){
//				System.out.print("cont:");
				set2DHashB( hasFigure, w, h, true, false);
				ans = pol;
			} else if ( tileEnv.intersects( env ) ){
//				System.out.print("sect:");
				ClipPolygonDouble cp = new ClipPolygonDouble(pol , rect);
				if ( hasId && cp.lastClipped.npoints > 1 ){
					set2DHashB( hasFigure, w, h, true, false);
				}
				ans = cp.getClippedPolygon();
			} else {
//				System.out.print("outs:");
				// NULL�ł��B
				ans = new PolygonDouble(1);
			}
			
			return ( ans );
		} else {
			return(pol);
		}
	}

	SimpleFeatureType readFT;
	FeatureCollection<SimpleFeatureType,SimpleFeature> fsShape;
	String metaNs , metaUrl;
	public void setMetadata( SimpleFeatureType rFT , FeatureCollection<SimpleFeatureType,SimpleFeature> fS , String mns , String murl ) throws Exception{
		readFT = rFT;
		fsShape = fS;
		metaNs = mns;
		metaUrl = murl;
		if ( tiled == false ){
			putMetadataToContainer(  );
		}
	}
	
	public void setMicroMeta2Header( SimpleFeatureType rFT , boolean useTitleAttrParam ) throws Exception{
		useTitleAttr = useTitleAttrParam;
		hasMicroMeta = true;
		readFT = rFT;
		metaNs = "";
		metaUrl = "";
		String metaSchema ="";
		for ( int i = 0 ; i < linkedMetaName.length ; i ++ ){
			if ( linkedMetaName[i].indexOf("the_geom") == -1 ){
				metaSchema += linkedMetaName[i];
				if ( i < linkedMetaName.length -1 ){
					metaSchema += ",";
				}
			}
		}
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setMicroMeta2Header( metaSchema );
		}
	}
	
	public void setMicroMetaHeader( SimpleFeatureType rFT , String mns , String murl ) throws Exception{
		hasMicroMeta = true;
		readFT = rFT;
		metaNs = mns;
		metaUrl = murl;
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setMicroMetaHeader( metaNs , metaUrl);
		}
	}
	
	private void putMetadataToContainer(  ) throws Exception{
		if ( hasContainer ){
			SimpleFeature oneFeature;
			Object value;
			double dval=0;
			int i , j;
			FeatureIterator<SimpleFeature> reader = fsShape.features();
			j = 0;
			putMetaHeader(container);
			
			while (reader.hasNext()) {
				oneFeature = reader.next();
				putMetaElement( container , (String)( idPrefix + j ) , oneFeature );
				j++;
			}
			putMetaFooter( container );
		}
	}
		
	private void putMetadataToTiles(  ) throws Exception{
		SimpleFeature oneFeature;
		Object value;
		double dval=0;
		int i , j;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		j = 0;
		for ( int w = 0 ; w < wPart ; w++ ){
			for ( int h = 0 ; h < hPart ; h++ ){
				if ( ! get2DHashB(parentTileExistence, w/2, h/2, true)  && get2DHashB(thisTileExistence, w, h, true) ){
					putMetaHeader((get2DHashSM( tiles, w, h)));
//					checkOutOfSize( oos , w , h );
				}
			}
		}
		
		while (reader.hasNext()) {
			hasFigure = (HashSet<Long>)elem.get(j);
			oneFeature = reader.next();
			for ( int w = 0 ; w < wPart ; w++ ){
				for ( int h = 0 ; h < hPart ; h++ ){
					if ( get2DHashB( hasFigure, w, h, false) ){
						if ( ! get2DHashB(parentTileExistence, w/2, h/2, true )  && get2DHashB(thisTileExistence, w, h, true) ){
							putMetaElement( (get2DHashSM( tiles, w, h)) , (String)( idPrefix + j ) , oneFeature );
//							checkOutOfSize( oos , w , h );
						}
					}
				}
			}
			j++;
		}
		for ( int w = 0 ; w < wPart ; w++ ){
			for ( int h = 0 ; h < hPart ; h++ ){
				if ( ! get2DHashB(parentTileExistence, w/2, h/2, true)  && get2DHashB(thisTileExistence, w, h, true) ){
					putMetaFooter((get2DHashSM( tiles, w, h)));
//					checkOutOfSize( oos , w , h );
				}
			}
		}
	}
	
	private void putMetaHeader( SvgMap smp ) throws Exception{
		smp.putPlaneString("<metadata>\n");
		smp.putPlaneString(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
		smp.putPlaneString(" xmlns:" + metaNs + "=\"" + metaUrl + "\" >\n");
	}
	
	private void putMetaFooter( SvgMap smp ) throws Exception{
		smp.putPlaneString(" </rdf:RDF>\n");
		smp.putPlaneString("</metadata>\n");
	}
	
	private void putMetaElement(SvgMap smp , String elemId , SimpleFeature oneFeature ) throws Exception{
		smp.putPlaneString("  <rdf:Description rdf:about=\"" + elemId  + "\" ");
		for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
			if (oneFeature.getAttribute(i) instanceof Geometry == false ){
				smp.putPlaneString( metaNs + ":" + readFT.getDescriptor(i).getLocalName() + "=\"" + oneFeature.getAttribute(i) + "\" ");
			}
		}
		smp.putPlaneString("/>\n");
	}
	
	public boolean strIsSJIS = true;
	private class SetMicroMeta extends svgMapCommands{
		String metas;
		SetMicroMeta( String metas ){
			this.metas = metas;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setMicroMeta( metas );
			return k;
		}
	}
	public void setMicroMeta( SimpleFeature oneFeature ) throws Exception{
		int mi = 0;
		StringBuffer metaString = new StringBuffer();
		for ( int i = 0 ; i < linkedMetaIndex.length ; i++){
			if (oneFeature.getAttribute(linkedMetaIndex[i]) instanceof Geometry == false ){
				metaString.append( metaNs );
				metaString.append(":");
//				metaString.append( readFT.getAttributeType(linkedMetaIndex[i]).getName());
				metaString.append( linkedMetaName[i] );
				metaString.append("=\"");
				Object oneAttr = oneFeature.getAttribute( linkedMetaIndex[i] );
				if (oneAttr instanceof String){
//					System.out.print((String)oneAttr + " : " );
					if ( strIsSJIS ){
						// patch 2013/2/15 windows.....
//						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Shift_JIS");
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Windows-31J");
					} else {
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "UTF-8");
					}
					oneAttr = (Object)htmlEscape((String)oneAttr);
//					System.out.println(oneAttr);
				}
				metaString.append( oneAttr );
				metaString.append( "\" ");
			}
		}
		
		svgMapCommands pu = new SetMicroMeta( metaString.toString() );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	
	int titleAttrIndex = -1;
	boolean useTitleAttr = false;
	private class SetMicroMeta2 extends svgMapCommands{
		String metas;
		SetMicroMeta2( String metas ){
			this.metas = metas;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( ! noMetaId ){
				(get2DHashSM( tiles, j, i)).setId( idPrefix + elementCount );
			}
			(get2DHashSM( tiles, j, i)).setMicroMeta( metas );
			return k;
		}
	}
	public void setMicroMeta2( SimpleFeature oneFeature ) throws Exception{
		int mi = 0;
		StringBuffer metaString = new StringBuffer();
		String titleAttr ="";
		metaString.append( "content=\"");
		for ( int i = 0 ; i < linkedMetaIndex.length ; i++){
			if (oneFeature.getAttribute(linkedMetaIndex[i]) instanceof Geometry == false ){
				if ( titleAttrIndex == -1 ){
					titleAttrIndex = linkedMetaIndex[i];
				}
				Object oneAttr = oneFeature.getAttribute( linkedMetaIndex[i] );
				if (oneAttr instanceof String){
					if ( strIsSJIS ){
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Windows-31J");
					} else {
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "UTF-8");
					}
					if ( ((String)oneAttr).indexOf(",") > 0 ){ // �J���}���f�[�^�ɓ����Ă���Ƃ��̌듮��h���E�E�E 2015.4.17
						oneAttr=(Object)("'"+(String)oneAttr+"'");
					}
					oneAttr = (Object)htmlEscape((String)oneAttr);
				}
				if ( linkedMetaIndex[i] == titleAttrIndex ){
					titleAttr = oneAttr.toString();
				}
				metaString.append( oneAttr );
				if ( i < linkedMetaIndex.length -1 ){
					metaString.append( ",");
				}
			}
		}
		metaString.append("\" ");
		
		if ( titleAttrIndex != -1 && useTitleAttr ){
			metaString.append("xlink:title=\"" + titleAttr + "\" ");
		}
		
		svgMapCommands pu = new SetMicroMeta2( metaString.toString() );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	private class SetCustomAttribute extends svgMapCommands{
		String cAttr;
		SetCustomAttribute( String cAttr ){
			this.cAttr = cAttr;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setCustomAttribute( cAttr );
			return k;
		}
	}
	public void setCustomAttribute( String cAttr ) throws Exception{
		
		svgMapCommands pu = new SetCustomAttribute( cAttr );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	
	public int checkOutOfSize( boolean oos , int j , int i , int k , SubTileInfo subTileInfo ){
		if ( ! oos ){
//			System.out.println("Out Of Size!" + ":" + j + ":" + i + ":" + k);
			//���~�b�^�[�𒴂�����(oos��false��������)�A���̃^�C����Exsitence��false�ɂ���
//			set2DHashB( thisTileExistence, j, i, false, true); // ������̕��́A���̃��x���̏��������邽�߂̂��́B�@����̓X���b�h�Z�[�t����Ȃ��o�O
			subTileInfo.subTileOverflowSet.add(getHashKey(j,i));
			
			/**
			// �Y������A�I�[�o�[�t���[�����^�C�����΂�������subTileList�̗v�f�폜�ɑ���AsubTileOverflowSet��p����svgMapCommands��exec()���ōs�� 2017.4.19
			if ( k >=0){
				subTileInfo.subTileList.remove( k ); // (sub)tileList�̃I�[�o�[�t���[�^�C���v�f���O�����ƂŁA���̎��̃p�[�X����Y���^�C�����O���B
				--k;
			}
			**/
			
			if ( subTileInfo.subTileSet.size() == subTileInfo.subTileOverflowSet.size() ){
				subTileInfo.subAllTilesOOS = true; 
//				allTilesOOS = true; // �}���`�X���b�h�̏ꍇ�A�X���b�h����subTileList�ɑ΂��Ă݂̂̔��f�ɉ߂��Ȃ��̂Ńo�O2016/4/12
//			System.out.println("Sub All OOS!! : " + subTileInfo.subAllTilesOOS);
				
			}
			subTileInfo.subOutOfSize = true; // �ǂꂩ��ł��A�E�g�I�u�T�C�Y�̃^�C��������
//			System.out.println("OOS!! : " + subTileInfo.subOutOfSize);
		}
		return ( k );
	}
	
	public void setLimitter( long size ){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
					(get2DHashSM( tiles, j, i)).limit = size;
		}
	}
	
	
	// ���ʌ݊��p (Vector�̒��g��Array�̃o�[�W����) 2014.2.14
	public void createRContainer( Vector tileIndexArrays , Vector tileElementsArrays ) throws Exception{
		createRContainer( tileIndexArrays , tileElementsArrays , 0 );
	}
	
	// (Vector�̒��g��Array�̃o�[�W����) 2014.2.14
	public void createRContainer( Vector tileIndexArrays , Vector tileElementsArrays , int densityControl) throws Exception{
		Vector<HashSet<Long>> tileIndex = new Vector<HashSet<Long>>();
		Vector<HashSet<Long>> tileElements = new Vector<HashSet<Long>>();
		for ( int lvl = level ; lvl < tileIndexArrays.size() ; lvl++ ){
			boolean[][] tileIndexA , tileElementsA;
			tileIndexA = (boolean[][])tileIndexArrays.get(lvl);
			tileElementsA = (boolean[][])tileElementsArrays.get(lvl);
			tileIndex.addElement(array2HashSetB(tileIndexA,true));
			tileElements.addElement(array2HashSetB(tileElementsA,false));
		}
		createRContainer2( tileIndex , tileElements , densityControl);
	}
	
	//�K�w�I�ȃR���e�i���쐬���� 2012.4.10 ���x�ɉ������\������@�\�@densityContorol��ǉ�
	// Vector��HashSet���I(tileIndex��Existence�Ȃ̂�def:true, tileElements:def:false) 2014.2.14
	public void createRContainer2( Vector<HashSet<Long>> tileIndex , Vector<HashSet<Long>> tileElements , int densityControl) throws Exception{
		// tileIndex: �^�C���̑��ݗL��(tileExistence)�����x�����ɑ��K�w�Œ~�ς�������
		// tileElements: �^�C���ɗv�f�����邩�Ȃ���(thisTileHasElements)�����x�����ɑ��K�w�Œ~��
		System.out.println("CreateContainer2 : level: " + level );
		int wp = wPart;
		int hp = hPart;
		System.out.println("createRContainer wPart:"+wPart + " hPart:" + hPart );
		HashSet<Long> thisIndex , parentIndex , thisElements;
		
		HashMap<String,SvgMap> subContainers = new HashMap<String,SvgMap>();
//		HashMap parentContainers = new HashMap();
//		SvgMap subContainer;
		
		int startSize = ((HashSet<Long>)(tileIndex.get(level))).size();
		int vLevel = 0;
		int vSize = 1;
		while ( startSize > vSize ){
			vSize = vSize * 4;
			++vLevel;
		}
		
		
		boolean pFlg =false;
		int pStartLevel = (tileIndex.size()-1) % pStep; // ���̊K�w�R���e�i����郌�x����ݒ�
		pStartLevel -= vLevel;
		if ( pStartLevel < 1 ){
			pStartLevel = 1;
		}
			
//		if ( pStartLevel < 2 ){
//			pStartLevel += pStep;
//		}
		
		double mw , mh;
		String lvlStr;
		
		/** ����������ĊԈႢ�ȋC������E�E ��{�I�ɂ�(level��0��1�̏ꍇ�����Ȃ��Ȃ�)��ɂQ�{����΂����̂ł�
		for ( int lvl = 0 ; lvl < level  ; lvl ++ ){
			wp = wp * 2;
			hp = hp * 2;
		}
		**/
		
// 2013.8.6 level0����(level1�łȂ�)������̐������[�`�����g���悤�ɉ��C�������߁A*2�͕s�v�ɂȂ���
//		wp = wp * 2; 
//		hp = hp * 2;
		
		SvgMap sm;
		System.out.println("StartLevel:" + level );
		System.out.println("EndLevel:" + (tileIndex.size() - 1) );
		System.out.println("PstartLevel:" + pStartLevel );
		
		SvgMap rootContainer = container;
		
		int pDiv = 1;
		int pLevel = 0;
		
		//�ċA�I�ȃ^�C���̃R���e�i�𐶐����Ă���
		for ( int lvl = level ; lvl < tileIndex.size() ; lvl++ ){
			rootContainer.putPlaneString("<!-- LEVEL: " + lvl + " -->\n");
			// ���̃��x���ȉ��̃��x���ŁA�^�C�������邩�ǂ����𓾂� (����͘_���I�ɈӖ����Ȃ��Ǝv����@��������
//			HashSet<Long> allTiles = getAllTilesTable( lvl , tileIndex.size() - 1 , (HashSet<Long>)tileIndex.get(tileIndex.size() - 1));
			
			mw = width / wp;
			mh = height / hp;
			
			thisIndex = (HashSet<Long>)tileIndex.get( lvl );
			thisElements = (HashSet<Long>)tileElements.get( lvl );
			
			if ( lvl < 1 ){
//				parentIndex = new boolean[wp][hp]; // �{���͂��̔��������ǂ܂���������
				parentIndex = new HashSet<Long>();
				for ( int i = 0 ; i < wp ; i++ ){
					for ( int j = 0 ; j < hp ; j++ ){
						set2DHashB( parentIndex , i , j , false , true );
					}
				}
			} else {
				parentIndex = (HashSet<Long>)tileIndex.get( lvl - 1 );
			}
			
//			if (lvl == level){  //} level=0�̎�������_l�Ȃ��ŗǂ��P�[�X���Ǝv�������ǁA�����ł��Ȃ���ł����ˁE�E�E 2017.5.12
			if (lvl == 0){  // ��͂�o�O���o���悤�ł��̂łЂƂ܂������ėl�q���܂� 2017.8.4
				lvlStr = "";
			} else {
				lvlStr = "_l" + lvl;
			}
			
//			System.out.println("lvl:" + lvl );
			if ( lvl == pStartLevel ){
				// ���̊K�w�^�C�����x���ɓ�����
				pFlg = true;
				pStartLevel += pStep;		// parentTileExistence �́@false�̂��̂������Ă���

//				System.out.println("rec:" + lvl );
//				parentContainers = (HashMap)subContainers.clone();
//				subContainers = new HashMap();
			} else {
				pFlg = false;
			}
			
			// �f���V�e�B�ɉ������\������p(2012.4.10)
			double minZoom = -1;
			double minZoomPng = -1;
			if (densityControl > 0){
				minZoom = (int)( 10000 * densityControl / mh ) / 100.0;
				if ( lvl != level ){
					minZoomPng = minZoom / 2;
				}
			}
			
			int contI , contJ;
			
			String key ="";
//			System.out.println("subCs:" + subContainers.keySet());
			Iterator<Long> it = parentIndex.iterator();
			while( it.hasNext()){
				int[] idx = getIndex( it.next() );
				for ( int ii = 0 ; ii < 2 ; ii++ ){
					for ( int jj = 0 ; jj < 2 ; jj++ ){
						int j = idx[0] * 2 + jj;
						int i = idx[1] * 2 + ii;
						if ( j >= wp || i >= hp ){
							continue;
						}
						
						if ( pLevel > 0 ){ // �K�w�^�C�����g���Ă���ꍇ
							key =  pLevel + "_" + (j / pDiv) + "_" + (i / pDiv) ;
							boolean emp = subContainers.containsKey( key );
	//						System.out.println("get SubC  pLevel:" + pLevel + "key:" + key + " contains:" + emp + " key:" + subContainers.keySet());
							container = (SvgMap)(subContainers.get( key ));							///////
	//						System.out.println("subContainers:" + pLevel);
						}
	//					System.out.println( "TileExistence " + i + ":" + j + "=" +  thisIndex[j][i] );
						Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
	//					System.out.println("lvl:" + lvl + ":::parent:" + parentIndex.length + "," + parentIndex[0].length + ":::this:" + thisIndex.length + "," + thisIndex[0].length + ":" + j + "," + i );
						if ( ! get2DHashB( parentIndex, j/2, i/2, true)  && get2DHashB( thisIndex, j, i, true ) ){ // ���̃��x���Ƀ^�C�����L��ꍇ
							if ( get2DHashB( thisElements, j, i, false ) ){ // ���̃^�C�������炾�����珑���o���K�v�Ȃ�
	//							Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
	//							System.out.println ( "file:cont" + key + " : " + pt +","+ mw +","+ mh +","+ href + lvlStr + "_" + j + "-" + i + ".svg" );
								
								
								container.putImage( pt , mw , mh , href + lvlStr + "_" + j + "-" + i + ".svg" , minZoom , -1 );
	//							container.putPlaneString("<!-- write to l" + pLevel + ":" + (j / pDiv) + ":" + (i / pDiv) + " -->\n");
								if ( tileDebug  ){
									if ( densityControl > 0){
										if ( bitimageGlobalTileLevel >= 0){
	//										container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , -1 , minZoom );
											container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , minZoom );
											
											// debug
											/**
											container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
												"\" width=\"" + mw + "\" height=\"" + mh + 
												"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
												"visibleMinZoom=\"" + minZoomPng + "\" visibleMaxZoom=\"" + minZoom + "\" "+
												"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
											**/
											
										} else {
	//										System.out.println("plane Rect");
											putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , tileRectFill , minZoom );
										}
									} else {
										
										container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
											"\" width=\"" + mw + "\" height=\"" + mh + 
											"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
											"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
									}
								}
							}
						} else if ( (lvl < tileIndex.size() - 1) && ! get2DHashB( thisIndex, j, i, true ) ){ // ���̉��̃��x���̃R���e���c���L��ꍇ 
							if ( pFlg ){ //���̃��x���ɓ���A���̉��̃��x���̃R���e���c���L��ꍇ�A[���K�w]�R���e�i�𐶐�
		//						Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
								String subCname =  href + "_cont" + lvlStr + "_" + j + "-" + i + ".svg";
								container.putImage( pt , mw , mh , subCname , minZoom , -1 );
								
								if ( tileDebug && densityControl > 0 ){
									if ( bitimageGlobalTileLevel >= 0){
										
										// TODO? 2014.02.14
										// ���K�w�R���e�i�ɂȂ����Ƃ��ɁA�r�b�g�C���[�W��������悤�ȋC������H
										// 2015.9.4 ���m�F
									} else {
										putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , tileRectFill , minZoom );
									}
								}
								
		//						container.putHeader( minX ,minY , width , height );
		//						System.out.println ("make:" + subCname );
								sm = new SvgMap( dir + subCname , nf );
								sm.isSvgTL = isSvgTL;
		//						System.out.println("make subC "+ subCname);
								sm.putHeader( minX ,minY , width , height );
								sm.putCrs(commonA,commonB,commonC,commonD,commonE,commonF);
		//						sm.putPlaneString( "fileName:" + subCname );
		//						sm.putImage( pt , mw , mh , subCname );
		//						sm.putFooter();
								subContainers.put( lvl + "_" + j + "_" + i  , sm );
								
		//						container.putPlaneString("<!-- write to l" + pLevel + ":" + (j / pDiv) + ":" + (i / pDiv) + " -->\n");
								
							}
	//						if ( allTiles[j][i] && tileDebug && densityControl > 0 && bitimageGlobalTileLevel >= 0){}
								// ���̃��x���Ƀ^�C�����Ȃ��A���̃��x���Ƀ^�C��������A���r�b�g�C���[�W���[���]�^�C�����o�͂���ꍇ del
							if ( tileDebug && densityControl > 0 && bitimageGlobalTileLevel >= 0){
								// ���̃��x���Ƀ^�C�����Ȃ��A���r�b�g�C���[�W���[���]�^�C�����o�͂���ꍇ(allTiles�͌����I�ɏ��true�ł��傤 2014.2.14)
								container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , minZoom );
								/**
								// debug
								container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
									"\" width=\"" + mw + "\" height=\"" + mh + 
									"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
									"visibleMinZoom=\"" + minZoomPng + "\" visibleMaxZoom=\"" + minZoom + "\" "+
									"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
								**/
							}
						} else if ( (lvl == tileIndex.size() - 1) && ! get2DHashB( thisIndex, j, i, true ) ){ // ���łɍő啪�����x���ɓ��B���Ă���̂ɁA���̉��Ƀ^�C��������Ƃ����Ă���ꍇ�́A�I�[�o�[�t���[���ď�����ł��؂��Ă���^�C��(���ꏈ��) 2014.2.19
							if ( tileDebug  ){
								if ( densityControl > 0){
									if ( bitimageGlobalTileLevel >= 0){
										container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , -1 );
									} else {
										putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , "#ff0000" , -1 );
									}
								} else {
									
									container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
										"\" width=\"" + mw + "\" height=\"" + mh + 
										"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
										"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
								}
							}
						}
					}
				}
			}
			wp = wp * 2;
			hp = hp * 2;
			
			if ( pFlg  ){ // ���̊K�w�^�C�����x���ɓ�������
//				if ( pLevel == 0 ){ //�K�w�^�C�����x����0(���[�g)�̏ꍇ
//					System.out.println("close container");
//					container.putFooter();														///////
//				} else {
//				}
				pLevel = pStartLevel - pStep; // pLevel�́A���̊K�w�^�C�����x��
				pDiv = 2;
			} else {
				pDiv *= 2;
			}
//				System.out.println( "pDiv:" + pDiv );
			
		}
		
		System.out.println("close root container");
		rootContainer.putFooter();																	///////
		Iterator it = subContainers.entrySet().iterator();
		boolean hasSub = it.hasNext();
		if ( hasSub ){
			System.out.println("close multi container");
			while ( it.hasNext() ){
				Map.Entry entry = (Map.Entry) it.next();
				sm = (SvgMap)(entry.getValue());
				sm.putFooter();															//////
			}
		}
	}
	
	private void putAreaRect( SvgMap doc, double x , double y , double w , double h , String color , double MaxZoom ) throws Exception{
		String fillColor = "#90FF90";
//									System.out.println("putFooter : defaultFill:" + defaultFill);
		if (color.length()>0){
			fillColor = color;
		} else {
		}
		
		if ( true ){
			doc.putPlaneString("<rect x=\"" + nf.format(x) + "\" y=\"" + nf.format(y) + "\" width=\"" + nf.format(w) + "\" height=\"" + nf.format(h) + "\" fill-opacity=\"0.5\" fill=\"" + fillColor + "\" stroke=\"none\"");
		} else {
			doc.putPlaneString("<path d=\"M" + nf.format(x) + " " + nf.format(y) + " L" + nf.format(x+w) + " " + nf.format(y) + " " + nf.format(x+w) + " " + nf.format(y+h) + " " + nf.format(x) + " " + nf.format(y+h) + "\" fill-opacity=\"0.5\" fill=\"" + fillColor + "\" stroke=\"none\"");
		}
		
		if ( MaxZoom >0 ){
			doc.putPlaneString(" visibleMaxZoom=\"" + nf.format(MaxZoom) + "\"");
		}
		doc.putPlaneString("/>\n");
		
	}
	
	
	
	public static String htmlEscape(String text){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<text.length();i++){
			switch(text.charAt(i)){
			case '&' :
				sb.append("&amp;");
				break;
			case '<' :
				sb.append("&lt;");
				break;
			case '>' :
				sb.append("&gt;");
				break;
			case '"' :
				sb.append("&quot;");
				break;
			case '\'' :
				sb.append("&#39;");
				break;
			case ' ' :
				sb.append("&#32;"); // 2016.10.17 debug
				break;
			case '\\' :
//				sb.append("&yen;"); // 2014.10.14 debug
				sb.append("&#165;");
				break;
			default :
				sb.append(text.charAt(i));
				break;
			}
		}
		return sb.toString();
	}	
	
	
	public void set2DHashSM( HashMap<Long,SvgMap> hMap , int index1 , int index2 , SvgMap value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key ,  value  );
	}
	
	public SvgMap get2DHashSM( HashMap<Long,SvgMap> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		SvgMap ans = hMap.get(key);
		return ( ans );
	}
	
	public void set2DHashD( HashMap<Long,Double> hMap , int index1 , int index2 , double value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key , new Double( value ) );
	}
	
	public double get2DHashD( HashMap<Long,Double> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		Double ans = hMap.get(key);
		return ( ans.doubleValue() );
	}
	
	public void set2DHashI( HashMap<Long,Integer> hMap , int index1 , int index2 , int value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key , new Integer( value ) );
	}
	
	public int get2DHashI( HashMap<Long,Integer> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		int ans;
		if ( hMap.containsKey(key)){
			ans = (hMap.get(key)).intValue();
		} else {
			ans = 0;
		}
		return ( ans );
	}
	
	public void set2DHashB( HashSet<Long> hMap , int index1 , int index2 , boolean value , boolean defaultVal){
		// default�̒l�̓n�b�V�������Ȃ��I
		if ( defaultVal != value ){
		Long key = getHashKey( index1 , index2 );
			hMap.add(key);
		}
	}
	
	public boolean get2DHashB( HashSet<Long> hMap , int index1 , int index2 , boolean defaultVal){
		Long key = getHashKey( index1 , index2 );
		boolean ans;
		if ( hMap.contains(key) ){
			ans = !defaultVal;
		} else {
			ans = defaultVal;
		}
		return ( ans );
	}	
	
	public Long getHashKey( int index1 , int index2 ){
		return ( new Long((long)((long)index1 * (long)100000000 + (long)index2)));
	}
		
	public int[] getIndex( Long key ){
		int[] ans = new int[2];
		long kl = key.longValue( );
		ans[0] = (int)(kl / (long)100000000);
		ans[1] = (int)(kl % (long)100000000);
//		System.out.println(key+" , " + ans[0] + ":"+ans[1]);
		return ( ans );
	}
		
	
	public HashSet<Long> array2HashSetB( boolean[][] array2 , boolean defaultVal){
		// default�̒l�̓n�b�V�������Ȃ��I
		HashSet<Long> ans = new HashSet<Long>();
		for ( int i = 0 ; i < array2[0].length ; i++ ){
			for ( int j = 0 ; j < array2.length ; j++ ){
//				System.out.println( j+","+i+":"+array2[j][i]);
				if ( array2[j][i] != defaultVal ){
					Long key = new Long((long)((long)j * (long)100000000 + (long)i));
					ans.add( key );
				
				}
//				System.out.println( "key:"+ new Long(j * 100000000 + i) + " contains:" + ans.contains(  new Long(j * 100000000 + i) ) );
			}
		}
		return ( ans );
	}	
	
	public boolean[][] getThisTileExistenceArray(){
		boolean[][] ans = new boolean[wPart][hPart];
		for ( int i = 0 ; i < wPart ; i++ ){
			for ( int j = 0 ; j < hPart ; j++ ){
				ans[i][j] = get2DHashB( thisTileExistence , i , j , true);
//				System.out.print(ans[i][j]?"o":"x");
			}
//			System.out.println("");
		}
		return(ans);
	}
	
	public boolean[][] getThisTileHasElementsArray(){
		boolean[][] ans = new boolean[wPart][hPart];
		for ( int i = 0 ; i < wPart ; i++ ){
			for ( int j = 0 ; j < hPart ; j++ ){
				ans[i][j] = get2DHashB( thisTileHasElements , i , j , false);
			}
		}
		return(ans);
	}
	
	public HashSet<Long> getThisTileExistenceSet(){
		return( thisTileExistence );
	}
	
	public HashSet<Long> getThisTileHasElementsSet(){
		return( thisTileHasElements );
	}
	
	public boolean divCheck( HashMap<Long,Integer> divErr , int limit , int currentLevel ){
		// ���ׂẴ^�C���̕����G���[�A�������Alimit�𒴂�����A���[�g(false)���o��
		// currentLevel�ƁA�����G���[�A�����������ꍇ�́A�K�w�I�^�C����������x���������Ă��Ȃ���ԂȂ̂ŁAlimit����+5�S�苭���`�F�b�N����(2016/3/15)
		boolean ans = false;
		Iterator it = divErr.keySet().iterator();
//		System.out.print("divCheck:  level:" + currentLevel + " :: ");
		for ( Long key : divErr.keySet()){
			int ec = divErr.get(key).intValue();
//			System.out.print(ec + "," );
			if ( ec == currentLevel && ec < limit + 5 ){
				ans = true;
			} else if ( ec < limit ){
				ans = true;
//				break;
			}
		}
//		System.out.println("");
//		System.out.println("totalSize:" + divErr.size());
		return ( ans );
	}
	
	// SvgMap�̊e�탁�\�b�h(�`�施�ߑ��ɑΉ�)���C���X�^���X�����邽�߂̒��ۃN���X 2016.4.7- ���t�@�N�^�����O�̒��S
	// �}���`�X���b�h���̂��߂ɁA�`�施�ߗ���ꎞ�~�ς��A��C�ɃX���b�h�Q�Ɏ�n�����񏈗��������グ�邽�߁A���\�b�h�̃x�N�^����邱�Ƃ��ړI
	private abstract class svgMapCommands{
		long pointHintKey = -1; // point��HashKey
		
		void exec() throws Exception{
			SubTileInfo subTileInfo = new SubTileInfo( null );
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
//				SvgMap hsm = get2DHashSM( tiles, j, i);
				k = drawObjectToTile( i, j, k , subTileInfo );
			}
			outOfSize = subTileInfo.subOutOfSize;
			allTilesOOS = subTileInfo.subAllTilesOOS;
		}
		
		// �}���`�X���b�h�����p (subTileList�͂��ꂼ��̃X���b�h�p�ɏ��������ꂽ�^�C���̃��X�g)
		void exec(SubTileInfo subTileInfo) throws Exception{
//			System.out.println("exec:pointHintKey:"+pointHintKey);
			if ( pointHintKey < 0 ){
				for ( Long key : subTileInfo.subTileSet){
					if ( ! subTileInfo.subTileOverflowSet.contains(key) && !thisTileExistence.contains(key)){ // ���̕`��V���[�Y�ƈ�O�܂ł̂Ƃ���܂łŃI�[�o�[�t���[���ĂȂ����̂̂ݕ`��
						int[] tileIndex = getIndex(key);
						int i = tileIndex[1];
						int j = tileIndex[0];
						drawObjectToTile( i, j, 0 , subTileInfo );
					}
				}
			} else { // pointHintKey������point�W�I���g���ɑ΂��ă^�C���ʒu���ꔭ�Ō��߁A����������
				if ( subTileInfo.subTileSet.contains(pointHintKey) && (!subTileInfo.subTileOverflowSet.contains(pointHintKey)&& !thisTileExistence.contains(pointHintKey) )){
					
					int[] tileIndex = getIndex(pointHintKey);
					int i = tileIndex[1];
					int j = tileIndex[0];
					drawObjectToTile( i, j, -1 , subTileInfo );
				}
			}
		}
		abstract int drawObjectToTile( int i , int j , int k , SubTileInfo subTileInfo ) throws Exception;
	}
	
	// �`�施�ߗ�̒~�ςƖ{���̕`������{������R���g���[���̃t�����g�G���h
//	int bufferedDrawSize = 128; // �����Ŏw�肵�������o�b�t�@����ăX���b�h�Ɉ�C�ɑ��t�����
	int bufferedDrawSize = 16384;
	ArrayList<svgMapCommands> smcArray = new ArrayList<svgMapCommands>(); // �`�施�ߗ�~�ϗparray ����͏��bufferedDrawSize
	
	private void bufferedDraw( svgMapCommands smc , boolean forceOutput) throws Exception{ // ���b�p�ł�
		bufferedDrawMS( smc , forceOutput);
	}
	
	// �X���b�h�ɂ��Ȃ��Ŏ��s����^�C�v(�����H���p�Fobsolute)
	private void bufferedDrawSS( svgMapCommands smc , boolean forceOutput) throws Exception{
		if ( smc != null ){
			smcArray.add(smc); // �^�C���ɂ��X���b�h�ɂ����������ɁA�R�}���h���������߂邾�����s���Ă���B
		}
		if ( smcArray.size() == bufferedDrawSize || forceOutput ){
//			System.out.println("compute each tile and buffer clear");
			// �o�b�t�@�������ς��ɂȂ�������ۂ̏o�͏��������s
			// �{���̓X���b�h����
			for ( int i = 0 ; i < smcArray.size() ; i++ ){
				(smcArray.get(i)).exec(); // ����exec���^�C���������ԐړI�ɔ���������
			}
			smcArray.clear();
		}
	}
	
	// �}���`�X���b�h�Ŏ��s����^�C�v(���p)
	private void bufferedDrawMS( svgMapCommands smc , boolean forceOutput) throws Exception{
		if ( smc != null ){
			smcArray.add(smc); // �^�C���ɂ��X���b�h�ɂ����������ɁA�R�}���h���������߂邾�����s���Ă���B
		}
		if ( smcArray.size() == bufferedDrawSize || forceOutput ){
//			System.out.println("compute each tile and buffer clear");
			// �o�b�t�@�������ς��ɂȂ�������ۂ̏o�͏��������s
			
//			System.out.println("subTileSets.size(): "+subTileSets.size());
			
			List<Future<?>> futureList = new ArrayList<Future<?>>();
			
			for ( int j = 0 ; j < subTileSets.size() ; j++ ){
				SvgMapExporterRunnable smet;
				if ( svgMapExporterRunnables.size() > j ){ // �����ȃu�����ė��p���Ă���
//					System.out.println("�����ȃu�����ė��p");
					smet = svgMapExporterRunnables.get(j);
					smet.initRunnable( subTileSets.get(j) );
				} else {
//					System.out.println("�����ȃu���V�K");
					smet = new SvgMapExporterRunnable( subTileSets.get(j), j);
					svgMapExporterRunnables.add(smet);
				}
				
				
				Future<?> future = svgMapExecutorService.submit(smet);
				futureList.add(future);
				
			}
			
			// �����̂��߂̑ҋ@���[�v
			for (Future<?> future : futureList) {
				future.get();
			}
			
			
			allTilesOOS = true;
			for ( int j = 0 ; j < subTileSets.size() ; j++ ){
				SvgMapExporterRunnable smet = svgMapExporterRunnables.get(j);
				SubTileInfo si = smet.subTileInfo;
				if ( si.subAllTilesOOS == false ){ // ���������ꂽsubAllTilesOOS�Ɉ�ł�false�������allTilesOOS��false
					allTilesOOS = false;
				}
				if ( si.subOutOfSize == true ){ // ���������ꂽsubOutOfSize�Ɉ�ł�true�������outOfSize��true
					outOfSize = true;
				}
				// thisTileExistence�i���ۂ͂��̃��x���ŃI�[�o�[�t���[�����^�C���̑S���X�g�j�ւ̃R�s�[
				thisTileExistence.addAll(si.subTileOverflowSet);
				
//				System.out.println("tiles:"+si.subTileSet + "  overflow:"+si.subTileOverflowSet);
				
//				si.deleteMember();
				si = null;
				smet = null;
//				System.out.println( svgMapThreads.get(j).getState()+" :: " + svgMapThreads.get(j).isAlive());
				
			}
			
			
//			svgMapThreads.clear();
			smcArray.clear();
		}
	}
	
	
	//�X���b�h�����@�\�F�}���`�X���b�h�p��tileList(�O���[�o���ϐ�)������������subTileSets(�O���[�o���ϐ�)�����
	int maxThreads; // �ő�̃X���b�h���`�X���b�h�v�[���Ɠ����i�^�C���̐�������ɖ����Ȃ���Γ��R�^�C�����j
		
	ArrayList<HashSet<Long>> subTileSets; // �}���`�X���b�h�p�ɏ��������ꂽtileList��HashSet����������
		
	private void buildSubTileSets(){
//		System.out.println("CALLED buildSubTileSets   tileList.size():" + tileList.size() + "    maxThreads:" + maxThreads);
		
		subTileSets = new ArrayList<HashSet<Long>>();
		
		int j = 0;
		for ( int i = 0 ; i < tileList.size() ; i++ ){
//			Object tileIndex = tileList.get(i);
			int[] tileIndex = (int[])(tileList.get(i));
			if ( i < maxThreads ){
				
				HashSet<Long> subSet = new HashSet<Long>();
				set2DHashB( subSet, tileIndex[1], tileIndex[0],true,false);
//				set2DHashB( subSet, tileIndex[0], tileIndex[1],true,false);
				subTileSets.add(subSet);
				
			} else {
				
				HashSet<Long> subSet = subTileSets.get( i % maxThreads );
				set2DHashB( subSet, tileIndex[1], tileIndex[0],true,false);
//				set2DHashB( subSet, tileIndex[0], tileIndex[1],true,false);
				
			}
			
			
			
		}
	}
	
	private void aggregateTileList(){
//		System.out.println("thisTileExistence:"+thisTileExistence);
		// tileList��outofsize�̂��̂����������̂ɍ�蒼��
		// thisTileExistence(�I�[�o�[�t���[�����^�C��)�@�����������Ηǂ�
//		System.out.print("TileList:");
		for ( int i = tileList.size()-1 ; i >=0 ; i-- ){
			int[] tileIndex = (int[])(tileList.get(i));
//			System.out.print(tileIndex[1]+"_"+tileIndex[0]+",");
			Long key = getHashKey(tileIndex[1],tileIndex[0]);
//			Long key = getHashKey(tileIndex[0],tileIndex[1]);
			if ( thisTileExistence.contains(key) ){
				tileList.remove( i );
			}
		}
//		System.out.println("");
	}
	
	private class SubTileInfo{
//		ArrayList<Object> subTileList;
		HashSet<Long> subTileOverflowSet;
		boolean subOutOfSize;
		boolean subAllTilesOOS;
		
		HashSet<Long> subTileSet;
		
		
		SubTileInfo(  HashSet<Long> subTileSet ){
			this.subTileSet = subTileSet;
			subTileOverflowSet = new HashSet<Long>();
			subOutOfSize = false;
			subAllTilesOOS = false;
			if ( subTileSet.size() == 0 ){
				subAllTilesOOS = true;
//				System.out.println("new SubTileInfo subAllTilesOOS!!");
			}
		}
		
		void deleteMember(){
			subTileOverflowSet.clear();
			subTileSet.clear();
			subTileOverflowSet = null;
			subTileSet = null;
		}
		
	}
	
	//�^�C���o�̓X���b�h
	private class SvgMapExporterRunnable implements Runnable{
//		ArrayList<svgMapCommands> smcArray; // �R�}���h���X�g : ���̂Ƃ�������N���X�Ȃ̂ŎQ�Ƃł��܂���
//		ArrayList<Object> subTileList; //���ۂ̃^�C���o�͂ւ̎Q�Ɨp�@�^�C���ԍ����X�g : subTileInfo�ɓ���
//		HashMap<Long,SvgMap> tiles; // ���ۂɓ����Ă���^�C���i�X�̃X���b�h�ȊO�̂��̂������Ă��鋤�ʂ̂��́j���̂Ƃ�������N���X�Ȃ̂ŎQ�Ƃł��܂���
		int threadNumber;
		
		SubTileInfo subTileInfo;
		
//		HashSet<Long> subTileOverflowSet;
//		boolean subOutOfSize;
//		boolean subAllTilesOOS;
		
		SvgMapExporterRunnable(  HashSet<Long> subTileSet , int threadNumber ){ // for debug
//			this.smcArray = smcArray;
			this.threadNumber = threadNumber;
//			int size = tiles.size();
			this.subTileInfo = new SubTileInfo( subTileSet );
		}
		SvgMapExporterRunnable( HashSet<Long> subTileSet ){
			this.subTileInfo = new SubTileInfo( subTileSet );
			this.threadNumber = 0;
		}
		public void initRunnable( HashSet<Long> subTileSet ){
			this.subTileInfo = new SubTileInfo( subTileSet);
		}
		public void run(){
//			System.out.print("T"+ threadNumber);
			try{
				for ( int i = 0 ; i < smcArray.size() ; i++){ // �R�}���h�񂷃��[�v
					svgMapCommands command = smcArray.get(i);
					command.exec(subTileInfo); // ���̃X���b�h�p�̃^�C���ɑ΂��Ă����o�͎w�����o��
				}
				flush(); // �������菑���o��
			} catch ( Exception e ){
				System.out.println("exception: " + e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
		}
		private void flush() throws Exception {
			for ( Long key : subTileInfo.subTileSet ){
				
				int[] tileIndex = getIndex(key);
				SvgMap hsm = get2DHashSM( tiles, tileIndex[0], tileIndex[1]); // �t�����H 2017.4.18
				if ( !thisTileExistence.contains(key) ){ // ��O�܂ł̕`��o�b�`�ő��݂��Ă�����̂�flush
					hsm.flush();
					if( subTileInfo.subTileOverflowSet.contains(key) ){ // �������A���̕`��o�b�`�ŃI�[�o�[�t���[�����炱���ŃN���[�Y
						hsm.putFooter();
					}
				}
			}
		}
	}
	
}