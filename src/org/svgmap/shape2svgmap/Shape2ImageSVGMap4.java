package org.svgmap.shape2svgmap;

// Shape2ImageSVGMap4 (shp2Image�������)
//
// shapefile����r�b�g�C���[�W�^�C���n�}�Q�Ƃ���SVG�R���e�i�𐶐�����
// Programmed by Satoru Takagi (2012/04)
// Copyright 2012-2017 by Satoru Takagi @ KDDI All Rights Reserved
//
// 2012/04/26 1st Ver
// 2012/05/10 mesh2DB��p���āA���{���㕔�݂̂̃f�[�^�����Ŏ኱������
// 2012/05/2? ���x�����w�肵�ĕ�������I�v�V������ǉ�
// 2012/05/2? �\���̈���f�[�^�̈�ɑ΂���{���Ŏw��ł���I�v�V������ǉ�
// 2012/05/29 �K�w�R���e�i(2�i�̂�)�ɑΉ��A�r�b�g�C���[�W�̕t�ԕ��@�̃o�O�΍�(smsArray)
// 2012/05/31-���\���P�Ƀ`�������W�@�r�b�g�C���[�W��傫������Đ؂蕪������@�͂ǂ����H
// 2012/06/01 ��L�����������������ʁA�啝�Ȑ��\���P�ɐ����I -sumUp 16�@����256�{�H�̍�����
// 2012/06/04 ??
// 2013/03/27 POI�̃����_�����O�ɂ��Ή�
// 2013/07/18 �������P(����lvl1-15�Ƃ���fixarea�̂Ƃ�)
// 2014/02/20 �܂��u���̃��x����null�ɂȂ����^�C���́A���̉��̃��x���͍��Ȃ��v�̌�����
// 2014/02/27 shape2SvgMap�̏o�͌��ʂ���Abitimage���K�v�ȃ^�C�������𐶐�����B
// 2014/02/28 �R���e�i�̓񎟌��z���hashMap�ɂ��Č�����
// 2014/03/?? for next�Ɠ񎟌��z��̂قƂ�ǂ�hashMap�ɂ��ă������������P
// 2014/05/02 debug(line obj����ʏ�ŕ`�悳��Ȃ��قǍׂ�����shape2SvgMap�ŕK�v�Ƃ����^�C������������Ȃ�)
// 2014/05/13 POI�̐F��prop�l�ɉ����ĕω��\��(from Shape2SVGMap)
// 2016/02/04 ������^�̑����ɑ΂��ĐF�e�[�u�����A�w��ł���@�\ -colorkey (Shape2SVGMap�ƌ݊�)
// 2016/02/05 ���l�^�̑����ɑ΂��Ă��F�e�[�u�����w��ł���g�� (Shape2SVGMap�ƌ݊�)
// 2016/02/12 ���l�^�̑����ɑ΂���F�t���ŁAiHSV����I�ׂ� -numColor (Shape2SVGMap�ƌ݊�)
// 2016/04/19 �G���[��������������(lowresimage���K�v���Ȃ��P�[�X)
// 2016/08/02 shape2svgmap���l�ɊO���t�@�C������I�v�V�����ǂ߂�@�\�ǉ�
// 2017/02/20 Rev4: �}���`�X���b�h���J���J�n shapefile���璼�ڐ�������featuresource�ł́A���\�̌���͋͂��Ȋ����@��͂蕡���̃t�@�C�����I�[�v�����Ă��܂����Ǝ��̂���肾�Ǝv����B�i����ɐ[���������K�v�j�@����ɑ΂��āAhttp://docs.geotools.org/stable/userguide/tutorial/datastore/source.html�Ȃǂ��Q�l�Ɉ��shapefile featuresource���o�b�t�@�����O���R�s�[���ĕ����̃X���b�h�ɕ��z����featuresource���������Ă����p����悤�ɂ���Ɖ��P���邩���H
// 2017/04/04 CSV�ǂݍ��ݑΉ� geotools8(8.7)���K�v�����Aimageio-ext-*.jar���s��N�����̂ŊO���Ă���
// 2017/05/12 �悤�₭������ł̃t�B���^��shape2svgmap�ƌ݊�(�O����v)�ɂȂ����Ǝv��
// 2018/08/10 �����v���p�e�B���̃J������F�����J�����Ɏw�肷��ƕϊ��ł��Ȃ����������B���̍ۂ�shapefile�̕��������̖��̉����@�����������̂ł�������΍�A�����Ă��ꂪ�g�y����csv data store���l�C�e�B�u�����R�[�h��sjis���������ł͂Ȃ��܂Ƃ��Ȏ������ł���悤�ɉ��C�B���Ƃ̂Ƃ���shape2SvgMap�ł͕s����N���Ă��Ȃ��̂Ő̂̂܂܂ɂ��Ă������A�ǁX���̂܂Ƃ��Ȏ����ɉ��C����ׂ�
// 2018/08/31 strkey�̒�������(keyLength)�ɑ΂���ׂ��ȐF�������肪�Ԉ���Ă����̂��C��
// 2019.09.21 csv���͂ł̒P����lineString,polygon�f�[�^�Ή�

import java.awt.*;
import java.awt.image.*;

import java.util.*;


import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.text.NumberFormat ;
import java.awt.geom.*;

import java.nio.charset.*;

import org.geotools.data.shapefile.*;
import org.geotools.feature.*;
import org.geotools.data.simple.*;
import org.opengis.filter.*;
import org.geotools.factory.*;
import org.geotools.geometry.jts.*;
import org.geotools.map.*;
import org.geotools.renderer.lite.*;
import org.geotools.styling.*;
import org.opengis.feature.simple.*;
import org.opengis.referencing.crs.*;
import org.opengis.feature.type.*;

import com.vividsolutions.jts.geom.*;

// for CSV Reader/Exporter 2017.4.3
import org.svgmap.shape2svgmap.cds.CSVDataStore;
import org.svgmap.shape2svgmap.cds.CSVFeatureReader;
import org.svgmap.shape2svgmap.cds.CSVFeatureSource;
import org.geotools.data.store.ContentDataStore;

