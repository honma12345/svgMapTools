package org.svgmap.shape2svgmap;

// SVGMapGetColorUtil feature��prop�l����color�����o�����[�e�B���e�B
// ���Ƃ��Ƃ�Shape2SVGMap*�̓����ɂ��������̂�؂�o���B
// Shape2ImageSVGMap�ɓ����@�\��݂��邽�߂ɍ쐬
// Shape2SVGMap*��������ɋ@�\���ڏ�����������l���Ă���
//
// Programmed by Satoru Takagi (2014/05-)
// Copyright 2014 by Satoru Takagi @ KDDI All Rights Reserved
//
// 2016/02/05 colorkeys�𐔒l�^�ł��L���ɂ���
// 2016/04/0x Shape2SVGMap19�ȍ~�́A������g�p����`�ɋ��ʉ����������܂��E�E
// 2017/05/15 outofrange�ɑ΂��鏈�����O��(skip,nullColor,counterStop)
//



import java.awt.*;
import java.awt.image.*;

import java.util.*;


import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.text.NumberFormat ;
import java.text.DecimalFormat;
import java.awt.geom.*;


import org.geotools.data.shapefile.*;
import org.geotools.data.simple.*;
import org.opengis.filter.*;
import org.geotools.factory.*;
import org.geotools.geometry.jts.*;
import org.geotools.map.*;
import org.geotools.renderer.lite.*;
import org.geotools.styling.*;
import org.geotools.feature.*;
import org.opengis.feature.simple.*;
import org.opengis.referencing.crs.*;
import org.opengis.feature.type.*;

import com.vividsolutions.jts.geom.*;

public class SVGMapGetColorUtil {
	// numcolor�̕��@
	static final int RED = 0;
	static final int HSV = 1;
	static final int QUOTA = 2;
	static final int iHSV = 3;
	
	// outOfRagte�̕\�����@
	static final int MARK = 0; // �A�E�g�I�u�����W�J���[�Ń}�[�N ( default )
	static final int SKIP = 1; // �\�����Ȃ��`�X�L�b�v
	static final int COUNTER_STOP = 2; // ����E�����l�ŃX�e�B�b�N
	
	static int colorMapMax=1024;
	static String nullColor = "#808080";
	boolean counterStop = false;
	
