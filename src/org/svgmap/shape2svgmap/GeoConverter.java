package org.svgmap.shape2svgmap;

// <PRE>
// �n����̈ʒu�̃N���X
// ���W�n�̕ϊ��@�\������
// 1997/01/08 by Satoru Takagi
// 2006/06/08 �����g���₷����������E�E
// 2007/05/17 �l�̃Z�b�^�[����ꂽ new���Ȃ��Ă����x�ł��g����悤�ɂ���
// 2007/05/17 Molodensky�@�����ꂽ�i����������̕��������j tiny=true�ŋN��
// ��         XY to BL�@�\��ǉ�
// 2009/02/06 �n���@��TKY2JGD�@�����ꂽ�i���ꂪ��Ԑ��m�Ȃ͂��j

import java.awt.geom.*;

public class GeoConverter {

    public LatLonAlt WGPos; // �����I�ȍ��W��WGS84�Ŏ����Ă���B
	private boolean tiny = false; // ���W�n�ϊ����ȈՂɍs���ꍇ�́A�����true�ɂ��Ă���
	private boolean useTky2jgd = false; // TKY2JGD���g���ꍇ�͂����true�ɂ���
    
    public final static int WGS84=1;
	public final static int JGD2000 = 1;
    public final static int TOKYO_BESSEL=2;
    public final static int BESSEL=2;
    public final static double dtorad = Math.PI / 180.0;
    public final static double radtod = 1 / dtorad;
	
	
    public final static int Normal = 0;
    public final static int Molodensky = 1;
    public final static int Tky2JGD = 2;
    
	
	public static void main(String[] args) {
		int mode = Normal;
		double lat = 39;
		double lon = 140;
		if ( args.length == 3 ){
			mode = Integer.parseInt(args[2]);
		}
		if ( args.length >= 2 ){
			lat = Double.parseDouble(args[0]);
			lon = Double.parseDouble(args[1]);
		}
		GeoConverter t2j = new GeoConverter( mode );
		t2j.setLatLon( lat , lon , GeoConverter.TOKYO_BESSEL );
		System.out.println ("wgs lat:" + (t2j.WGPos).latitude + " lon:" + (t2j.WGPos).longitude );
	}
	
	
	GeoConverter (){
	}
	
	tky2jgd t2j;
	GeoConverter ( int method ){
		if ( method == Normal ){
			tiny = false;
			useTky2jgd = false;
		} else if ( method == Molodensky ){
			tiny = true;
			useTky2jgd = false;
		} else if ( method == Tky2JGD ){
			tiny = false;
			useTky2jgd = true;
			t2j = new tky2jgd();
		}
	}
	
    GeoConverter ( LatLonAlt ixy , int crs ){
       setLatLonAlt (  ixy ,  crs );
    }
	
	// ���̊֐����A�S�Ă̈ܓx�o�x�ϊ��V�X�e���̃n�u�ɂȂ��Ă���
	public void setLatLonAlt ( LatLonAlt ixy , int crs ){
       if (crs == WGS84) {
            WGPos = ixy;
        } else if (crs == TOKYO_BESSEL ) {
            WGPos = BLtoWGS( ixy );
        } else {
            WGPos = new LatLonAlt( 0.0 , 0.0 , 0.0 );
        }
    }

    GeoConverter ( double lat, double lon , int crs ){
        setLatLon ( lat, lon , crs );
    }
	
	public void setLatLon ( double lat, double lon , int crs ){
        LatLonAlt ixy;
        ixy = new LatLonAlt(lat , lon , 0.0 );
    	setLatLonAlt (  ixy ,  crs );
	}
	
	
    GeoConverter ( double lat, double lon , double alt, int crs ){
        setLatLonAlt( lat, lon , alt, crs );
    }
	
	public void setLatLonAlt( double lat, double lon , double alt, int crs ){
        LatLonAlt ixy;
        ixy = new LatLonAlt(lat , lon , alt );
    	setLatLonAlt (  ixy ,  crs );
	}
	
	GeoConverter ( double x, double y , int kei , int crs ){
		setXY( x, y , kei , crs );
	}
	
