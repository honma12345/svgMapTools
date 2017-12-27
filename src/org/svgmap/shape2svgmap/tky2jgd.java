package org.svgmap.shape2svgmap;

import java.io.*;
import java.util.*;
import java.awt.geom.*;

// ���y�n���@��tky2jgd�̃p�����[�^�t�@�C����p���āA
// �����n�n(�����x�b�Z��)����GRS80(JGD2000)�ܓx�o�x���W�n�ɕϊ�����֐�
// tky2jgdGen���K�v
// Copright 2009 by Satoru Takagi @ KDDI All RIghts Reserved
// ToDo: �f�[�^�x�[�X���O�̏ꍇ�A�e���b�V�����_�ɑ�������Y���ʂ���]�ȉ~�̂ɂ�
// ��ϊ����狁�߁A���I�Ƀn�b�V����ǉ�����@�\�i����Ȃ猋�\�ȒP���Ǝv���j
// �n�b�V������̌������ʂ�������L���b�V������@�\(�|�����C���Ȃǂ̘A���_�̕ϊ��ɗL��)

public class tky2jgd {
	public static void main(String[] args) {
		tky2jgd t2j = new tky2jgd();
		
		double lon = 140.0;
		double lat = 36.0;
		if ( args.length > 1 ){
			lat = Double.parseDouble(args[0]);
			lon = Double.parseDouble(args[1]);
		}
		Point2D.Double p = t2j.transform(lat,lon);
		
		
		System.out.println( "In:" + lon + "," +  lat + " Out:" + p );
		
	}
	
	HashMap<Integer , Point2D.Float> tky2jgdTable;
	
	Point2D.Float getOutMesh( double lat0 , double lng0 ){
		Point2D.Float ans = new Point2D.Float();
//			System.out.println("NULL");
			normalConverter.setLatLon( lat0 , lng0 , GeoConverter.TOKYO_BESSEL );
			ans.x = (float)(3600.0 * ( ( normalConverter.WGPos).longitude - lng0 ));
			ans.y = (float)(3600.0 * ( ( normalConverter.WGPos).latitude - lat0 ));
		return ( ans );
	}
	
	
	int prevMeshcode = -999; // ���O�̕ϊ��Ɠ������b�V����̕ϊ��̏ꍇ�ɂ̓f�[�^�x�[�X�������ȗ����邽��
	Point2D.Float crd0 , crdU , crdR , crdUR; // crd*�ɂ́Ax�Ɍo�x�Ay�Ɉܓx�������Ă���
	
	Point2D.Double transform( double lat , double lon){
		double lat0, lng0;
		JpMesh mesh = new JpMesh( lat, lon );
		Integer meshcode = Integer.valueOf(mesh.toIntMesh3Code());
//		System.out.println("mesh:" + meshcode);
		// �l���̂R�����b�V���̃Y���ʂ𓾂�
		// �f�[�^�x�[�X�ɑ��݂��Ȃ��ꍇ�́A�m�[�}���ȕϊ��֐��ŊY�����镔���̃Y���ʂ��v�Z����
		if ( prevMeshcode != meshcode ){
			crd0 = tky2jgdTable.get(meshcode);
			if ( crd0 == null ){
				crd0 = getOutMesh( mesh.getLatitude() , mesh.getLongitude() );
			}
			crdU = tky2jgdTable.get(Integer.valueOf(mesh.getUpMesh3()));
			if ( crdU == null ){
				crdU = getOutMesh( mesh.getLatitude() + mesh.m3lat , mesh.getLongitude() );
			}
			crdR = tky2jgdTable.get(Integer.valueOf(mesh.getRightMesh3()));
			if ( crdR == null ){
				crdR = getOutMesh( mesh.getLatitude() , mesh.getLongitude() + mesh.m3long );
			}
			crdUR = tky2jgdTable.get(Integer.valueOf(mesh.getUpRightMesh3()));
			if ( crdUR == null ){
				crdUR = getOutMesh( mesh.getLatitude() + mesh.m3lat , mesh.getLongitude() + mesh.m3long );
			}
		}
//		System.out.println(crd0);
		
		
		//�o�C���j�A���
		double a,b,x,y;
//		System.out.println("mesh3:" +  mesh.getLatitude() + "," + mesh.getLongitude() );
		a = (lon - mesh.getLongitude()) / (45.0 / 3600.0); // �o�x�����̕ψ�
		b = (lat - mesh.getLatitude()) / (30.0 / 3600.0); // �ܓx�����̕ψ�
//		System.out.println( "a(lon)" + a + " b(lat):" + b);
		
		//��Ԏ���W�J��������
		x = crd0.x + ( crdU.x - crd0.x ) * b + ( crdR.x - crd0.x ) * a + ( crdUR.x - crdR.x - crdU.x + crd0.x ) * b * a;
		y = crd0.y + ( crdU.y - crd0.y ) * b + ( crdR.y - crd0.y ) * a + ( crdUR.y - crdR.y - crdU.y + crd0.y ) * b * a;
		
		lon = x / 3600.0 + lon;
		lat = y / 3600.0 + lat;
		return ( new Point2D.Double(lon, lat));
	}
	