// use Executor 2017.5.18
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Shape2ImageSVGMap4 {
	
	boolean checkJp = false;
	boolean includeBaseMap = false;
	
	int colorResolution = 48;
	
	int maxThreads = 4;
	ExecutorService svgMapExecutorService; // for Executor multi thread 2017.4.19
	
	jpLandTester jpt;
	
	String colorKeys =""; // 2016.2.4
	int colorTable = SVGMapGetColorUtil.HSV; // 2016.2.12
	
	// step[deg] (Options) (Layer1Path #fillColor #strokeColor strokewidth) (Layer2Path #fillColor #strokeColor strokewidth)
	//
	// 
	
	// for CSV Support 2017.4.3
	boolean inputCsv = false;
	boolean gZipped = false; // 2017.5.15
	String csvSchemaPath="";
	String charset ="MS932"; // 2017.7.20 CSV�̂Ƃ���charset(shp�ɂ͖���)
	
	// for outOfRange Color Control
	int outOfRangeView = SVGMapGetColorUtil.MARK;
	
	//
	File complementalSourceSvg = null;
	
	public static void showHelp(){
		System.out.println( "Shape2ImageSVGMap: Generate bit image tiles type SVG Map data from (Shapefile|csv)");
		System.out.println( "Copyright 2012 - 2017 by Satoru Takagi @ KDDI All Rights Reserved");
		System.out.println( "" );
		System.out.println( "Shape2ImageSVGMap TileSize (-Options) (Path4Shape.(shp|csv) (#fillColor | attrNumb) #strokeColor strokeWidth symbolSize )* " );
		System.out.println( "" );
		System.out.println( "TileSize: ( level | tileSize\"deg\" | sigle | statLevl-endLevel | (vectorTiledContainer.svg) )" );
		System.out.println( " tileSize: (tileSize)\"deg\"" );
		System.out.println( " level:    (0....nn:see below)" );
		System.out.println( " single: output as single bitimagemap" );
		System.out.println( " statLevl-endLevel:   output multi-level map" );
		System.out.println( " (vectorTiledContainer.svg) : build complemental image svgmap from vectorTiledContainer.svg");
		System.out.println( "" );
		System.out.println( "lvl: tileWidth[deg]" );
		System.out.println( "  0: 360deg");
		System.out.println( "  1: 180deg");
		System.out.println( "  2:  90deg");
		System.out.println( "  3:  45deg");
		System.out.println( "  4:  22.5deg");
		System.out.println( "  5:  11.25deg");
		System.out.println( "  6:   5.625deg");
		System.out.println( "  7:   2.8125deg");
		System.out.println( "  8:   1.40625deg");
		System.out.println( "  9:   0.703125deg (1.40624-0.703125)");
		System.out.println( " 10:   0.3515625deg");
		System.out.println( " 11:   0.17578125deg");
		System.out.println( " 12:   0.087890625deg");
		System.out.println( " 13:   0.0439453125deg");
		System.out.println( " ...");
		System.out.println( " ...");
		System.out.println( " rootContainerSvgfileName : Build complemental multi-level tiles for density controlled vector tiles ");
		System.out.println( "" );
		System.out.println( "#fillColor   : fillColorString(in web format)." );
		System.out.println( "attrNumb     : Attribute number for determining fillColor. Calculation logic is the same as Shape2SVGMap's -color option. Options for outline are ignored. (no outline)" );
		System.out.println( "               If you set [numb]R[min],[max] then attr range is forced min .. max.");
		System.out.println( "#strokeColor : strokeColorString(in web format). You cannot set up an attribute number." );
		System.out.println( "strokewidith : width of stroke(in px)(int)." );
		System.out.println( "symbolSize   : size of symbol(in px)(int)." );
		System.out.println( " If \"dash\" is added (ex. 1dash) then stroke is dashed." );
		System.out.println( "" );
		System.out.println( "-Options:" );
		System.out.println( " -viewBuffer : set buffer area for viewBox (param: default:1 (no Buffer))" );
		System.out.println( " -sumUp      : set sumUpRendering (param: default:1 (no sumUp)) should be even number" );
		System.out.println( " -tileSize   : set tileSize(width=height)(param: default:256)" );
		System.out.println( " -antiAlias  : set anti alias rendering (no param)" );
//		System.out.println( " -fixArea    : fix GeoArea to start level (for shp2svgmap compati.)" );
		System.out.println( " -jpOnly     : Build Japan area only (no param)" );
		System.out.println( " -colorkey   : Specify the enumeration-type color table in the case of changing a color according to the string attribute in CSV." );
		System.out.println( "   param Example : value,value.... (Each colors is defined arbitrarily.)");
		System.out.println( "                 : value#color,value#color.... (Specify a color to each value.)");
		System.out.println( " -strcolor   : Set key length for changing a color according to the string attribute" );
		System.out.println( "  param  default : 2");
		System.out.println( " -numcolor   : The color table on a numerical value (param RED:Red gradation, HSV:(min:blue,nax:red), iHSV:(min:red,max:blue), QUOTA:randomly)");
		System.out.println( "                default:HSV");
		System.out.println( " -outOfRange : ��������𒴂����l�̏���");
		System.out.println( "                 �f�t�H���g:�O���[�ɐݒ�");
		System.out.println( "                 skip (=skipoutofrange)");
		System.out.println( "                 counterStop ��������l�ɒ���t��");
		System.out.println( " -optionFile : set -options using text file (param: optiontextfile.txt)");
		System.out.println( " -threads    : set maximum number of threads (default 4)");
		System.out.println( " -charset    : set charset for csv : Default:SJIS  (SJIS:UTF-8)");
		System.out.println( " -csvschema  : set schema file for csv");
		System.out.println( " Source CSV File rules:");
		System.out.println( "   Schema (name of each columns) of CSV should be set by the first row of source CSV or single line schema file using -csvschema option.");
		System.out.println( "   Properties other than coordinate values are basically treated as string.");
		System.out.println( "   ");
		System.out.println( "   Point feature:");
		System.out.println("      You should declare latitude and longitude column by schema name 'LAT' and 'LON' or 'LATITUDE' and 'LONGITUDE' (case insensitive).");
		System.out.println( "   LineString feature:");
		System.out.println("      Only one unbroken polyline is supported. A 'latitude:line' and 'longitude:line' pair must be at the last column of the schema. That is, other property must exist at the column before the coordinate value. And as actual data, arbitrary numbers of latitude and longitude pairs are listed from the last column.");
		System.out.println( "   Polygon feature:");
		System.out.println("      Only one polygon that is not split and is not even a donut is supported. The 'latitude:polygon' and 'longitude:polygon' pairs must be at the last column of the schema. Other than that it is the same as above.");
		System.out.println("    If you use -csvschema schema file must be single line and terminated (CR|LF).");
		
		
	}
	
	public static String[] getOptionFile( String[] mainArgs) {
		// -optionFile �I�v�V����������ꍇ�A�����D�悷��
		String optFilePath = null;
		int firstShpPos = -1;
		for ( int i = 0 ; i < mainArgs.length ; i++ ){
			if ( mainArgs[i].toLowerCase().equals("-optionfile")){
				optFilePath = mainArgs[i+1];
			}
			
			if ( mainArgs[i].toLowerCase().endsWith(".shp") || mainArgs[i].toLowerCase().endsWith(".gz") || mainArgs[i].toLowerCase().endsWith(".dbf")  ||  mainArgs[i].toLowerCase().endsWith(".shx") ||  mainArgs[i].toLowerCase().endsWith(".csv")  ) {
				firstShpPos = i;
				break;
			}
		}
		
		if ( firstShpPos < 0 ){
			System.out.println("Error: No shapefile or csv file assigned.");
			return ( mainArgs );
		}
		
		if ( optFilePath != null ){
			String[] ans=null;
			try {
				optionsReader or = new optionsReader( new File(optFilePath) );
				String[] opts = or.getOptions();
				
				ArrayList<String> argList = new ArrayList<String>();
				
				argList.add(mainArgs[0]); // TileSize
				
				for ( int i = 0 ; i < opts.length ; i++ ){ // Options by File
					argList.add(opts[i]);
				}
				
				for ( int i = firstShpPos ; i < mainArgs.length ; i++ ){ // each shapefiles and styles
					argList.add(mainArgs[i]);
				}
				
				ans = (String[])(argList.toArray(new String[0]));
				
			} catch ( Exception e ){
				System.out.println("Error: Invalid -optionFile option");
				System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
				System.exit(0);
			}
			return ( ans );
		} else {
			return ( mainArgs );
		}
		
	}
	
	public static void main(String args[]) throws Exception {
		
		args = getOptionFile( args );
		
		Shape2ImageSVGMap4 s2i = new Shape2ImageSVGMap4();
		int startLvl = 0;
		int endLvl = 0;
		
		boolean complementalSvg = false;
		SvgMapContainerReader sr = null;
		
//		boolean fixArea = true; // ������f�t�H���g�Ƃ���I(1)�̌������̂��� 2014.2.20
		
		if ( args.length<1){
			showHelp();
			System.exit(0);
		}
		
		try{
			if ( args[0].indexOf(".svg") > 0 ){
				complementalSvg = true;
				System.out.println("Use Complemental Vector Svg Container:" + args[0]);
				sr = new SvgMapContainerReader( args[0] );
				s2i.complementalSourceSvg = new File(args[0]);
				
			} else if ( args[0].indexOf("d")>0){
				s2i.partDeg = Double.parseDouble(args[0].substring(0,args[0].indexOf("d")));
			} else if ( args[0].indexOf("single")==0){
				s2i.singleImage = true;
			} else if ( args[0].indexOf("-") > 0 ){
				int ix = args[0].indexOf("-");
				startLvl = Integer.parseInt(args[0].substring(0,ix));
				endLvl = Integer.parseInt(args[0].substring(ix+1));
				if ( startLvl > endLvl ){
					int lt = startLvl;
					startLvl = endLvl;
					endLvl = lt;
				}
				System.out.println("level:" + startLvl + " to " + endLvl );
			} else {
				s2i.lvl = Integer.parseInt(args[0]);
			}
		} catch ( Exception  e ){
			System.out.println("\n\nError: Invalid TileSize parameter : " + args[0]);
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			e.printStackTrace();
			System.exit(0);
		}
		
		int i = 1;
		
		try{
			while( args[i].indexOf("-")==0){
				if ( args[i].toLowerCase().indexOf("-viewbuffer")>=0){
					++i;
					s2i.viewBuffer = Double.parseDouble(args[i]);
					System.out.println("viewBuffer:" + s2i.viewBuffer);
				} else if ( args[i].toLowerCase().indexOf("-sumup")>=0){
					++i;
					int su = Integer.parseInt(args[i]);
					if ( su <= 1){
						su = 1;
					} else if( su % 2 == 1 ){ // sumUp�͋����݂̂�����(�������P�ȊO)
						su = 2 * (int)(1 + su / 2);
					}
					s2i.sumUp = su;
					
					System.out.println("sumUp:" + s2i.sumUp);
				} else if ( args[i].toLowerCase().indexOf("-rebuildcontaineronly")>=0){
					s2i.rebuildContainerOnly = true;
					System.out.println("rebuildContainerOnly");
				} else if ( args[i].toLowerCase().indexOf("-includebasemap")>=0){
					s2i.includeBaseMap = true;
					System.out.println("includeBaseMap");
				} else if ( args[i].toLowerCase().indexOf("-antialias")>=0){
					s2i.antiAlias = true;
					System.out.println("includeBaseMap");
				} else if ( args[i].toLowerCase().indexOf("-tilesize")>=0){
					++i;
					s2i.imageWidth = Integer.parseInt(args[i]);
					s2i.imageHeight = s2i.imageWidth;
	//			} else if ( args[i].toLowerCase().indexOf("-fixarea")>=0){
	//				fixArea = true;
				} else if ( args[i].toLowerCase().indexOf("-jponly")>=0){
					s2i.checkJp = true;
				} else if ( args[i].toLowerCase().indexOf("-colorkey")>=0){
					++i;
					s2i.colorKeys = args[i];
				} else if ( args[i].toLowerCase().indexOf("-strcolor")>=0){
					++i;
					s2i.keyLength = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().indexOf("-numcolor")>=0){
					++i;
					if (args[i].toLowerCase().equals("hsv")){
						s2i.colorTable = SVGMapGetColorUtil.HSV;
					} else if (args[i].toLowerCase().equals("ihsv")){
						s2i.colorTable = SVGMapGetColorUtil.iHSV;
					} else if (args[i].toLowerCase().equals("red")){
						s2i.colorTable = SVGMapGetColorUtil.RED;
					} else if (args[i].toLowerCase().equals("quota")){
						s2i.colorTable = SVGMapGetColorUtil.QUOTA;
					}
				} else if ( args[i].toLowerCase().indexOf("-outofrange")>=0){
					++i;
					if (args[i].toLowerCase().equals("skip")){
						s2i.outOfRangeView = SVGMapGetColorUtil.SKIP;
					} else if (args[i].toLowerCase().equals("counterstop")){
						s2i.outOfRangeView = SVGMapGetColorUtil.COUNTER_STOP;
					}
					
				} else if ( args[i].toLowerCase().indexOf("-threads")>=0){
					++i;
					if ( Integer.parseInt(args[i]) > 0 ){
						s2i.maxThreads = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().indexOf("-csvschema")>=0){
					++i;
					s2i.csvSchemaPath = args[i];
					System.out.println("Schema Path for CSV file: " + s2i.csvSchemaPath);
				} else if ( args[i].toLowerCase().indexOf("-charset")>=0){
					++i;
					s2i.charset = args[i];
				} else {
					System.out.println("\n\nError :  There is no option named " + args[i]);
					System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
					System.exit(0);
				}
				++i;
			}
		} catch ( Exception e ){
			System.out.println("\n\nError: Invalid Option : " + args[i]);
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			e.printStackTrace();
			System.exit(0);
		}
		
		int layerCount=0;
		int i2 = i;
		while(i2 < args.length){
			i2+=5;
			++ layerCount;
		}
		
		s2i.setLayerCounts(layerCount);
		
		if ( ( args.length - i ) % 5 != 0 ){
			System.out.println("\n\nErrpr: Invalid layer parameters. For each layer you have to set five parameters as below.");
			System.out.println(" Path4Shape.(shp|csv) (#fillColor | attrNumb) #strokeColor strokeWidth symbolSize");
			System.out.println("");
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			System.exit(0);
		}
		
		while(i < args.length ){
//			try{
				int markerSize = Integer.parseInt(args[i+4]);
				//            sourve  , fillClr   , strokeClr , strokeWid , symbolSize
				s2i.loadLayer(args[i] , args[i+1] , args[i+2] , args[i+3] , args[i+4]);
				i += 5;
//			} catch (Exception e){
//				s2i.loadLayer(args[i] , args[i+1] , args[i+2] , args[i+3] , "0");
//				i += 4;
//			}
			++ layerCount;
		}
		
		s2i.buildSvgMapExecutorService();
		
		if ( complementalSvg ){ // shp2svgmap??�ō쐬�����x�N�^SVGMap��⊮���鏬�k�ڕ\���p��imagesvgmap����郂�[�h
			s2i.customTile = true;
			int[] bp = sr.getBasePart();
			Rectangle2D.Double gb = sr.getGeoBounds();
			HashSet<Long>[] rt = sr.getRequiredTiles();
			double[] tz = sr.getTileZoom();
			s2i.customArea = sr.getGeoBounds();
			s2i.buildComplementalMap( rt , tz , bp );
		} else if ( s2i.lvl == -1 &&  startLvl != endLvl ){ // �X�^�[�g����G���h�̃^�C�����x�����w�肵���K�w�I�^�C�����O
			s2i.buildHierarchicalMap( startLvl , endLvl );
		} else if ( s2i.singleImage != true && s2i.lvl == -1 ){ // ����͖���������G���[�A�E�g�̂�����
			System.out.println("You should set tile level");
		} else { // �����Ȃ���΁A�ꖇ���m�̃f�[�^����邩�A�w�肵�����x�������̃^�C�����O���ꂽsvgimagemap�����
			s2i.buildImage();
		}
		
		s2i.shutdownSvgMapExecuterService();
	}
	
	void buildSvgMapExecutorService(){
		svgMapExecutorService = Executors.newFixedThreadPool(maxThreads);
	}
	
	void shutdownSvgMapExecuterService(){
		svgMapExecutorService.shutdown();
	}
	
	SvgMap getHierarchicalContainer() throws Exception{
		String containerFileName = getOutDir(true, 0, 0 )+"rootImgMap.svg";
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(6);
		
		System.out.println("Build HierarchicalMap Container:" + containerFileName );
		SvgMap smcr = new SvgMap ( containerFileName , nFmt);
		
		ReferencedEnvelope bounds = sfcArray[0].getBounds();
		double lngMin  = bounds.getMinX();
		double latMin  = bounds.getMinY(); 
		double lngSpan = bounds.getWidth();
		double latSpan = bounds.getHeight();
		smcr.putHeader( (lngMin + 0.5 * lngSpan * ( 1 - viewBuffer )) * tcmul , - (latMin + 0.5 * latSpan * ( 1 + viewBuffer) ) * tcmul  , viewBuffer * lngSpan * tcmul , viewBuffer * latSpan * tcmul );
		smcr.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		return ( smcr );
	}
	
	double getZoomTh( double tileDeg){
		return ( 100.0 * (double)imageWidth / ( tileDeg * tcmul ));
	}
	
	String getsubClink( String fpath ){
		String ans = fpath.substring( 1 + fpath.lastIndexOf(File.separator, fpath.lastIndexOf(File.separator) - 1));
		ans = ans.replace(File.separator,"/");
		return( ans );
	}
	
	void buildComplementalMap( HashSet<Long>[] rt , double[] tz , int[] bp ) throws Exception{
		SvgMap smcr = getHierarchicalContainer();
		
		for ( int lv = 0 ; lv < rt.length ; lv ++ ){
			reqTile = rt[lv];
			customPart = bp;
			System.out.println("\n=====================");
			String spath = buildImage();
			System.out.println("\nConvert partDeg:"+ partDeg);
//			double zth = getZoomTh(getPartDeg(lvl));
			double zth = getZoomTh(partDeg);
			if ( tz != null && tz[lv] >0 ){
				zth = tz[lv];
			} else {
				System.out.println("WARN: There are no zoomRange in vector svgMap container....");
			}
			double minz = zth / 2.0;
			double maxz = zth;
			if ( lv == 0 ){
				minz = -1;
			} else if( lv == rt.length-1 ){
//				maxz = -1;
			}
			smcr.putImage(new Coordinate( -30000 , -30000 ) , 60000 , 60000 , getsubClink(spath) ,minz , maxz );
			bp[0] = bp[0] * 2;
			bp[1] = bp[1] * 2;
			usePrevBounds = true;
		}
		smcr.putFooter();
	}
	
	void buildHierarchicalMap( int startLvl , int endLvl ) throws Exception{
		
		SvgMap smcr = getHierarchicalContainer();
		
		for ( int lv = startLvl ; lv <= endLvl ; lv++ ){
			lvl = lv;
			partDeg = -1;
			System.out.println("\n=====================");
			System.out.println("Convert level:" + lvl + " partDeg:"+ getPartDeg(lvl));
			double zth = getZoomTh(getPartDeg(lvl));
			
	//				if ( fixArea == true && lv != startLvl ){}
			if ( lv != startLvl ){
				usePrevBounds = true; // �n�}�̐����̈����ԏ�̃��x���̂��̂ŌŒ肷��(���̃��x���ɂȂ�΁A�����ƃ^�C�g�ȃ{�b�N�X�ɂȂ�̂����Ashp2svgmap������ړI�ɁA�����}�����邽��)
			} else {
				usePrevBounds = false;
			}
			
			String spath = buildImage();
			double minz = zth / 2.0;
			double maxz = zth;
			if ( lv == startLvl ){
				minz = -1;
			} else if( lv == endLvl ){
				maxz = -1;
			}
			smcr.putImage(new Coordinate( -30000 , -30000 ) , 60000 , 60000 , getsubClink(spath) , minz , maxz );
		}
		smcr.putFooter();
	}
	
	
	SimpleFeatureCollection[] sfcArray;
	File[] inputFileArray;
	
	Style[] styleArray;
	double partDeg = -1;
	int lvl = -1;
	
	void setLayerCounts( int layers ){
		System.out.println("Layers count:"+layers);
		sfcArray = new SimpleFeatureCollection[layers];
		inputFileArray = new File[layers];
		styleArray = new Style[layers];
	}
	
	int layerCount = 0;
	public void loadLayer( String filePath , String fillColorS , String strokeColorS , String strokeWidthS , String markerSizeS ) throws Exception {
		
		System.out.println("call loadLayer::::::: fillColor:"+fillColorS+"  strokeColorS:"+strokeColorS+"  strokeWidthS:"+strokeWidthS+"  markerSizeS:"+markerSizeS);
		
		int attrColorNumber = -1;
		double fMin = -9e99;
		double fMax = 9e99;
		
		Color fillColor = null;
		if ( fillColorS.indexOf("#") == 0 ){
			fillColor = getColor(fillColorS);
		} else if (fillColorS.indexOf("R") < 0 ){
			attrColorNumber = Integer.parseInt(fillColorS);
		} else { // num range set
			attrColorNumber = Integer.parseInt(fillColorS.substring(0,fillColorS.indexOf("R")));
			fMin = Double.parseDouble(fillColorS.substring(fillColorS.indexOf("R")+1,fillColorS.indexOf(",")));
			fMax = Double.parseDouble(fillColorS.substring(fillColorS.indexOf(",")+1));
			if ( fMin > fMax ){
				double ft = fMin;
				fMin = fMax;
				fMax = ft;
			}
			System.out.println("Force numerical range : "+ fMin + " ... " + fMax );
		}
		Color strokeColor = getColor(strokeColorS);
		boolean dash = false;
		
		int strokeWidth = 0;
		if ( strokeWidthS.indexOf("none") != 0 ){
			if ( strokeWidthS.indexOf("dash") == -1 ){
				strokeWidth = Integer.parseInt(strokeWidthS);
			} else {
				strokeWidthS = strokeWidthS.replace("dash","");
				strokeWidth = Integer.parseInt(strokeWidthS);
				dash = true;
			}
			if (strokeWidth < 0){
				strokeWidth = 0;
			}
		}
		
		int markerSize = Integer.parseInt( markerSizeS );
		
		
	  	File file = new File(filePath);
		
		if ( filePath.endsWith(".csv")){
			inputCsv = true;
			System.out.println("CSV input");
		} else if ( filePath.endsWith(".gz")){
			inputCsv = true;
			gZipped = true;
			System.out.println("gz_CSV input");
		} else {
			System.out.println("Shapefile input");
		}
		
//		ContentDataStore readStore = null;
		SimpleFeatureSource fs = null;
		CSVDataStore cds =null;
		ShapefileDataStore sds = null;
		if ( inputCsv ){
			// CSV�t�@�C����ǂݍ��� 
			cds =null;
			if ( csvSchemaPath =="" ){
				cds = new CSVDataStore( new File(filePath), gZipped, charset );
			} else {
				cds = new CSVDataStore( new File(filePath), new File(csvSchemaPath), gZipped, charset );
			}
			cds.sjisInternalCharset = false; // 2018.8.10 shapefile�ł������R�[�h�𐳂������ʂ��ēǂ߂邱�Ƃ����������̂ŁE�E�E
			fs = cds.getFeatureSource(cds.getNames().get(0));
		} else {
			URI uri = file.toURI();
			URL url = uri.toURL();
		  	System.out.println("URL:"+url);
//		    sds = new ShapefileDataStore(url); 
		    sds = new ShapefileDataStore(url, true , Charset.forName("Windows-31j")); // 2018.8.10 debug   shapefile�ł�charset���w�肷��Ε����������Ȃ����Ƃ������@��������Ȃ��ƁAFilterFactory��property����������������Ȃ����߁E�E
		    fs = sds.getFeatureSource();
		}
		
	    SimpleFeatureCollection sfc = fs.getFeatures();
	  	
	  	System.out.println("FeatureCol. Bounds:" + sfc.getBounds() + " Size:" + sfc.size());
		
		
//	  	System.out.println("FeatureCol. Bounds:" + sfc.getBounds() );
		sfcArray[layerCount] = sfc;
		inputFileArray[layerCount] = file;
		
		Style style;
		if ( attrColorNumber < 0 ){
			style = getFeatureStyle( fillColor , strokeColor , strokeWidth , dash , markerSize , fs.getSchema());
		} else {
//			style = getFeatureStyle2( 2 , false , 3 , fs );
			style = getFeatureStyle2( attrColorNumber , false , markerSize , fs , fMin , fMax );
		}
		styleArray[layerCount] = style;
		
		++layerCount;
	}
	
	public Color getColor(String webColor){
		Color ans = null;
		if ( webColor.indexOf("#") == 0 ){
			int colorNum = Integer.parseInt( webColor.substring(1), 16);
			ans = new Color(colorNum);
		}
		return ( ans );
	}
	
	String getOutDir(boolean singleImage, int lvl, double partDeg ){
		String outDir ="";
		String outName = "";
		if ( complementalSourceSvg != null ){
			outDir = complementalSourceSvg.getParent()+ File.separator;
			outName = complementalSourceSvg.getName().substring(0,complementalSourceSvg.getName().lastIndexOf("."));
		} else if ( inputFileArray[0].getParent() != null ){
			outDir = inputFileArray[0].getParent()+ File.separator;
			outName = inputFileArray[0].getName().substring(0,inputFileArray[0].getName().lastIndexOf("."));
		}
		
		
		
		if ( !singleImage ){
			if ( partDeg >0){
				lvl = getLvl(partDeg);
			} else {
				partDeg = getPartDeg(lvl);
			}
			
			System.out.println("tileLevel:" + lvl + "  tileWidth:" + partDeg );
			
			outDir += outName + File.separator + "lvl" + lvl + File.separator;
		} else {
			outDir += outName + File.separator ;
		}
		
		
		File outDirF = new File (outDir);
		if ( !outDirF.exists() ){
			outDirF.mkdirs();
		}
		
		System.out.println("outDir:" +outDir );
		return ( outDir );
	}
	
	boolean rebuildContainerOnly = false; // �K�w�R���e�i�ɍč쐬�̂��߁A�r�b�g�C���[�W�͍��Ȃ�(������x�����g��Ȃ��Ǝv���}�X)
	int containerCounts = 16; // �K�w�R���e�i����邩�ǂ�����臒l(���̒l�̂Q��̃^�C���������~�b�g�ɂ���)
	boolean singleImage = false;
	
	boolean customTile = false;
	Rectangle2D.Double customArea;
	int[] customPart;
	
	String containerName = "container.svg";
	double viewBuffer = 1.0;
	
	boolean antiAlias = false;
	
	int sumUp = 1; // �^�C���ꊇ�`�搔�i�^�C�����͂��̒l�̓��j
	
	double tcmul = 100.0; // Geo->SVG�ϊ��W��(matrix��a,-d)(�S�^�C�����ʁE�E)
	
	
	// �����ɕK�v�ȂW�̃p�����[�^
	int imageWidth = 256; //�^�C���̃T�C�Y
	int imageHeight = 256;
	double geoXstart, geoYstart;
	double geoXstep, geoYstep;
	int GridXsize, GridYsize;
	
	double prevGeoXstart, prevGeoYstart, prevGeoXend, prevGeoYend; // shape2svgmap18�Ƃ̌݊����Ƃ邽��
	HashSet<Long> prevTileExistence;
	HashSet<Long> thisTileExistence;
	
	// �������ׂ��^�C�����X�g�̃C���f�b�N�X���O������^����
	HashSet<Long> reqTile = null;
	
	int keyLength = 2; // added 2016.4.1 for -strcolor
	
	boolean usePrevBounds = false;
	
	// ���O�Ɍ��߂Ȃ���΂Ȃ�Ȃ��W�p�����[�^��ݒ肷��A�R��ނ̕��@
	// int imageWidth, imageHeight;
	// double geoXstep, geoYstep;
	// double geoXstart, geoYstart;
	// int GridXsize, GridYsize;
	void setSingleBuildConditions( Rectangle2D.Double bounds ){
		imageWidth = imageWidth;
		imageHeight = (int)(imageWidth * bounds.height / bounds.width);
		geoXstep = bounds.width;
		geoYstep = bounds.height;
		geoXstart = bounds.x;
		geoYstart = bounds.y;
		GridXsize = 1;
		GridYsize = 1;
	}
	
	void setGlobalTileBuildConditions( Rectangle2D.Double bounds , int level ){
		imageWidth = imageWidth;
		imageHeight = imageHeight;
		double partDeg = getPartDeg(level);
		geoXstep = partDeg;
		geoYstep = partDeg;
		geoXstart = (int)((bounds.x + 180.0) / geoXstep ) * geoXstep - 180.0;
		geoYstart = (int)((bounds.y + 180.0) / geoYstep ) * geoYstep - 180.0;
		GridXsize = (int)Math.ceil( ((bounds.x + bounds.width)-geoXstart)/geoXstep);
		GridYsize = (int)Math.ceil( ((bounds.y + bounds.height)-geoYstart)/geoYstep);
	}
	
	void setCustomTileBuildConditions( Rectangle2D.Double bounds , int xPart , int yPart ){
		imageWidth = imageWidth;
		imageHeight = (int)(imageWidth * (bounds.height / yPart) / (bounds.width / xPart));
		geoXstep = bounds.width / xPart;
		geoYstep = bounds.height / yPart;
		geoXstart =  bounds.x;
		geoYstart = bounds.y;
		GridXsize = xPart;
		GridYsize = yPart;
	}
	
	public Long getReqTileKey( int tx , int ty ){
		// ���� y���Ђ�����Ԃ��Ă��܂���I
		return (new Long((long)((long)tx * (long)100000000 + (long)(GridYsize-ty-1))));
	}
	
	public String buildImage() throws Exception {
		
		
		// mapContext���쐬
		CoordinateReferenceSystem crs = 
		sfcArray[0].getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
		MapLayer layers[] = {};
		DefaultMapContext map = new DefaultMapContext(layers, crs);
		
		for ( int i = 0 ; i < layerCount ; i++ ){
			System.out.println("Add Layer:" + i );
			// ���C����ǉ�����(���ꂪmapContext�Ƃ������̂ɁA���ۂ�shape�f�[�^������t���Ă���)
			map.addLayer(new FeatureLayer(sfcArray[i], styleArray[i]));
		}
		
		// �����_������������@shapefile���琶�������n�����X�g���[������͂��Ă���ɂ���ĕ`�悷�郌���_���Ƃ����Ӗ����Ǝv����
		/**
		StreamingRenderer renderer = new StreamingRenderer();
		if ( antiAlias ){
			// �A���`�G���A�X�֌W�̃q���g��t����
			RenderingHints rh=new RenderingHints(
				RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
			rh.add(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
			rh.add(new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			renderer.setJava2DHints(rh);		
		}
		renderer.setContext(map); // �����mapContext�����shapefile�Q���X�g���[�~���O�����_���Ɋ�����Ă���
		**/
		
		ReferencedEnvelope bounds = map.getMaxBounds();
//		System.out.println("bounds:"+bounds+"  : "+bounds.getMinX()+","+bounds.getMinY()+","+bounds.getMaxX()+","+bounds.getMaxY());
		if ( bounds.getMinX()==0 && bounds.getMaxX()==-1 ){
			bounds = sfcArray[0].getBounds();
			System.out.println("Hmmm can't get bounds.. fixIt by layer0 : " + bounds);
		}
		
		
		// ���O�Ɍ��߂Ȃ���΂Ȃ�Ȃ��W�p�����[�^��ݒ肷��
		// int imageWidth, imageHeight;
		// double geoXstep, geoYstep;
		// double geoXstart, geoYstart;
		// int GridXsize, GridYsize;
		if ( singleImage ){
			setSingleBuildConditions( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight() ) );
		} else if ( customTile ){
			setCustomTileBuildConditions( customArea , customPart[0] , customPart[1] );
			partDeg = geoXstep; // �Ƃ肠�����e�X�g�p�E�E�E
		} else {
			if ( lvl == -1 && partDeg > 0 ){
				lvl = getLvl(partDeg);
			}
			setGlobalTileBuildConditions( new Rectangle2D.Double( bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight() ) , lvl );
		}
		
//		System.out.println("imageWidth:" + imageWidth + " imageHeight:" + imageHeight + " geoXstep:" + geoXstep + " gepYstep:" + geoYstep + " geoXstart:" + geoXstart + " geoYstart:" + geoYstart + " GridXsize:" + GridXsize + " GridYsize:" + GridYsize);
		
		prevGeoXstart = geoXstart;
		prevGeoYstart = geoYstart;
		prevGeoXend = geoXstart + GridXsize * geoXstep;
		prevGeoYend = geoYstart + GridYsize * geoYstep;
		
		if ( rebuildContainerOnly ){
			sumUp = 1; // �R���e�i�����݂̂̏ꍇ�͈ꊇ�����͗}��
		}
		
		
//		System.out.println("Rect:"+imageWidth * sumUp + "," + imageHeight * sumUp);
		Rectangle rect = new Rectangle(0, 0, imageWidth * sumUp , imageHeight * sumUp );
		
		if ( usePrevBounds ){
			geoXstart = prevGeoXstart;
			geoYstart = prevGeoYstart;
			GridXsize = (int)Math.round( ( prevGeoXend - prevGeoXstart ) / geoXstep);
			GridYsize = (int)Math.round( ( prevGeoYend - prevGeoYstart ) / geoYstep);
		}
		
		int totalGrid = GridXsize * GridYsize;
		
		String outDir = getOutDir( singleImage, lvl, partDeg ); // �o�͐�𐶐����Ă��邾���łȂ��ApartDeg�Ƃ��̐S�ȌW��������Ă��邼�E�E�E�E(����𐥐���)
		
		if ( checkJp ){
			// ���{�̈�`�F�b�J�[��ǂݍ���
			jpt = new jpLandTester();
		}
		
//		double geoXstep=0;
//		double geoYstep=0;
		
//		System.out.println("image Rect:" + rect + "  \ndata Bounds: minX:" + bounds.getMinX() + " minY:" + bounds.getMinY() + " width:" + bounds.getWidth() + " height:" + bounds.getHeight() + " \nraw:" + bounds );
		
		
		double lngMin  = bounds.getMinX();
		double latMin  = bounds.getMinY(); 
		double lngSpan = bounds.getWidth();
		double latSpan = bounds.getHeight();
		
		String containerFileName = outDir + containerName;
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(6);
		SvgMap sm = new SvgMap ( containerFileName , nFmt);
		sm.putHeader( (lngMin + 0.5 * lngSpan * ( 1 - viewBuffer )) * tcmul , - (latMin + 0.5 * latSpan * ( 1 + viewBuffer) ) * tcmul  , viewBuffer * lngSpan * tcmul , viewBuffer * latSpan * tcmul );
		sm.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		
		if ( includeBaseMap ){
			sm.putImage(
				new Coordinate( -30000 , -30000 ) ,
				60000 , 60000 ,
				"http://svg2.mbsrv.net/extmaps/Bing/ContainerBing.svg"
			);
		}
		sm.setGroup();
		
		
		int counter = 0;
//		int GridXsize, GridYsize;
		
		
		HashMap<Long, SvgMap> smsMap = null;
		boolean hasSmsMap = false;
		
		//containerCounts^2�istatic�j��葍�O���b�h���������ꍇ�́A�Q�K�w�R���e�i�Ƃ���
		if ( totalGrid > containerCounts * containerCounts ){
			smsMap   = buildSmsMap( GridXsize , GridYsize );
			hasSmsMap = true;
		}
		
		int smX , smY;
		
		double geoX , geoY;
		
//		System.out.println("GridXsize,sumUp,ceil" + GridXsize + "," + sumUp + "," + (int)Math.ceil( (double)GridXsize / sumUp ) );
		
		
		// add 2014.2.20 for speedup (1)
		thisTileExistence = new HashSet<Long>();
		if ( prevTileExistence == null ){ // �ŏ��̃��x����prevTileExistence��S���Z�b�g����
			prevTileExistence = new HashSet<Long>();
			for ( int i = 0 ; i < (int)Math.ceil( (double)GridXsize / sumUp ) ; i++ ){
				for ( int j = 0 ; j < (int)Math.ceil( (double)GridYsize / sumUp ) ; j++ ){
					prevTileExistence.add(getHashKey( i , j ));
				}
			}
		}
		
		String prg="";
		
		System.out.println("tileSize x:" + GridXsize + " y:" + GridYsize + "  sumUpTiles x:" +(int)Math.ceil((double)GridXsize/(double)sumUp) + " y:" +  (int)Math.ceil((double)GridYsize/(double)sumUp) + " total:" + ((int)Math.ceil((double)GridXsize/(double)sumUp)*(int)Math.ceil((double)GridYsize/(double)sumUp)) + "  effectiveSumUpTiles:"+ prevTileExistence.size());
		
		Iterator pti = prevTileExistence.iterator(); // prevTileExistence��p�����n�b�V�����[�v�ɑ�� 2014/2/27
		
		int skipCounter = 0;
		
		ArrayList<int[]> tilePosList = new ArrayList<int[]>();
//		ArrayList<TileRenderRunnable> tileRenderRunnables= new ArrayList<TileRenderRunnable>();
		
		while( pti.hasNext() ){
			int[] existPrevSumUpTile =getIndex((Long)pti.next());
			int tx = existPrevSumUpTile[0];
			int ty = existPrevSumUpTile[1];
//		// ����2�dfornext���[�v��HashSet(prevTileExistence)���[�v�ɑウ��ׂ�(ToDo 2014/2/24)�@�ˏ�����
			geoX = tx * geoXstep * sumUp + geoXstart;
			geoY = ty * geoYstep * sumUp + geoYstart;
			
			if ( reqTile != null ){
				// reqTile(svg�R���e�i����^����ꂽ�������ׂ��^�C��)������ꍇ�A����Ƃƍ��v�����u���b�N�̂ݏ���
				boolean containsReqTile = false;
				cnrt: for ( int sx = 0 ; sx < sumUp ; sx++ ){
					for ( int sy = 0 ; sy < sumUp ; sy++ ){
						if ( reqTile.contains(getReqTileKey( tx * sumUp + sx, ty * sumUp + sy ) ) ){
							containsReqTile = true;
							break cnrt;
						}
					}
				}
				if ( ! containsReqTile ){
					if ( skipCounter <= 10 ){
						System.out.print("s"); // skip image building
					} else if ( skipCounter <= 100000 &&  skipCounter % 1000 == 0 ){
						System.out.print("S");
					} else if ( skipCounter <= 10000000 &&  skipCounter % 100000 == 0 ){
						System.out.print("S5");
					}
					++skipCounter;
					continue;
				} else {
					skipCounter = 0;
				}
			}
			
//				System.out.println("tx,ty:"+tx+","+ty);
			
			counter += sumUp * sumUp;
			if ( false && (geoX * geoY + geoY) % 5 == 0){ // ��߂܂����E�E�E
				
				String del="";
				for ( int pct = 0 ; pct < prg.length() ; pct++ ){
					del += "\b";
				}
				System.out.print(del);
				prg = nFmt.format(100.0 * (double)counter / (double)totalGrid) +"%       ";
				System.out.print(prg);
				
//					System.out.print( "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
//					System.out.print( nFmt.format(100.0 * (double)counter / (double)totalGrid) +"%      ");
			}
			
			if ( checkJp && ! jpt.hasMap( geoY + geoYstep * sumUp , geoY , geoX + geoXstep * sumUp , geoX ) ){
				// ���{��`�F�b�NOn�ŁA���{��O�Ȃ烋�[�v��΂�
//					System.out.println("out of ja");
				System.out.print("s");
				continue;
			}
			
//				System.out.println("*******intersec:" + geoX+","+(geoX + (geoXstep * sumUp))+","+ geoY+","+(geoY + (geoYstep * sumUp))+","+bounds);
			if ( ! bounds.intersects( new Envelope(geoX , geoX + (geoXstep * sumUp) , geoY , geoY + (geoYstep * sumUp)))){
				// �`��̈�Ƀt�B�[�`���[(shapefile�S�̂�bbox)���Ȃ��ꍇ����΂�
//					System.out.println("no feature in bbox");
				continue;
			}
			
//				if ( ! prevTileExistence.contains(getHashKey( tx , ty ) ) ){
//					// �ЂƂO�̒i�K�̃^�C���ɐ}�`�����݂��ĂȂ���Δ�΂�
//					System.out.print("s");
//					continue;
//				}
			System.out.print(".");
			
			String tileName;
				
			// �w�i�𓧖��ɂ��邽��ARGB���g��
//			BufferedImage image = new BufferedImage( imageWidth * sumUp , imageHeight * sumUp , BufferedImage.TYPE_INT_ARGB );
			
//			ReferencedEnvelope geoRect = new ReferencedEnvelope(geoX , geoX + (geoXstep * sumUp) , geoY , geoY + (geoYstep * sumUp) , crs);
			
			if ( rebuildContainerOnly ){
				// �����_�����O�͍s�Ȃ�Ȃ��E�E sumUp != 1�ł̓o�O��̂ł́H(2013.3.28)
				tileName = "tile" + tx + "_" + ty + ".png";
				File imageFile = new File(outDir + tileName);
				if ( ! imageFile.exists()){
					continue;
				}
			} else {
				int[] txy = { tx, ty};
				tilePosList.add(  txy );
				// �`�悷�邽�߂̃X���b�h����������
//				System.out.println("build runnable");
//				TileRenderRunnable trr = new TileRenderRunnable( antiAlias, map, image, rect, geoRect, tx, ty );
//				tileRenderRunnables.add( trr );
			}
		}
		
		System.out.println("\n Build images");
		
//		ArrayList<Thread> tileRenderThreads;
		ArrayList<TileRenderRunnable> tileRenderRunnables;
		
		int threadBlockIndex = 0;
		while ( threadBlockIndex < tilePosList.size() ){
//			tileRenderThreads = new ArrayList<Thread>();
			tileRenderRunnables = new ArrayList<TileRenderRunnable>();
			
			ArrayList<Future<?>> futureList = new ArrayList<Future<?>>();
			
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				int[] txy = tilePosList.get(threadCount);
				int tx = txy[0];
				int ty = txy[1];
				geoX = tx * geoXstep * sumUp + geoXstart;
				geoY = ty * geoYstep * sumUp + geoYstart;
				
				// �`��p�C���[�W��p�ӂ���F�w�i�𓧖��ɂ��邽��ARGB���g��
				BufferedImage image = new BufferedImage( imageWidth * sumUp , imageHeight * sumUp , BufferedImage.TYPE_INT_ARGB );
				// �`��n�����W�̈�𐶐�����
				ReferencedEnvelope geoRect = new ReferencedEnvelope(geoX , geoX + (geoXstep * sumUp) , geoY , geoY + (geoYstep * sumUp) , crs);
				
				
				//�`��runnable�𐶐�����
//				System.out.println("build runnable");
				TileRenderRunnable trr = new TileRenderRunnable( antiAlias, map, image, rect, geoRect, tx, ty, sumUp, reqTile, rebuildContainerOnly , outDir );
				tileRenderRunnables.add(trr);
				
				//�r�b�g�C���[�W�`��E�^�C�������E�ۑ��X���b�h���N������
//				System.out.println("build thread");
				
				/**
				Thread th =  new Thread(trr);
				tileRenderThreads.add(th);
				th.start();
				**/
				
				Future<?> future = svgMapExecutorService.submit(trr);
				futureList.add(future);
				
//				(tileRenderThreads.get(threadCount - threadBlockIndex)).start();
			}
			
			// �`��X���b�h�����̓����̂��߂̑ҋ@���[�v
			/**
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				(tileRenderThreads.get(threadCount - threadBlockIndex)).join();
				
			}
			**/
			for (Future<?> future : futureList) {
				future.get();
			}
			
			
			// �R���e�iSVG(�ꍇ�ɂ���Ă͊K�w�I)��������
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				TileRenderRunnable trr = tileRenderRunnables.get(threadCount - threadBlockIndex);
				int tx = trr.tx;
				int ty = trr.ty;
				geoX = tx * geoXstep * sumUp + geoXstart;
				geoY = ty * geoYstep * sumUp + geoYstart;
				
				int[][] hasData = trr.hasData;
				boolean[][] isImage = trr.isImage;
				
				int divs = sumUp / 2;
				if ( sumUp == 1 ){ divs = 1; } // sumUp == 1�̓��ꏈ��
				for ( int subX = 0 ; subX < sumUp ; subX++ ){
					int ttx = tx * sumUp + subX;
					smX = ttx / containerCounts;
					for ( int subY = 0 ; subY < sumUp ; subY++ ){
						int tty = ty * sumUp + subY;
						smY = tty / containerCounts;
						SvgMap smp;
						if ( isImage[subX][subY] ){
							if (hasSmsMap ){
								if ( !smsMap.containsKey(getHashKey(smX,smY) )){
									smp = buildSms(smX , smY , smsMap , bounds , tcmul , nFmt , containerName , outDir );
								} else {
									smp = getSms(smX,smY,smsMap);
								}
							} else {
								smp = sm;
							}
	//						System.out.println("subX,subY:" + subX +","+subY);
							String tileName = "tile" + ttx + "_" + (GridYsize - tty -1) + ".png";
							smp.putImage( 
								new Coordinate( (geoX + subX * geoXstep ) * tcmul ,
									( - ( geoY + subY * geoYstep + geoYstep ) * tcmul  ) ) ,
								geoXstep*tcmul , geoYstep * tcmul  , tileName );
						}
					}
				}
				
				if ( sumUp == 1 && hasData[0][0] > 0 ){ // sumUp==1�̓��ꏈ��
					hasData[0][0] = 1; hasData[0][1] = 1; hasData[1][0] = 1; hasData[1][1] = 1; 
				}
				for ( int i = 0 ; i < 2 ; i++){
					for ( int j = 0 ; j < 2 ; j++){
						if ( hasData[i][j] > 0 ){
							thisTileExistence.add(getHashKey( i + tx * 2, j + ty * 2 ));
						}
					}
				}
			}
			threadBlockIndex += maxThreads;
		}
		
		prevTileExistence = thisTileExistence;
		
		if ( hasSmsMap ){
			Iterator<Long> it = smsMap.keySet().iterator();
			while ( it.hasNext() ){
				Long skey = (Long)it.next();
				int[] smsix = getIndex(skey);
				int i = smsix[0];
				int j = smsix[1];
				geoY = geoYstart + j * containerCounts * geoYstep;
				geoX = geoXstart + i * containerCounts * geoXstep;
				SvgMap oneSms = smsMap.get(skey);
				oneSms.termGroup();
				oneSms.putFooter();
				String subCfilePath = "sub_" + i + "_" + j + "_" + containerName;
//					File mapFile = new File( outDir + subCfilePath );
//					if ( mapFile.exists()){}
				sm.putImage( new Coordinate( geoX * tcmul , ( - (geoY + containerCounts * geoYstep) * tcmul  ) ) ,
				geoXstep * containerCounts * tcmul , geoYstep * containerCounts * tcmul  , subCfilePath );
			}
		}
		sm.termGroup();
		sm.putFooter();
		