	// ���̊֐����AXY���W�n����ܓx�o�x���W�n�ւ̕ϊ��̃n�u
	public void setXY( double x, double y , int kei , int crs ){
		// XY���W�n����R���o�[�g����
		LatLonAlt ixy;
		ixy =  xyToBl( x ,  y , kei , crs );
		setLatLonAlt (  ixy ,  crs );
	}
	
	public LatLonAlt By( int mode ){
        LatLonAlt Pos;
        if ( mode == WGS84 ) {
           Pos =  WGPos;
        } else if ( mode == TOKYO_BESSEL ){
           Pos =  WGStoBL( WGPos );
        } else {
           Pos =  new LatLonAlt( 0.0 , 0.0 , 0.0 );
        }
        return ( Pos );
    }

    public LatLonAlt toBESSEL(){
        return ( WGStoBL( WGPos ) );
    }

    public LatLonAlt toWGS84(){
        return (  WGPos  );
    }
    

    // WGS84<->BESSEL �ϊ��̂��߂̒萔�Q
	// a:���a f:�G���� D*:���W���� [m]
    protected static final double a_WGS = 6378137.0 , f_WGS = 1 / 298.257222101;
//    protected static final double a_WGS = 6378137.0 , f_WGS = 1 / 298.257;
    protected static final double a_BL = 6377397.155 , f_BL = 1 / 299.152813;
    protected static final double DU_WB = 146.3 , DV_WB = -507.1 , DW_WB = -681.0;
    protected static final double DU_BW = -DU_WB , DV_BW = -DV_WB , DW_BW = -DW_WB;

    public LatLonAlt WGStoBL( LatLonAlt WGS ) {
    	if ( tiny ){
	        return( MolodenskyConv( WGS , a_WGS , f_WGS ,
	                           a_BL , f_BL , DU_WB , DV_WB , DW_WB ) );
    	} else {
	        return( GeoSysConv( WGS , a_WGS , f_WGS ,
	                           a_BL , f_BL , DU_WB , DV_WB , DW_WB ) );
    	}
    }

    public LatLonAlt BLtoWGS( LatLonAlt BL ) {
    	if ( tiny ){
	        return( MolodenskyConv( BL , a_BL , f_BL ,
	                         a_WGS , f_WGS , DU_BW , DV_BW , DW_BW ) );
    	} else if ( useTky2jgd){
    		Point2D.Double wg = t2j.transform( BL.latitude , BL.longitude );
    		return ( new LatLonAlt( wg.y , wg.x , 0.0 ) );
    	} else {
	        return( GeoSysConv( BL , a_BL , f_BL ,
	                         a_WGS , f_WGS , DU_BW , DV_BW , DW_BW ) );
    	}
    }
	
	public LatLonAlt sysConv( LatLonAlt pos , int from , int to ){
		// from �ɓ��͂����l�̍��W�n
		// to �ɏo�͂���l�̍��W�n
		// pos �ɍ��W�l����
		LatLonAlt ret;
		if ( from == to ){
			ret = pos;
		} else if ( from == TOKYO_BESSEL ){
			ret = BLtoWGS( pos );
		} else {
			ret = WGStoBL( pos );
		}
		return (ret);
	}

    // �C�ӂ̉�]�ȉ~�̂P����ʂ̔C�ӂ̉�]�ȉ~�̂Q�ւ̕ϊ�    
    // 1996/04/10 by Satoru Takagi   (takagi@lab.kdd.co.jp)
    // �ܓx(Lat)�E�o�x(Lon)�̓��W�A���Aa*,DU,DV,DW�̓��[�g��
    //
    // �Q�l�����F�C��ۈ����@���H���ϑ��񍐉q�����n�ҁ@��ꍆ S.63/03 P.76�`

