package org.svgmap.shape2svgmap;

import java.util.*;
import java.io.*;
// ���{�̍��y�Ȃ̂��ǂ�����񎟃��b�V���f�[�^�x�[�X�����ɒ��ׂ�
// 2012/05/10 Satoru Takagi
//
public class jpLandTester{
	public jpLandTester(){
		getMesh2();
	}
	
	public HashSet<String> mesh2;
	private void getMesh2( ){
		mesh2 = new HashSet<String>();
		try{
			FileReader f = new FileReader("mesh2.txt");
			BufferedReader b = new BufferedReader(f);
			String s;
			int l1MeshCount = 0;
			while((s = b.readLine())!=null){
				mesh2.add(s);
			}
		}catch(Exception e){
			System.out.println("�t�@�C���ǂݍ��ݎ��s");
		}
	}
	
	// �w�肵�����W��(�l�p�`)��2�����b�V���ɓ����Ă���(�����ł�����Ă���)���ǂ������m�F����
	// hasMap(�ő�ܓx�A�ŏ��ܓx�A�ő�o�x�A�ŏ��o�x)
	// hasMap( bb.y , bb.y - bb.height , bb.x + bb.width , bb.x )
	//
	public boolean hasMap( double latMax , double latMin , double lngMax , double lngMin ){
//		System.out.println("lat:" + latMax +"," + latMin + "lng:"+ lngMax + "," + lngMin);
		boolean ans = false;
		JpMesh jmMin = new  JpMesh( latMin , lngMin );
//		JpMesh jmMax = new  JpMesh( latMax , lngMax );
		
		double MinM2lat = ((double)jmMin.m1u + (double)jmMin.m2u / 8.0 ) / 1.5;
		double MinM2lng = 100 + (double)jmMin.m1d + (double)jmMin.m2d / 8.0;
		
//		System.out.println("loop- lat:" + MinM2lat +"," + latMax + "lng:"+  MinM2lng + "," + lngMax);
		
		loop1: for ( double lat = MinM2lat + 0.00001 ; lat < latMax ; lat += JpMesh.m2lat ){
			for ( double lng =  MinM2lng + 0.00001 ; lng < lngMax ; lng += JpMesh.m2long ){
				JpMesh jm = new JpMesh(lat,lng);
//				System.out.println(lat + "," + lng + ":" + jm.mesh1 + jm.mesh2);
				if ( mesh2.contains(String.valueOf(jm.toIntMesh2Code())) ){ // debug 2017.11.17 : jpMesh��mesh1,2��������Ȃ��Ȃ��Ă��E�E
					ans = true;
					break loop1;
				}
			}
		}
//		System.out.println(ans);
		return ( ans );
	}
}