//		System.out.println("</g>");
//		System.out.println("</svg>");
		map.dispose();
		
		return (containerFileName);
	}
	
	
	
	// �|���S���X�^�C���̐����֐�
	// Color�́@new Color(47, 184, 27)�@����Ȃ���
	public Style getFeatureStyle( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema ){
		//�X�^�C�����쐬
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		Rule rule = getOneSymbolizeRule( fillColor , strokeColor , strokeWidth , dash , markerSize , schema );
		
/**			
		// add for checking
//		Filter filter = ff.equals( ff.property("Brand") , ff.literal("ENEOS") );
		Filter filter1 = ff.greater( ff.property("test3") , ff.literal(35.5) );
		Filter filter2 = ff.less( ff.property("test3") , ff.literal(38.5) );
		Filter filter = ff.and( filter1 , filter2 );
		rule.setFilter(filter);
		
		// test2
		Rule ruleOther = getOneSymbolizeRule( getColor("#A0A000") , getColor("#000000") , 1 , false , 3 , schema );
		ruleOther.setElseFilter(true);
			
		
		Rule rules[] = {rule , ruleOther};
**/
		Rule rules[] = {rule};
		FeatureTypeStyle fts = sf.createFeatureTypeStyle(rules);
		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return ( style );
	}
	
	
	// �|���S���X�^�C���̐����֐��Q�@�l�ɉ����ĐF��ω�������@�\�p
	public Style getFeatureStyle2( int colorCol , boolean dash , int markerSize ,  SimpleFeatureSource fs , double fMin , double fMax ) throws Exception {
		System.out.println("Call getFeatureStyle2 : colorCol:" + colorCol );
		FeatureType schema = fs.getSchema();
		//�X�^�C�����쐬
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		SVGMapGetColorUtil colu;
		if ( colorKeys ==""){
//			if ( colorTable == SVGMapGetColorUtil.HSV ){
//				colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol  );
//			} else {
				colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol , -1 , colorTable , keyLength , true , "" );
//			}
			colu.getAttrExtent(true);
		} else { // add 2016.2.4 (-colorkey)
			colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol , -1 , SVGMapGetColorUtil.HSV , 128 , true , colorKeys );
		}
		
		colu.setOutOfRangeView( outOfRangeView );
		
		
		for ( int i = 0 ; i < ((SimpleFeatureType)schema).getAttributeCount() ; i++){
//			System.out.print(i+":" + colu.getKanjiProp(((SimpleFeatureType)schema).getDescriptor(i).getLocalName()));
			System.out.print(i+":" + (((SimpleFeatureType)schema).getDescriptor(i).getLocalName()));
			if ( i == colorCol ){
				System.out.println(" => Selected Attribute");
			} else {
				System.out.println("");
			}
		}
		
		System.out.println("Attribute is Number? : "+ colu.mainAttrIsNumber);
		