    public LatLonAlt GeoSysConv( LatLonAlt POS1 ,
                                 double a1 , double f1 ,
                                 double a2 , double f2 ,
                                 double DU , double DV , double DW ){
        double h , Lat , Lon ;
        double a , ee , N , nn ;
        double U , V , W;
        double R , S , alpha , T , m , m_old;
        
        //�ܓx�E�o�x�E���x�̗p��
        Lat = POS1.latitude * dtorad;
        Lon = POS1.longitude * dtorad;
        h = POS1.altitude;		// ���x�̈����ɒ���

/*
		System.out.println( "" );
		System.out.println( "Input" );
		System.out.println( Lat * 180.0 / Math.PI );
		System.out.println( Lon * 180.0 / Math.PI );
		System.out.println( h );
		System.out.println( "CALC START" );
*/
    
      if ( (Lat < Math.PI/2.0 && Lat > -Math.PI/2.0) ){
        // ���n�p�̕⏕�ϐ��̗p��
        ee = f1 * ( 2.0 - f1 );
        N = a1 / Math.sqrt( 1.0 - ee * Math.sin(Lat) * Math.sin(Lat) );
        nn = N * ( 1.0 - ee );

        // ���n�ł̒������W���v�Z
        U = (N + h) * Math.cos(Lat) * Math.cos(Lon);
        V = (N + h) * Math.cos(Lat) * Math.sin(Lon);
        W = (nn + h) * Math.sin(Lat);


        // ���n�ł̒������W��p��
        U += DU;
        V += DV;
        W += DW;
    
        // ���n�p�̕⏕�ϐ��̗p��
        ee = f2 * ( 2.0 - f2 );
        a = a2;

        // �������W����ȉ~�̍��W�ւ̕ϊ�
        Lon = Math.atan( V / U );
        R = Math.sqrt( U * U + V * V);

        // �����v�Z�i���������Q�`�R��~�܂�j
        m = 0.0;
        alpha = 0.0;
        do {
            m_old = m;
            S = W - ee * m * Math.sin( alpha );

            alpha = Math.atan( S / ( R * ( 1.0 - ee ) ) );
            T = a / Math.sqrt( 1.0 - ee * Math.sin( alpha ) * Math.sin( alpha ) ) ;
            m = ( R / Math.cos( alpha ) ) - T;
//			System.out.println( m - m_old );

        } while ( Math.abs( m - m_old ) > 0.001 ) ;
    
        Lat = alpha * radtod;
        h = m;

        if( U < 0 ){            // ABS(�o�x)��90�x�ȏ�̎���atan�Ő܂�Ԃ���������
            Lon += Math.PI;
            
            if( Lon > Math.PI ){     // �o�x��180�x�ȏ�̎���-2PI�ŕ��ɂ���
                Lon -= Math.PI + Math.PI ;
            }
        }
        Lon = Lon * radtod;
      	
//        height = h;

/*
		System.out.println( "Output" );
		System.out.println( Lat * 180.0 / Math.PI );
		System.out.println( Lon * 180.0 / Math.PI );
		System.out.println( h );
*/
      }
    
        return( new LatLonAlt( Lat , Lon , h ) );
    }
	
	
	// Molodensky�@�ɂ��A�ȉ~�̂�p�����ȈՍ��W�n�ϊ�
    public LatLonAlt MolodenskyConv( LatLonAlt POS1 , 
    	double a1 , double f1 ,double a2 , double f2 ,
    	double DU , double DV , double DW ){
    	double from_esq , da , df , slat , slon , clat , clon , ssqlat , adb , rn , rm , dlat , dlon , dh;
    	dlat = 0.0 ;
    	dlon = 0.0 ;
    	dh = 0.0;
		//�ܓx�E�o�x�E���x�̗p��
		double Lat = POS1.latitude * dtorad;
		double Lon = POS1.longitude * dtorad;
		double h = POS1.altitude;		// ���x�̈����ɒ���
		if ( (Lat < Math.PI/2.0 && Lat > -Math.PI/2.0) ){
			from_esq = 2.0 * f1 - f1 * f1;
			da = a2 - a1;
			df = f2 - f1;
			slat = Math.sin(Lat);
			clat = Math.cos(Lat);
			slon = Math.sin(Lon);
			clon = Math.cos(Lon);
			ssqlat = slat * slat;
			adb = 1.0 / ( 1.0 - f1 );
			rn = 1.0 / Math.sqrt( 1.0 - from_esq * ssqlat);
			rm = a1 * ( 1.0 - from_esq) / (Math.pow((1.0 - from_esq * ssqlat) , (3.0 / 2.0)));
			rn = a1 * rn;
			
			dlat = (((((-DU * slat * clon - DV * slat * slon) + DW * clat) + (da * ((rn * from_esq * slat * clat) / a1))) + (df * (rm * adb + rn / adb) * slat * clat))) / (rm + h);
			dlon = (-DU * slon + DV * clon) / ((rn + h) * clat);
			dh = (DU * clat * clon) + (DV * clat * slon) + (DW * slat) - (da * (a1 / rn)) + ((df * rn * ssqlat) / adb);
		}
		return ( new LatLonAlt( (Lat + dlat)*radtod , (Lon + dlon)*radtod , h + dh ));
    }
	
                                 	
	
