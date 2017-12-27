package org.svgmap.shape2svgmap;

// SVG Map�Ŏg���A�n�����W(XY��)����SVG���W�֕ϊ����邽�߂̍��W�ϊ��V�X�e��
// 2007/11/1 S.Takagi
// 2010.08.19 geoTools 2.6.5 support

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import org.geotools.data.DataStore;
import java.text.NumberFormat ;
import java.util.regex.*;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.*;
import org.geotools.feature.type.*;


import org.geotools.data.Transaction;
import org.geotools.filter.*;
// import org.geotools.data.vpf.*;
import com.vividsolutions.jts.geom.*;


public class SvgMapAffineTransformer {
	public int xySys; // �匳�̃f�[�^�����{��XY���W�n�̂Ƃ�(1-19:m , -1 - -19:mm)
	public int datum; // �����n�n�̂Ƃ�GeoConverter.BESSEL , WGS84:GeoConverter.JGD2000
	public GeoConverter gconv;
	public Transform g2s; // WGS84�n����SVG�n�ւ̕ϊ��p�����[�^
	
	SvgMapAffineTransformer( int dtm , int xys , Transform gs){
//		gconv.tiny = true; // WGS<>Bessel�ϊ����ȈՔł�
		xySys = xys;
		datum = dtm;
		if ( datum == GeoConverter.BESSEL ) {
			gconv = new GeoConverter(GeoConverter.Tky2JGD);
		} else {
			gconv = new GeoConverter();
		}
		g2s = gs;
	}
	
	SvgMapAffineTransformer( int dtm , int xys ){
//		gconv = new GeoConverter();
//		gconv.tiny = true; // WGS<>Bessel�ϊ����ȈՔł�
		xySys = xys;
		datum = dtm;
		if ( datum == GeoConverter.BESSEL ) {
			gconv = new GeoConverter(GeoConverter.Tky2JGD);
		} else {
			gconv = new GeoConverter();
		}
		g2s = new Transform ( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 ); // �Ƃ肠�����ˁE�E
	}
	
	public void setCrsTransform( Transform gs ){
		g2s = gs;
	}
	
	synchronized public  Coordinate transCoordinate( Coordinate inCrd ){
		Coordinate outCrd;
		if ( xySys > 0 ){
			gconv.setXY(inCrd.y , inCrd.x , xySys , datum );
			LatLonAlt BL = gconv.toWGS84();
			g2s.calcTransform(BL.longitude , BL.latitude );
		} else if ( xySys < 0 ){
			gconv.setXY(inCrd.y / 1000.0 , inCrd.x / 1000.0 , -xySys , datum );
			LatLonAlt BL = gconv.toWGS84();
			g2s.calcTransform(BL.longitude , BL.latitude );
			
		} else {
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(inCrd.y , inCrd.x , datum );
				LatLonAlt BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
			} else {
				g2s.calcTransform(inCrd.x , inCrd.y );
			}
		}
		outCrd = new Coordinate( g2s.x , g2s.y );
		return (outCrd);
	}
	
	synchronized public PolygonDouble transCoordinates( Coordinate[] inCrd ){
		Coordinate crd;
		PolygonDouble outPol = new PolygonDouble(inCrd.length);
		if ( xySys > 0 ){
			LatLonAlt BL;
			for ( int i = 0 ; i < inCrd.length ; i++){
				gconv.setXY(inCrd[i].y , inCrd[i].x , xySys , datum );
				BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
				outPol.addPoint( g2s.x , g2s.y );
			}
		} else if ( xySys < 0 ){
			LatLonAlt BL;
			for ( int i = 0 ; i < inCrd.length ; i++){
				gconv.setXY(inCrd[i].y / 1000.0 , inCrd[i].x / 1000.0 , -xySys , datum );
				BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
				outPol.addPoint( g2s.x , g2s.y );
			}
		} else {
			if ( datum == GeoConverter.BESSEL ){
				LatLonAlt BL;
				for ( int i = 0 ; i < inCrd.length ; i++){
					gconv.setLatLon(inCrd[i].y , inCrd[i].x , datum );
					BL = gconv.toWGS84();
					g2s.calcTransform(BL.longitude , BL.latitude );
					outPol.addPoint( g2s.x , g2s.y );
				}
			} else {
				for ( int i = 0 ; i < inCrd.length ; i++){
					g2s.calcTransform(inCrd[i].x , inCrd[i].y );
					outPol.addPoint( g2s.x , g2s.y );
				}
			}
		}
		return (outPol);
	}
	
	
	// ���W�Q�ƌn�̈Ⴂ���������AWGS84�n�ł�BBOX�����߂�
	synchronized public Envelope getWgsBounds( Envelope env){
		Envelope bounds= new Envelope();
		if ( xySys > 0 ) {
			// XY�n�̂Ƃ�
			// �V�F�[�v�t�@�C����XY�͐��K��XY�Ƌt�Ȃ�ł���ˁE�E�E
			gconv.setXY(env.getMaxY() , env.getMaxX() , xySys , datum);
			LatLonAlt maxBL = gconv.toWGS84();
			gconv.setXY(env.getMinY() , env.getMinX() , xySys , datum);
			LatLonAlt minBL = gconv.toWGS84();
			
			bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
		} else if ( xySys < 0 ) {
			gconv.setXY(env.getMaxY()/1000.0 , env.getMaxX()/1000.0 , -xySys , datum);
			LatLonAlt maxBL = gconv.toWGS84();
			gconv.setXY(env.getMinY()/1000.0 , env.getMinX()/1000.0 , -xySys , datum);
			LatLonAlt minBL = gconv.toWGS84();
			
			bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
			
		} else {
			// XY�n�ł͂Ȃ��Ƃ�
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(env.getMaxY() , env.getMaxX() , datum);
				LatLonAlt maxBL = gconv.toWGS84();
				gconv.setLatLon(env.getMinY() , env.getMinX() , datum);
				LatLonAlt minBL = gconv.toWGS84();
				bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
			} else {
				bounds = env;
			}
		}
		return ( bounds );
	}
	
	
	// WGS84���W�n�ɑ΂���SVG���W�ł�BBOX�Z�o
	synchronized public Envelope getSvgBoundsW( Envelope wgsEnv  ){
		// SVG �̃r���[�{�b�N�X�����߂Ă���B 
		double x , y;
		g2s.calcTransform( wgsEnv.getMinX() , wgsEnv.getMinY() );
		x = g2s.x;
		y = g2s.y;
		g2s.calcTransform( wgsEnv.getMaxX() , wgsEnv.getMaxY() );
		
		return ( new Envelope ( x , g2s.x , y , g2s.y ) );
	}
	
	// ���W�Q�ƌn�̈Ⴂ���������ASVG���W�ł�BBOX�Z�o 2007.10.23
	synchronized public Envelope getSvgBounds( Envelope env){
		double x , y;
		Envelope wgsEnv = getWgsBounds( env );
		return ( getSvgBoundsW( wgsEnv ) );
	}

}