	Point2D.Double transform( Point2D.Double crd ){
		return ( this.transform( crd.y , crd.x ) );
	}
	
	GeoConverter normalConverter;
	boolean readFromObject = false;
	
	@SuppressWarnings("unchecked")
	tky2jgd(){
		if ( readFromObject ){
			try{
				System.out.println("Reading database");
				//��_�f�[�^�̃n�b�V���e�[�u������ǂݍ���
				FileInputStream inFile = new FileInputStream("TKY2JGD.jso");
				ObjectInputStream inObject = new ObjectInputStream( inFile );
				tky2jgdTable = (HashMap<Integer , Point2D.Float>)inObject.readObject();
				inObject.close();
				System.out.println("Init End");
				// tky2jgdTable��point2dFloat�́A�ܓx���Fx , �o�x��:y (�t)�Ȃ̂Œ��ӁI
				
			} catch (Exception e) {
				// File�I�u�W�F�N�g�������̗�O�ߑ�
				e.printStackTrace();
			}
		} else {
			readAndBuild();
		}
		normalConverter = new GeoConverter();
	}
	
	public void readAndBuild(){
		boolean startData = false;
		Integer meshCode;
		float x,y;
		int counter = 0;
//		Point2D.Float meshCrd;
		System.out.println("Reading database");
		
		try {
			File csv = new File("TKY2JGD.par"); // CSV�f�[�^�t�@�C��
			
			tky2jgdTable = new HashMap<Integer , Point2D.Float>();
			
			BufferedReader br = new BufferedReader(new FileReader(csv));
			// �ŏI�s�܂œǂݍ���
			while (br.ready()) {
				String line = br.readLine();
				// 1�s���f�[�^�̗v�f�ɕ���
				StringTokenizer st = new StringTokenizer(line, " ");
				
				if ( startData != true ){ // "MeshCode"�s������܂œǂݔ�΂�
					
					if ((st.nextToken().indexOf("MeshCode")) > -1 ){
						startData = true;
					}
				} else {
					meshCode = Integer.decode(st.nextToken());
					y = Float.parseFloat(st.nextToken()); // �ܓx�ψ�
					x = Float.parseFloat(st.nextToken()); // �o�x�ψ�
					tky2jgdTable.put(meshCode,new Point2D.Float(x,y));
				}
				++ counter;
				if ( counter % 10000 == 0 ){
					System.out.print(".");
				}
			}
			br.close();
			System.out.println("");
			System.out.println("END");
		} catch (FileNotFoundException e) {
			// File�I�u�W�F�N�g�������̗�O�ߑ�
			e.printStackTrace();
		} catch (IOException e) {
			// BufferedReader�I�u�W�F�N�g�̃N���[�Y���̗�O�ߑ�
			e.printStackTrace();
		}
	}
	
}