	private final static double[] Phi =
	{ 0 , 
		33.0 * dtorad,
		33.0 * dtorad,
		36.0 * dtorad,
		33.0 * dtorad,
		36.0 * dtorad,
		36.0 * dtorad,
		36.0 * dtorad,
		36.0 * dtorad,
		36.0 * dtorad,
		40.0 * dtorad,
		44.0 * dtorad,
		44.0 * dtorad,
		44.0 * dtorad,
		26.0 * dtorad,
		26.0 * dtorad,
		26.0 * dtorad,
		26.0 * dtorad,
		20.0 * dtorad,
		26.0 * dtorad};
	
	private final static double[] Lambda =
	{ 0 ,
		129.5 * dtorad,
		131.0 * dtorad,
		(132.0 + 10.0 / 60.0) * dtorad,
		133.5 * dtorad,
		(134.0 + 20.0 / 60.0) * dtorad,
		136.0 * dtorad,
		(137.0 + 10.0 / 60.0) * dtorad,
		138.5 * dtorad,
		(139.0 + 50.0 / 60.0) * dtorad,
		(140.0 + 50.0 / 60.0) * dtorad,
		(140.0 + 15.0 / 60.0) * dtorad,
		(142.0 + 15.0 / 60.0) * dtorad,
		(144.0 + 15.0 / 60.0) * dtorad,
		142.0 * dtorad,
		127.5 * dtorad,
		124.0 * dtorad,
		131.0 * dtorad,
		136.0 * dtorad,
		154.0 * dtorad};
		
	
	public LatLonAlt xyToBl ( double x , double y , int kei , int crs ){
		// x,y:XY���W�n�̒l
		// kei:���{���n�n�@�S19�n�̌��_�ԍ�
		// crs: WGS84(JGD2000) or TOKYO_BESSEL
		// See http://vldb.gsi.go.jp/sokuchi/surveycalc/algorithm/xy2bl/xy2bl.htm
		double fnA , fnB , fnC , fnD ;
		double Phi0 , Lambda0 , Phi1 , Eta1_2 , t1 , N1 , m0;
		double s_a , s_f , s_e , s_e_;
		double s_M;
		LatLonAlt ans = new LatLonAlt();
		int i;

		if ( crs == TOKYO_BESSEL ){
			s_a = a_BL;
			s_f = f_BL;
		} else {
			s_a = a_WGS;
			s_f = f_WGS;
		}
		s_e = Math.sqrt(2 * s_f - s_f * s_f);
		s_e_ = Math.sqrt(2 * (1 / s_f) - 1) / ((1 / s_f) - 1);
		
		
		Phi0 = Phi[kei];
		Lambda0 = Lambda[kei];
		m0 = 0.9999;
		s_M = getMeridianArcLength(s_a, s_e, Phi0) + x / m0;
		Phi1 = getPhiFromMeridianArcLength(5, s_a, s_e, s_M, Phi0);
		N1 = getSpheroidN(Phi1, s_a, s_e);
		Eta1_2 = (Math.pow(s_e_ ,2)) * (Math.pow(Math.cos(Phi1) , 2));
// System.out.println( "s_M:" + s_M + " Phi1:" + Phi1 + " N1:" + N1 + " Eta1_2:" + Eta1_2 );
		t1 = Math.tan(Phi1);
		fnA = Phi1 - (1.0 / 2.0) * (1.0 / (Math.pow(N1 , 2))) * t1 * (1.0 + Eta1_2) * (Math.pow((y / m0) , 2));
		fnB = (1.0 / 24.0) * (1.0 / (Math.pow(N1 , 4))) * t1 * (5.0 + 3.0 * (Math.pow(t1 , 2)) + 6.0 * Eta1_2 - 6.0 * (Math.pow(t1 , 2)) * Eta1_2 - 3.0 * (Math.pow(Eta1_2 , 2)) - 9.0 * (Math.pow(t1 , 2)) * (Math.pow(Eta1_2 , 2))) * (Math.pow((y / m0) , 4));
		fnC = -(1.0 / 720.0) * (1.0 / (Math.pow(N1 , 6))) * t1 * (61.0 + 90.0 * (Math.pow(t1 , 2)) + 45.0 * (Math.pow(t1 , 4)) + 107.0 * Eta1_2 - 162.0 * (Math.pow(t1 , 2)) * Eta1_2 - 45.0 * (Math.pow(t1 , 4)) * Eta1_2) * (Math.pow((y / m0) , 6));
		fnD = (1.0 / 40320.0) * (1.0 / (Math.pow(N1 , 8))) * t1 * (1385.0 + 3633.0 * (Math.pow(t1 , 2)) + 4095.0 * (Math.pow(t1 , 4)) + 1575.0 * (Math.pow(t1 , 6))) * (Math.pow((y / m0) , 8));
		ans.latitude = fnA + fnB + fnC + fnD;
		fnA = 1.0 / (N1 * Math.cos(Phi1)) * (y / m0);
		fnB = -(1.0 / 6.0) * (1.0 / ((Math.pow(N1 , 3)) * Math.cos(Phi1))) * (1.0 + 2.0 * (Math.pow(t1 , 2)) + Eta1_2) * (Math.pow((y / m0) , 3));
		fnC = (1.0 / 120.0) * (1.0 / ((Math.pow(N1 , 5)) * Math.cos(Phi1))) * (5.0 + 28.0 * (Math.pow(t1 , 2)) + 24.0 * (Math.pow(t1 , 4)) + 6.0 * Eta1_2 + 8.0 * (Math.pow(t1 , 2)) * Eta1_2) * (Math.pow((y / m0) , 5));
		fnD = -(1.0 / 5040.0) * (1.0 / ((Math.pow(N1 , 7)) * Math.cos(Phi1))) * (61.0 + 662.0 * (Math.pow(t1 , 2)) + 1320.0 * (Math.pow(t1 , 4)) + 720.0 * (Math.pow(t1 , 6))) * (Math.pow((y / m0) , 7));
		ans.longitude = Lambda0 + fnA + fnB + fnC + fnD;
		
		ans.altitude = 0;
		ans.latitude = ans.latitude * radtod;
		ans.longitude = ans.longitude * radtod;
		
		return (ans);
	}
	