//		String attrName = colu.getKanjiProp(((SimpleFeatureType)schema).getDescriptor(colorCol).getLocalName());
		String attrName = (((SimpleFeatureType)schema).getDescriptor(colorCol).getLocalName());
//		System.out.println("check attrName:"+attrName+" ::  2kanjiProp" + colu.getKanjiProp(attrName) + "   parseAsSJ:"+(new String(((String)attrName).getBytes("iso-8859-1"),"Windows-31J")).trim()+"   parseAsUTF:"+(new String(((String)attrName).getBytes("iso-8859-1"),"UTF-8")).trim());
//		attrName = colu.getKanjiProp(attrName);
		
		if ( colu.mainAttrIsNumber ) {
			System.out.println(" attrMin:" + colu.mainAttrMin + "  attrMax:" + colu.mainAttrMax );
			if ( fMin >-9e99 ){
				System.out.println(" But you forced min:" + fMin + "  max:" + fMax );
				colu.mainAttrMin = fMin;
				colu.mainAttrMax = fMax;
			}
		} else {
			HashMap colorMap = colu.colorMap;
			Set set = colorMap.keySet();
			Iterator it = set.iterator();
			while (it.hasNext()){
				String o = (String)it.next();
				System.out.println(colorMap.get(o) + " = " + o);
			}
		}
		
		
		Rule rules[] = null;
		
		if ( colu.mainAttrIsNumber ){
			
			// outOfRangeView...
			
			double vSpan = (colu.mainAttrMax - colu.mainAttrMin) / (double)colorResolution;
			
			if ( outOfRangeView == SVGMapGetColorUtil.COUNTER_STOP || outOfRangeView == SVGMapGetColorUtil.MARK ){
				// �I�[�o�[�t���[�f�[�^�ɑ΂���`������@�Q���ǉ�
				rules = new Rule[colorResolution+2];
				
				// �I�[�o�[�t���[��
				Color overFlowColor =  getColor(colu.getColor( colu.mainAttrMax + 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax ));
				Filter overFlowFl =  ff.greater( ff.property(attrName) , ff.literal(colu.mainAttrMax) );
				Rule overFlowRl = getOneSymbolizeRule( overFlowColor , overFlowColor , 0 , dash , markerSize , schema );
				overFlowRl.setFilter(overFlowFl);
				rules[colorResolution] = overFlowRl;
				
				// �A���_�[�t���[��
				Color underFlowColor =  getColor(colu.getColor( colu.mainAttrMin - 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax ));
				Filter underFlowFl = ff.less( ff.property(attrName) , ff.literal(colu.mainAttrMin) );
				Rule underFlowRl = getOneSymbolizeRule( underFlowColor , underFlowColor , 0 , dash , markerSize , schema );
				underFlowRl.setFilter(underFlowFl);
				rules[colorResolution+1] = underFlowRl;
				
			} else {
				rules = new Rule[colorResolution];
			}
			
			for ( int i = 0 ; i < colorResolution ; i ++ ){
				double vLow = i * vSpan + colu.mainAttrMin;
				double vHigh = vLow + vSpan;
				String color = colu.getColor( vLow + 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax );
				Filter fl1;
				if ( i == 0 ){
					fl1 = ff.greaterOrEqual( ff.property(attrName) , ff.literal(vLow) );
				} else {
					fl1 = ff.greater( ff.property(attrName) , ff.literal(vLow) );
				}
				Filter fl2 = ff.lessOrEqual( ff.property(attrName) , ff.literal(vHigh) );
				Filter fl = ff.and( fl1 , fl2 );
				Rule rl = getOneSymbolizeRule( getColor(color) , getColor(color) , 0 , dash , markerSize , schema );
				rl.setFilter(fl);
				rules[ i ] = rl;
				System.out.println(color + " = " + vLow + " ... " + vHigh);
			}
			
		} else {
			HashMap colorMap = colu.colorMap;
			HashSet truncatedKey = colu.truncatedKey; // added 2018.8.31
			rules = new Rule[colorMap.size()+1];
			
			Set set = colorMap.keySet();
			Iterator it = set.iterator();
			int i = 0;
			ArrayList<Filter> orList = new ArrayList<Filter>();
			while (it.hasNext()){
				String o = (String)it.next();
				String color = (String)colorMap.get(o);
				boolean truncated = false;
				if ( truncatedKey.contains(o) ){
					truncated = true;
				}
//				Filter fl = ff.begins( ff.property(attrName) , ff.literal( new String( o.getBytes("Shift_JIS"), 0) ) );
//				Filter fl = ff.equals( ff.property(attrName) , ff.literal( new String( o.getBytes("Shift_JIS"), 0) ) );
				
				Filter fl = null;
				if ( truncated ){ // �J�b�g���ꂽ�L�[�̏ꍇ�́A���C���h�J�[�h�w�� added 2018.8.31
					fl = ff.like( ff.property(attrName) , o+"*" );
				} else { // �����łȂ����̂͊��S��v
					fl = ff.like( ff.property(attrName) , o );
				}
				
				// �ȉ��Q�s�͂���������E�E�E�@�L�[���Z�k���ꂽ���̂̏ꍇ�̓��C���h�J�[�h�Ō����A�����łȂ����̂̏ꍇ�͊��S��v���K�v�@��ɏC�� 2018.8.31
//				Filter fl = ff.like( ff.property(attrName) , o+"*" ); // http://www.programcreek.com/java-api-examples/index.php?api=org.opengis.filter.FilterFactory2 ���Q�l�ɂ������C���h�J�[�h�����E�E(���̕��������������) 2017.5.12
//				Filter fl = ff.like( ff.property(attrName) , o ); // ���C���h�J�[�h�͂܂����ꍇ������B���S��v�ɏC���E�E�@debug: 2018/8/9
				
//				Rule rl = getOneSymbolizeRule( getColor(color) , getColor("#000000") , 0 , dash , markerSize , schema );
				Rule rl = getOneSymbolizeRule( getColor(color) , getColor(color) , 0 , dash , markerSize , schema );
				rl.setFilter(fl);
				rules[ i ] = rl;
				orList.add(fl);
				++i;
			}
			// ����ȊO�̏ꍇ��nullColor�̃A�C�R����u�����߂̃��[�������
			@SuppressWarnings("unchecked")
			Filter otfl = ff.or(orList);
			otfl = ff.not(otfl);
			Rule otrl = getOneSymbolizeRule( getColor(colu.nullColor) , getColor(colu.nullColor) , 0 , dash , markerSize , schema );
			otrl.setFilter(otfl);
			rules[ colorMap.size() ] = otrl;
		}
		
		