	DecimalFormat tzformat = new DecimalFormat("0.###########");
	// fsShape    �f�[�^
	// mColorCol  ���C���̐F�̑����ԍ�
	// oColorCol  �T�u(�A�E�g���C��)�̐F�̑����ԍ�
	// colorTable �F�e�[�u��(SVGMapGetColorUtil.RED,SVGMapGetColorUtil.HSV,SVGMapGetColorUtil.QUOTA,SVGMapGetColorUtil.iHSV)
	// keyLength  ������l�̏ꍇ�̃L�[��
	// strIsSJIS  ������̏ꍇ�̕����R�[�h��SJIS���ǂ���
	// colorKeys  
	//		������ɂ�����n�b�V���L�[(�J���[�e�[�u��)��񋓌^������Ŏw��i�F���сF�����j");
	//		CSV�ő����l��񋓂���B�e�����l�̌��#xxxxxx�������ꍇ�͒��ڐF���w��ł���");
	//		�n�b�V���L�[�̕����񒷂��͑S�ē����łȂ��ƂȂ�Ȃ��B-numcolor�͂��̒l���玩���ݒ�");
	//		�����֌W����I�v�V�����F�F�̊��t��:-numcolor");
	//		��P�F�����l�P,�����l�Q,....�@��Q�F�����l�P#F08020,�����l�Q#30D000,....");
	//
	// �G�N�X�e���g���v�Z���邽�߂� getAttrExtent( boolean makeStringColorMap ) ���ĂԕK�v������B
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI , int oColorColI , int colorTableI , int keyLengthI , boolean strIsSJISI , String colorKeys ){
		keyLength = keyLengthI;
		colorTable = colorTableI;
		strIsSJIS = strIsSJISI;
		mColorCol = mColorColI;
		oColorCol = oColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
		if ( colorKeys!=""){
			mainAttrIsNumber = false;
			colorMap = initColorKeyEnum( colorKeys );
		}
	}
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI  ){
		mColorCol = mColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI , int colorTableI  ){
		colorTable = colorTableI;
		mColorCol = mColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	FeatureCollection fsShape;
	
	int mColorCol = -1;
	int oColorCol = -1;
	int colorTable = HSV;
	int keyLength = 128;
	boolean strIsSJIS = true;
	
	
	public HashMap<Object,String> colorMap = new HashMap<Object,String>(); // �����ɕ�����`�񋓒l�̏ꍇ�̃J���[�e�[�u�������܂�(Key:������,Val:#color)
	
	
	public double mainAttrMax = -9e99;
	public double mainAttrMin = 9e99;
	public double outlineAttrMax = -9e99;
	public double outlineAttrMin = 9e99;
	
	public boolean mainAttrIsNumber = true;
	public boolean outlineAttrIsNumber = true; 
	
	public String getMainColor( SimpleFeature  oneFeature ){
		return ( getColor( oneFeature.getAttribute(mColorCol) , mainAttrMin , mainAttrMax) );
	}
	public String getOutlineColor( SimpleFeature  oneFeature ){
		if ( oColorCol >=0 ){
			return ( getColor( oneFeature.getAttribute(oColorCol) , outlineAttrMin , outlineAttrMax) );
		} else {
			return ("none");
		}
	}
	
	// extent�����l�̏ꍇ��true string�̏ꍇ��false
	public void getAttrExtent( ){
		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	public void getAttrExtent( boolean makeStringColorMap ){
		initColorSeq();
		getAttrExtent( fsShape , mColorCol , oColorCol , makeStringColorMap );
		initColorSeq();
	}
	
	public void setOutOfRangeView( int method ){
		if ( method == COUNTER_STOP ){
			counterStop = true;
		} else if ( method == SKIP ){
			nullColor ="";
		} else if ( method == MARK ){ 
			// default mark by nullColor
		} else {
			// do nothing
		}
	}
	
	
	private boolean useColorKeys = false;
	public String getColor( Object sValue , double attrMin , double attrMax){
		String key;
		if ( sValue == null ){
			return (nullColor);
		}
		String color = "green";
		double dValue = 0.0;
		if ( sValue instanceof String ){ // ������x�[�X�̑�������F������
//			System.out.println("color build fm STRING");
			String sVal = getKanjiProp((String)sValue);
			if ( sVal.length() > keyLength ){
				key = sVal.substring(0,keyLength);
			} else {
				key = sVal;
			}
//			key = (String)sValue + "  ";
//			key = key.substring(0,keyLength);
			
			if ( useColorKeys ){ // ���炩���ߐݒ肳�ꂽ�J���[�e�[�u�����g�� 2013/2
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					color = nullColor; // �e�[�u���ɖ������̂͊D�F
				}
			} else { // �K���Ȕz��
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					
					getRGBtSeq();
					color = "#" 
						+ Integer.toHexString(btR+256).substring(1,3)
						+ Integer.toHexString(btG+256).substring(1,3)
						+ Integer.toHexString(btB+256).substring(1,3);
					
	//				color = "#" + Integer.toHexString(getBtSeq()).toUpperCase();
					colorMap.put(key , color);
				}
			}
//			System.out.println(sVal + " : " + key + " : " + color);
		} else { // ���l�x�[�X�̑�������F�����
//			System.out.println("color build fm number");
			if ( sValue instanceof Integer ){
				dValue = ((Integer)sValue).doubleValue();
			} else if ( sValue instanceof Double ){
				dValue = ((Double)sValue).doubleValue();
			} else if ( sValue instanceof Long ){
				dValue = ((Long)sValue).doubleValue();
			}
			
			// �ŏ��ő�l�𒴂������͖̂����F��ݒ肷��i���F���N���b�v����j
			// �{���A���̎d�l���I�v�V�����őI�ׂ�ׂ�����
			if ( dValue < attrMin ){
				if ( !counterStop ){
					color = nullColor;
					dValue = attrMin;
	//				System.out.println("< attrMin :" + color);
					return ( color );
				} else {
					dValue = attrMin;
				}
			} else if ( dValue >attrMax ){
				if ( !counterStop ){
					color = nullColor;
					dValue = attrMax;
	//				System.out.println("> attrMax :" + color);
					return ( color );
				} else {
					dValue = attrMax;
				}
			} 
			
			if ( useColorKeys ){ // ���l�ɑ΂��Ă��J���[�L�[�e�[�u�����g����悤�ɂ���(2016.2.5)
				key = tzformat.format(dValue);
				if ( colorMap.containsKey( key ) ){
					color = (String)colorMap.get(key);
				} else {
					color = nullColor; // �e�[�u���ɖ������̂͊D�F
				}
			} else if ( colorTable == HSV || colorTable == iHSV){
				// HSV�F�@��(0)�F�Œ�A�@��(270���炢)�F�ō��@�œh��킯 (����300���炢�܂ł��Ó�����)
//				getRGB( ( 360.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
				if ( dValue >= attrMin && dValue <= attrMax){
					if ( colorTable == iHSV ){
						getRGB( ( 270.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
					} else {
						getRGB( 270.0 - ( 270.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
					}
				} else { // ��̐ݒ�ŁA����͂Ȃ��Ȃ��Ă邩�ȁi���̕ӂ��I�v�V�����I�𐧂̗��R�j
					cr = 0;
					cg = 0;
					cb = 0;
				}
				color = "#" + Integer.toHexString(256 + cr).substring(1,3)
				+ Integer.toHexString(256 + cg).substring(1,3)
				+ Integer.toHexString(256 + cb).substring(1,3);
				if ( colorMap.size() < colorMapMax ){ // ���l��colorMap�͎g���Ă��Ȃ��̂ł́H�i�Ō�ɃR�����g�o�͂���p�r�����ł����g���ĂȂ��j
					if ( ! colorMap.containsKey(sValue) ){
						colorMap.put( sValue, color );
					}
				}
			} else if ( colorTable == RED) {
				color = "#" + Integer.toHexString(256 + (int)(255.9 * ( (dValue - attrMin ) / (attrMax - attrMin)))).substring(1,3) + "0000";
				if ( colorMap.size() < colorMapMax ){
					if ( ! colorMap.containsKey(sValue) ){
						colorMap.put( sValue, color );
					}
				}
			} else { // �����Ƃ����������ȐF�����@
				key = Double.toString(dValue);
//				key = key.substring(0,keyLength);
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					
					getRGBtSeq();
					color = "#" 
						+ Integer.toHexString(btR+256).substring(1,3)
						+ Integer.toHexString(btG+256).substring(1,3)
						+ Integer.toHexString(btB+256).substring(1,3);
					colorMap.put(key , color);
				}
				
			}
			
//			System.out.println("in:" + sValue + " double:" + dValue + " color:" + color );
		}
		
		
		return ( color );
	}
	
	
	
	
	
	private void getAttrExtent(FeatureCollection fsShape , int mColorCol , int oColorCol , boolean makeStringColorMap ){
		SimpleFeature oneFeature = null;
		Object valueM , valueO;
		double dvalM = 0;
		double dvalO = 0;
		@SuppressWarnings("unchecked")
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		boolean mValIsStr = false;
		boolean oValIsStr = false;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("ERR");
					hasFeature = false;
				}
			}
			if ( mColorCol >=0  && !mValIsStr ){
				valueM = (oneFeature.getAttribute(mColorCol));
				if ( valueM != null ){
					if ( valueM instanceof Double ){
						dvalM = ((Double)valueM).doubleValue();
					} else if ( valueM instanceof Integer ){
						dvalM = ((Integer)valueM).intValue();
					} else if ( valueM instanceof Long ){
						dvalM = ((Long)valueM).longValue();
					} else {
						mainAttrIsNumber = false;
						if ( makeStringColorMap ){
							registColorMap((String)valueM);
						} else {
							mValIsStr = true;
							System.out.println("main Value is String");
						}
					}
					
					if ( dvalM > mainAttrMax ){
						mainAttrMax = dvalM;
					}
					if ( dvalM < mainAttrMin ){
						mainAttrMin = dvalM;
					}
				}
			}
			if ( oColorCol >=0 && !oValIsStr ){
				valueO = (oneFeature.getAttribute(oColorCol));
				if ( valueO != null ){
					if ( valueO instanceof Double ){
						dvalO = ((Double)valueO).doubleValue();
					} else if ( valueO instanceof Integer ){
						dvalO = ((Integer)valueO).intValue();
					} else if ( valueO instanceof Long ){
						dvalO = ((Long)valueO).longValue();
					} else {
						outlineAttrIsNumber = false;
						if ( makeStringColorMap ){
							registColorMap((String)valueO);
						} else {
							oValIsStr = true;
							System.out.println("outline Value is String");
						}
					}
					
					if ( dvalO > outlineAttrMax ){
						outlineAttrMax = dvalO;
					}
					if ( dvalO < outlineAttrMin ){
						outlineAttrMin = dvalO;
					}
				}
			}
			if ( mValIsStr && oValIsStr ){
				// �����Ƃ������񂾂�����Ӗ��Ȃ��̂ŏI��
				break;
			}
		}
		if ( mColorCol >=0 && !mValIsStr ){
			System.out.println( "mainColorAttr    Col:" + mColorCol + ": Min:" + mainAttrMin + " Max:" + mainAttrMax );
		}
		if ( oColorCol >=0 && !oValIsStr ){
			System.out.println( "outlineColorAttr Col:" + oColorCol + ": Min:" + outlineAttrMin + " Max:" + outlineAttrMax );
		}
	}
	
	// �O���[�o���ϐ��ɒ��ړ������Ă���
	private void registColorMap( String sValue ){
		String sVal = getKanjiProp((String)sValue);
		String key;
		if ( sVal.length() > keyLength ){
			key = sVal.substring(0,keyLength);
		} else {
			key = sVal;
		}
		if ( colorMap.containsKey(key) ){
		} else {
			getRGBtSeq();
			String color = "#" 
				+ Integer.toHexString(btR+256).substring(1,3)
				+ Integer.toHexString(btG+256).substring(1,3)
				+ Integer.toHexString(btB+256).substring(1,3);
			colorMap.put(key , color);
		}
	}
	
	private HashMap<Object,String> initColorKeyEnum( String colorKeys ){ // add 2013/2 for -colorkey
		
		
		HashMap<Object,String> colorMap = new HashMap<Object,String>();
		colorMap.put("default" , "green");
		
		if (colorKeys.length() == 0){
			return ( colorMap );
		}
		
		String[] colorKeyEnum  = colorKeys.split(",");
//		System.out.println("colorEnum:" + colorKeys );
		int kl=0;
		keyLength = 0;
		if ( colorKeyEnum.length > 1 ){
			useColorKeys = true;
			for ( int i = 0 ; i < colorKeyEnum.length ; i++ ){
				String ck;
				String color="";
				
				ck = colorKeyEnum[i];
//				System.out.println("orig:" + ck);
				
				if ( ck.indexOf("#") > 0){ // �p�����[�^�ŐF�𖾎����Ă���P�[�X
					color = ck.substring(ck.indexOf("#")); // �F��ݒ肷��
					ck = ck.substring(0,ck.indexOf("#"));
//					System.out.println("incl#" + ck.indexOf("#") + " : " + ck + " : " + color);
				}
				
				
				// key�̒����́A�p�����[�^���玩���ݒ肳����B�@���ׂẴL�[�̕����񒷂͓������K�v������
				kl = ck.length();
				if ( keyLength < kl ){
					keyLength = kl;
				}
				/**
				if ( i == 0 ){
					keyLength = ck.length();
				}
				
				if ( ck.length() != keyLength ){
					System.out.println("ERROR! Inconsistent key length.");
					System.exit(0);
				}
				**/
				/**
				if ( ck.length() > keyLength ){ // keyLength�����w�肵��Key�������Ƃ��͒Z�k����
					ck = ck.substring(0,keyLength);
				} else {
					ck = ck;
				}
				**/
				
				if ( color == ""){
					// �p�����[�^�ŐF�𖾎����Ă��Ȃ��P�[�X�ł́A�p�����[�^�̕��я����x�[�X��HTV||invHSV�ŐF��ݒ肷��
					if ( colorTable == iHSV ){
						getRGB( ( 270.0 * i  /  ( colorKeyEnum.length - 1 ) ) , 1.0 , 1.0 );
					} else {
						getRGB( 270.0 - ( 270.0 * i /  ( colorKeyEnum.length - 1 ) ) , 1.0 , 1.0 );
					}
					color = "#" + Integer.toHexString(256 + cr).substring(1,3)
						+ Integer.toHexString(256 + cg).substring(1,3)
						+ Integer.toHexString(256 + cb).substring(1,3);
				}
//				System.out.println("colorMap:" + ck + " : " + color );
				colorMap.put(ck , color);
			}
			System.out.println( "colorMap(" + colorMap.size() + "vals , keyLength:" + keyLength + "):" + colorMap );
		} else {
			System.out.println("ERROR! -colorkey syntax error");
			System.exit(0);
		}
		
		return ( colorMap );
	}

	private void test(){
		for (double h = 0 ; h < 360 ; h +=1 ){
			getRGB( h , 1.0 , 1.0 );
			System.out.println( "H:" + h + " : " + cr + ":" + cg + ":" + cb );
		}
	}
	
	
	private int cr, cg, cb;
	private void getRGB( double h , double s , double v ){
		// h: 0-360 , s: 0-1 , v: 0-1
		double f ;
		int m , n , k ;
		int i = (int)( h / 60.0 );
		v = v * 255.9;
		f = h / 60.0  - i;
		m = (int)(v * ( 1.0 - s ));
		n = (int)(v * ( 1.0 - s * f ));
		k = (int)(v * ( 1.0 - s * ( 1.0 - f ) ));
		switch (i){
		case 0:
			cr = (int)v;
			cg = k;
			cb = m;
			break;
		case 1:
			cr = n;
			cg = (int)v;
			cb = m;
			break;
		case 2:
			cr = m;
			cg = (int)v;
			cb = k;
			break;
		case 3:
			cr = m;
			cg = n;
			cb = (int)v;
			break;
		case 4:
			cr = k;
			cg = m;
			cb = (int)v;
			break;
		case 5:
			cr = (int)v;
			cg = m;
			cb = n;
			break;
		default:
			break;
		}
		
	}
	
	
	private static int btMin = 0;
	private static int btMax= 256;
	private int btR = 128;
	private int btG = 128;
	private int btB = 128;
	private int btStart = 128;
	private int btStep = 256;
	
	private void initColorSeq(){
		btR = 128;
		btG = 128;
		btB = 128;
		btStart = 128;
		btStep = 256;
	}
	
	private void getRGBtSeq(){
		if ((btR + btStep) <= btMax ){
			btR += btStep;
		} else {
			btR = btStart;
			if ((btG + btStep) <= btMax ){
				btG += btStep;
			} else {
				btG = btStart;
				if ((btB + btStep) <= btMax ){
					btB += btStep;
				} else {
					btStep = btStep / 2;
					btStart = btStep/2;
					btB = btStart;
					btG = btStart;
					btR = btStart;
					
				}
			}
		}
	}
	
	
	private int btSp = 256;
	private int btPrev = -1;
	private int getBtSeq(){
		// �񕪖ؐ���̐���
		if (btPrev < 0){
			btPrev = 0;
		} else if ( (btPrev + btSp) < btMax ) {
			btPrev += (btSp + btSp );
		} else {
			btSp = btSp/2;
			if (btSp < 1){
				btSp = btMax;
				btPrev = btMin;
			} else {
				btPrev = btSp;
			}
		}
		
		return ( btPrev );
	}

	String getKanjiProp( String input ){
		String ans ="";
		try {
			if ( strIsSJIS ){
				// 2013/02/15 WINDOWS...
//				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Shift_JIS")).trim();
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
			} else {
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"UTF-8")).trim();
			}
		} catch (Exception e){
			ans = "";
		}
		return ( ans );
	}

}