	private double getMeridianArcLength( double a , double e , double Phi ){
		double[] b = new double[10];
		double[] a1 = new double[10];
		double s;
// System.out.println( "a:" + a + " e:" + e + " Phi:" + Phi );
	
		a1[1] = 1.0 + 3.0 / 4.0 * Math.pow(e , 2) + 45.0 / 64.0 * Math.pow(e , 4) + 175.0 / 256.0 * Math.pow(e , 6) + 11025.0 / 16384.0 * Math.pow(e , 8) + 43659.0 / 65536.0 * Math.pow(e , 10) + 693693.0 / 1048576.0 * Math.pow(e , 12) + 19324305.0 / 29360128.0 * Math.pow(e , 14) + 4927697775.0 / 7516192768.0 * Math.pow(e , 16);
		a1[2] = 3.0 / 4.0 * Math.pow(e , 2) + 15.0 / 16.0 * Math.pow(e , 4) + 525.0 / 512.0 * Math.pow(e , 6) + 2205.0 / 2048.0 * Math.pow(e , 8) + 72765.0 / 65536.0 * Math.pow(e , 10) + 297297.0 / 262144.0 * Math.pow(e , 12) + 135270135.0 / 117440512.0 * Math.pow(e , 14) + 547521975.0 / 469762048.0 * Math.pow(e , 16);
		a1[3] = 15.0 / 64.0 * Math.pow(e , 4) + 105.0 / 256.0 * Math.pow(e , 6) + 2205.0 / 4096.0 * Math.pow(e , 8) + 10395.0 / 16384.0 * Math.pow(e , 10) + 1486485.0 / 2097152.0 * Math.pow(e , 12) + 45090045.0 / 58720256.0 * Math.pow(e , 14) + 766530765.0 / 939524096.0 * Math.pow(e , 16);
		a1[4] = 35.0 / 512.0 * Math.pow(e , 6) + 315.0 / 2048.0 * Math.pow(e , 8) + 31185.0 / 131072.0 * Math.pow(e , 10) + 165165.0 / 524288.0 * Math.pow(e , 12) + 45090045.0 / 117440512.0 * Math.pow(e , 14) + 209053845.0 / 469762048.0 * Math.pow(e , 16);
		a1[5] = 315.0 / 16384.0 * Math.pow(e , 8) + 3465.0 / 65536.0 * Math.pow(e , 10) + 99099.0 / 1048576.0 * Math.pow(e , 12) + 4099095.0 / 29360128.0 * Math.pow(e , 14) + 348423075.0 / 1879048192.0 * Math.pow(e , 16);
		a1[6] = 693.0 / 131072.0 * Math.pow(e , 10) + 9009.0 / 524288.0 * Math.pow(e , 12) + 4099095.0 / 117440512.0 * Math.pow(e , 14) + 26801775.0 / 469762048.0 * Math.pow(e , 16);
		a1[7] = 3003.0 / 2097152.0 * Math.pow(e , 12) + 315315.0 / 58720256.0 * Math.pow(e , 14) + 11486475.0 / 939524096.0 * Math.pow(e , 16);
		a1[8] = 45045.0 / 117440512.0 * Math.pow(e , 14) + 765765.0 / 469762048.0 * Math.pow(e , 16);
		a1[9] = 765765.0 / 7516192768.0 * Math.pow(e , 16);

		b[1] = a * (1.0 - e * e) * (a1[1] / 1.0);
		b[2] = a * (1.0 - e * e) * (-a1[2] / 2.0);
		b[3] = a * (1.0 - e * e) * (a1[3] / 4.0);
		b[4] = a * (1.0 - e * e) * (-a1[4] / 6.0);
		b[5] = a * (1.0 - e * e) * (a1[5] / 8.0);
		b[6] = a * (1.0 - e * e) * (-a1[6] / 10.0);
		b[7] = a * (1.0 - e * e) * (a1[7] / 12.0);
		b[8] = a * (1.0 - e * e) * (-a1[8] / 14.0);
		b[9] = a * (1.0 - e * e) * (a1[8] / 16.0);
// System.out.println( "a1:" + a1[1] + " " + a1[2] + " " + a1[3] + " " + a1[4] + " " + a1[5] + " " + a1[6] + " " + a1[7] + " " + a1[8] + " " + a1[9] ); 
// System.out.println( "b :" + b[1] + " " + b[2] + " " + b[3] + " " + b[4] + " " + b[5] + " " + b[6] + " " + b[7] + " " + b[8] + " " + b[9] ); 

		s = b[1] * Phi + b[2] * Math.sin(2 * Phi) + b[3] * Math.sin(4 * Phi) + b[4] * Math.sin(6 * Phi) + b[5] * Math.sin(8 * Phi) + b[6] * Math.sin(10 * Phi) + b[7] * Math.sin(12 * Phi) + b[8] * Math.sin(14 * Phi) + b[9] * Math.sin(16 * Phi);
		
		return (s);
	}
	
	private double getPhiFromMeridianArcLength( int n , double a , double e , double M , double Phi0 ){
		double s , Phi;
		Phi = Phi0;
		for ( int i = 0 ; i < n ; i++ ){
			s = getMeridianArcLength(a, e, Phi);
			Phi = Phi + (2.0 * (s - M) * Math.pow((1.0 - e * e * (Math.sin(Phi)*Math.sin(Phi))) , (3.0 / 2.0))) / (3.0 * e * e * (s - M) * Math.sin(Phi) * Math.cos(Phi) * Math.sqrt(1.0 - e * e * (Math.sin(Phi)*Math.sin(Phi)) ) - 2.0 * a * (1.0 - e * e));
		}
		return Phi;
	}

	private double getSpheroidN(double Phi , double  a , double e ){
		double W;
		W = getSpheroidW(Phi, e);
		return (  a / W );
	}

	private double  getSpheroidW(double Phi , double e ){
		return ( Math.sqrt(1 - (e * e ) * ((Math.sin(Phi)) * (Math.sin(Phi)))) );
	}

}
// </PRE>