//		Rule rule = getOneSymbolizeRule( fillColor , strokeColor , strokeWidth , dash , markerSize , schema );
		
		FeatureTypeStyle fts = sf.createFeatureTypeStyle(rules);
		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return ( style );
	}
	
	
	
	
	Rule getOneSymbolizeRule( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema ){
		//�X�^�C�����쐬
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		Class<?> type = schema.getGeometryDescriptor().getType().getBinding();
		Rule rule = sf.createRule();
		if(type.isAssignableFrom( com.vividsolutions.jts.geom.Point.class) || 
		type.isAssignableFrom( com.vividsolutions.jts.geom.MultiPoint.class) ){
			if ( markerSize <= 0 ){
				markerSize = strokeWidth * 3; // �Ƃ肠�����ˁE�E
			}
//			System.out.println("Point Style : " +  fillColor + ", size:" + markerSize );
			// point����ǉ�
			Mark mark = sf.getCircleMark(); // �~�`�̃}�[�N��I�� �ȉ��Q�s�Ő��Ɠh��ݒ�
			org.geotools.styling.Stroke strokeP = sf.createStroke(
				ff.literal(strokeColor),
				ff.literal(strokeWidth)
			);
			mark.setStroke(strokeP);
			org.geotools.styling.Fill fillP = sf.createFill(ff.literal(fillColor));
			mark.setFill(fillP);
		
			Graphic graphic = sf.createDefaultGraphic();
			graphic.graphicalSymbols().clear();
			graphic.graphicalSymbols().add(mark);
			graphic.setSize(ff.literal(markerSize));
			
			PointSymbolizer poiSym = sf.createPointSymbolizer();
	//		poiSym.setGraphic(sf.getDefaultGraphic());
			poiSym.setGraphic(graphic);
			
			rule.symbolizers().add(poiSym);
		} else if(type.isAssignableFrom( com.vividsolutions.jts.geom.LineString.class) ||
		type.isAssignableFrom( com.vividsolutions.jts.geom.MultiLineString.class) ){
//			System.out.println("Line Style");
			// ���̐F�E����
			org.geotools.styling.Stroke stroke = null;
			if ( strokeWidth <= 0 ){
				strokeWidth = 1;
			}
			if ( strokeColor != null && strokeWidth > 0 ){
				if ( !dash ){
					stroke = sf.createStroke( ff.literal(strokeColor) , ff.literal(strokeWidth) );
				} else {
					stroke = sf.createStroke( 
						ff.literal(strokeColor) ,
						ff.literal(strokeWidth) ,
						ff.literal(1),
						ff.literal("bevel"),
						ff.literal("square"),
							new float[] { (float)(strokeWidth*3.0), (float)(strokeWidth*3.0) },
						ff.literal(0),
						null,
						null
					);
				}
			}
			
			LineSymbolizer sym = sf.createLineSymbolizer(stroke , null);
			
			rule.symbolizers().add(sym);
		} else {
//			System.out.println("Polygon Style");
			// �|���S���̐��̐F
			org.geotools.styling.Stroke stroke = null;
			if ( strokeColor != null && strokeWidth > 0 ){
				stroke = sf.createStroke( ff.literal(strokeColor) , ff.literal(strokeWidth) );
			}
			
			// �|���S���̓h��Ԃ��F
			org.geotools.styling.Fill fill = null;
			if ( fillColor != null ){
				fill = sf.createFill( ff.literal(fillColor) );
			}
			
			PolygonSymbolizer sym = sf.createPolygonSymbolizer(stroke, fill, null);
			
			rule.symbolizers().add(sym);
		}
		
		return ( rule );
	}
	
	boolean notUseIsNullImage = false;
	
	// Image���J���Ȃ̂��ǂ����𔻕ʂ���E�E�E�@�d������?�قǂł͂Ȃ� �����_�����y���Ɏx�z�I������
	boolean isNullImage( BufferedImage image ){
//		System.out.print("isNullImg?:");
		boolean ans = true;
		if (notUseIsNullImage ){
			ans = false;
		} else {
			int height = image.getHeight();
			int width  = image.getWidth();
			loop0: for ( int i = 0 ; i < height ; i++ ){
				for ( int j = 0 ; j < width ; j++ ){
					int pix = image.getRGB(j , i) ;
					if ( pix >> 24 != 0 ){
						ans = false;
						break loop0;
					}
				}
			}
		}
//		System.out.println(ans);
		return ( ans );
	}
	
	int getLvl(double deg ){
		int lvl = 0;
		double sub = 360 / deg;
		while ( sub > 1){
			sub = sub / 2;
			++lvl;
		}
		return (lvl);
	}
	
	double getPartDeg(int lvl){
		double partD = 360.0;
		for ( int i = 0 ; i < lvl ; i++){
			partD = partD / 2.0;
		}
		return ( partD );
	}
	
	private HashMap<Long,SvgMap> buildSmsMap( int GridXsize , int GridYsize ) throws Exception{
		return ( new HashMap<Long,SvgMap>() );
		
	}
	private SvgMap getSms( int GridX , int GridY , HashMap<Long,SvgMap> smsMap ) throws Exception{
		return( (SvgMap)smsMap.get(getHashKey( GridX , GridY ) ) );
	}
	
	public SvgMap buildSms( int j , int i , HashMap<Long,SvgMap> smsMap , ReferencedEnvelope bounds , double tcmul , NumberFormat nFmt ,  String fileName , String dir ) throws Exception{
		double lngMin  = bounds.getMinX();
		double latMin  = bounds.getMinY(); 
		double lngSpan = bounds.getWidth();
		double latSpan = bounds.getHeight();
		SvgMap newsm = new SvgMap ( dir + "sub_" + j + "_" + i + "_" + fileName , nFmt);
		newsm.putHeader( lngMin * tcmul , - (latMin + latSpan) * tcmul  , lngSpan * tcmul , latSpan * tcmul );
		newsm.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		newsm.setGroup();
		smsMap.put(getHashKey(j,i),newsm);
		return ( newsm);
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
	
	private class  TileRenderRunnable implements Runnable{
		
		StreamingRenderer renderer;
		Graphics2D gr;
		BufferedImage image;
		Rectangle rect;
		ReferencedEnvelope geoRect;
		int tx, ty;
		int[][] hasData;
		boolean[][] isImage;
		String outDir;
		
		TileRenderRunnable( boolean antiAlias , DefaultMapContext map , BufferedImage image , Rectangle rect , ReferencedEnvelope geoRect , int tx, int ty , int sumUp , HashSet<Long> reqTile , boolean rebuildContainerOnly , String outDir ){
			renderer = new StreamingRenderer();
			if ( antiAlias ){
				// �A���`�G���A�X�֌W�̃q���g��t����
				RenderingHints rh=new RenderingHints(
					RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
				rh.add(new RenderingHints(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON));
				rh.add(new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
				renderer.setJava2DHints(rh);		
			}
			renderer.setContext(map);
			this.gr = gr;
			this.rect = rect;
			this.geoRect = geoRect;
			this.tx = tx;
			this.ty = ty;
			this.image = image;
			this.outDir = outDir;
			gr = image.createGraphics();
			int[][] hd = {{0,0},{0,0}};
			hasData = hd;
			isImage = new boolean[sumUp][sumUp];
		}
		
		public void run(){
			// geotools�ɓ�����gr�o�R��image�Ƀ����_�����O������
//			System.out.println("Thread: tx:" + tx + ", ty:"+ ty + "  �`��J�n");
			renderer.paint(gr, rect, geoRect);
//			System.out.println("Thread: tx:" + tx + ", ty:"+ ty + "  �`��I��");
			
			// sumUp�^�C���̕�������
			
			int divs = sumUp / 2;
			if ( sumUp == 1 ){ divs = 1; } // sumUp == 1�̓��ꏈ��
			for ( int subX = 0 ; subX < sumUp ; subX++ ){
				int ttx = tx * sumUp + subX;
				int smX = ttx / containerCounts;
				for ( int subY = 0 ; subY < sumUp ; subY++ ){
					isImage[subX][subY] = false; // �O�̂��߁E�E
					int tty = ty * sumUp + subY;
					int smY = tty / containerCounts;
					if ( ttx >= GridXsize || tty >= GridYsize ){
						continue;
					}
					if ( reqTile != null && ! reqTile.contains(getReqTileKey( ttx, tty ) ) ){ // 2014.2.28 debug
						continue;
					}
					String tileName = "tile" + ttx + "_" + (GridYsize - tty -1) + ".png";
					if ( !rebuildContainerOnly ){
						BufferedImage bim = image.getSubimage( imageWidth * subX , imageHeight * (sumUp - 1 - subY) , imageWidth , imageHeight );
						if( reqTile == null && isNullImage( bim ) ){ // 2014.5.2 debug line obj�̏ꍇnullImage�ɂȂ�Ƃ�������̂ŁE�E
//								System.out.println("NULL IMAGE....");
							continue;
						} else {
							++ hasData[subX/divs][subY/divs];
						}
						try{
							ImageIO.write(bim , "png" , new File(outDir + tileName ) );
						} catch ( Exception e ){
							System.out.println(e);
							System.exit(0);
						}
						isImage[subX][subY] = true;
					} else {
						isImage[subX][subY] = true;
					}
//						System.out.println("subX,subY:" + subX +","+subY);
				}
			}
			
			if ( sumUp == 1 && hasData[0][0] > 0 ){ // sumUp==1�̓��ꏈ��
				hasData[0][0] = 1; hasData[0][1] = 1; hasData[1][0] = 1; hasData[1][1] = 1; 
			}
			
			System.out.print("*");
		}
	}	
	
}

