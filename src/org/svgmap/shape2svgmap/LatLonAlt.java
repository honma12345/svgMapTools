package org.svgmap.shape2svgmap;

public class LatLonAlt{
	// �n�����W�̃N���X
	// Ver.1.0:  2006.6.8 bu Satoru Takagi
	public double latitude;  // �ܓx[deg] ���W�A���ɂ��ׂ����H �����E�E
	public double longitude; // �o�x[deg]
	public double altitude; // ���x(�W�I�C�h���炵��)[m]
	
	
	LatLonAlt( ){
	}
	
	LatLonAlt( double lat , double lon , double alt ){
		latitude = lat;
		longitude = lon;
		altitude = alt;
	}
	
	public String toString(){
		return ( "lat:" + latitude + " lon:" + longitude + " alt:" + altitude );
	}
	
}
		