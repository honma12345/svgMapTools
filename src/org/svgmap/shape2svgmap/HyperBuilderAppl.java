package org.svgmap.shape2svgmap;

// HyperLayerBuilder: ������SVG Map�R���e���c���d�􂷂�R���e�i�𐶐����܂��B
// Copyright 2009-2010 by Satoru Takagi All Rights Reserved.
// 2009.09.08 �R���e�i���R���e���c�Ɠ����f�B���N�g���ɏo�́B�����N�𑊑΃p�X�ɁB
// 2009.09.09 �R���e�i��image��x,y,width,height�ɃC���|�[�gSVG��viewBox�����̂܂܂��Ă��������C���E�E�{���I�ȃo�O
// 2009.09.09 �R���e�i�̏o�͐���w��ł���悤�ɂ����B�R���e�i�̑��ݔ͈͂�rect�ŕ\���ł���悤�ɂ����B
// 2009.10.07 SVG Tiling & Layering�̐V����CRS��`�ɑΉ�
// 2012.06.05 �I�v�V�����Q�ǉ�(-opacity, -viewarea top)
// 2012.06.06 �I�v�V�����P�ǉ�(-global)
// 2016.11.25 �I�v�V�����R�ǉ�(-visible, -hidden, -rootattr)
//

import java.util.*;
import java.io.*;
// import javax.servlet.*;
// import javax.servlet.http.*;
import java.net.*;
import java.text.NumberFormat ;
import java.awt.geom.*;

// XML�̃N���X���C�u����
import org.w3c.dom.*;
// import org.apache.xerces.parsers.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
// XML�o�͗p�̃��C�u����
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class HyperBuilderAppl {
	
	boolean isSvgTL = true; // SVG 1.2 Tiling & Layering �ɏ��� ( <animation , visibleMax/MinZoom���g�p(�������͗��_���L)  )
	
	Document layer , topLayer;
	Transform layerG2S;
	
	String rootPath ="";
	
	double projCenter = 999; // �W���ܐ��̈ʒu 999:�ŏ��̃C���|�[�gSVG�̂܂�, -999:���S ���̑�:���̈ܓx
	double containerHeight = 0; // �R���e�iSVG��height 0:�ŏ��̃C���|�[�gSVG���p��
	
	Document container;
	Transform contG2S;
	String contCRSURI = "http://purl.org/crs/84";
	Rectangle2D.Double contGeoBBOX; // �R���e���c�̑��݃G���A ���̃V�X�e���ł͌o�xx,�ܓxy����
	
	
	static String containerName = "Container.svg";
	
	boolean debug = false;
	boolean appendMode = false;
	
	NumberFormat fmt = NumberFormat.getNumberInstance();
	
	Map<String, String> dataAttributes = new HashMap<String, String>(); // data-�J�X�^������ 2016.11.25
	
	String visibilityFile="";
	int visibilityFilter=0; // 0:none , 1:visible , -1:hidden
	
	public static void main(String args[]) throws Exception{
//		String input="san.svg";
		String input="test.svg";
		String output="";
		String vbString ="";
		
		if (args.length == 0 ){
			System.out.println("HyperBuilder: ������SVG Map�R���e���c���d�􂷂�R���e�i�𐶐����܂��B");
			System.out.println("Copyright 2009-2016 by Satoru Takagi @ KDDI All Rights Reserved.");
			System.out.println("----------");
			System.out.println("java HyperBuilder [Options] inputfile1 [Options] [... inputfileN [Options]] ");
			System.out.println("");
			System.out.println("inputfile�̏d�Ȃ菇:��ɏ��������̂���ɂȂ� ");
			System.out.println("");
			System.out.println("Options   : -optionName (value)");
			System.out.println("-o        : �R���e�i�̏o�͐���w��");
			System.out.println("            �p�X���w��");
			System.out.println("            �f�t�H���g:inputfile1�Ɠ����f�B���N�g����Container.svg�Ƃ��Đ���");
			System.out.println("-viewarea : �����\���G���A���w�肷��");
			System.out.println("            ISO6709��2�_: ��: +35.0+136.0/+35.5+136.5/");
			System.out.println("            1�_+���a[m] : ��: +35.25+136.25/1000");
			System.out.println("            \"top\"     : ��ԏヌ�C��(inputfil1e)�̃G���A�ɍ��킹��");
			System.out.println("            �f�t�H���g:�S�Ẵf�[�^���܂���G���A");
			System.out.println("-proj     : �����~���}�@�̕W���ܐ����w�肷��");
			System.out.println("            center: �G���A�̒��S"); 
			System.out.println("            �ܓx: ��: 34.56");
			System.out.println("            �f�t�H���g:�ŏ��Ɏw�肳�ꂽ�C���|�[�gSVG���p��");
			System.out.println("-height   : �R���e�iSVG��height���w�肷��");
			System.out.println("            �f�t�H���g:�ŏ��Ɏw�肳�ꂽ�C���|�[�gSVG���p��");
			System.out.println("-range    : ���O��inputfile�ɑ΂��\�������W���w�肷��");
			System.out.println("            (100�s�N�Z��������̃��[�g���Ŏw�� ��100pix=��ʏ�ŊT3cm)");
			System.out.println("            min[m/100pix]-max[m/100pix] ��: 10-1000 ");
			System.out.println("            �f�t�H���g:�펞�\��");
			System.out.println("-opacity  : ���O��inputfile�ɑ΂�opacity���w�肷��");
			System.out.println("            �f�t�H���g:�Ȃ�(1�̈Ӗ�)");
			System.out.println("-global   : ���O��inputfile�ɑ΂�x,y,width,height��S���K��(���������[�h)�Ɏw��");
			System.out.println("            �f�t�H���g:���L�̂Ƃ���");
			System.out.println("-append   : �ŏ��Ɏw�肵���R���e���c���̂ɑ��̃R���e���c�̃����N��ǉ�����");
			System.out.println("            �`�ŃR���e�i�𐶐�");
			System.out.println("-rootattr : ���������R���e�i�̃h�L�������g���[�gSVG�v�f�ɔC�ӂ�data-�J�X�^��������}��");
			System.out.println("            name1=value1,name2=value2 ==> data-name1=\"value1\" data-name2=\"value2\"");
			System.out.println("-visible  : �w�肵�������݂̂�visibility=visible�ɂ��� (-hidden�Ɣr��)");
			System.out.println("            ���̓t�@�C���p�X�ɑ΂��镔����v (*partOfInputfile*)");
			System.out.println("-hidden   : �w�肵�������݂̂�visibility=hidden�ɂ���  (-visible�Ɣr��)");
			System.out.println("            ���̓t�@�C���p�X�ɑ΂��镔����v (*partOfInputfile*)");
			System.out.println("-showtile : �f�o�b�O�E�m�F�p(�^�C�����E�����o�͂���)");
			System.out.println("            [�l����]");
			
			System.out.println("���L�F�S�Ă�inputfile�ɂ͒n�����W���^�f�[�^�ƁA���炩��");
			System.out.println("      �͈͑���(go:dataArea(�ŗD��)||go:boundingBox(��)||viewBox||width,height)���K�v�ł��B");
			System.out.println("      ���͈̔͑��������ɁA�R���e�i��image�v�f��x,y,width,height��ݒ肵�܂��B");
			System.out.println("      �R���e�i�ɂ͑S�Ă�inputfile�͈̔͂��܂���go:BoudingBox���ݒ肳��܂��B");
			System.exit(0);
		}
		
		if (args.length > 0){
			input = args[0];
		}
		
		HyperBuilderAppl hba = new HyperBuilderAppl();
		
		ArrayList<String> layerPathList  = new ArrayList<String>();
		ArrayList<String> ZoomRangeList  = new ArrayList<String>();
		ArrayList<String> OpacityList    = new ArrayList<String>(); //��X�g�����邩������Ȃ��̂ŕ�����ɂ��Ă���
		ArrayList<String> AreaOptionList = new ArrayList<String>(); //��X�g�����邩������Ȃ��̂ŕ�����ɂ��Ă���
		
		boolean followTopViewArea = false;
		
		// �R�}���h�I�v�V�����̎擾
		for ( int k = 0 ; k < args.length ; k++){
			if ( args[k].equals("-showtile") ){
				hba.debug = true;
			} else if ( args[k].equals("-o") ){
				++k;
				hba.rootPath = (new File (args[k])).getAbsolutePath();
				if ( ! (hba.rootPath).endsWith(".svg") ){
					hba.rootPath = hba.rootPath + File.separator + hba.containerName;
				}
			} else if ( (args[k].toLowerCase()).equals("-viewarea") ){
				++k;
				if  ( (args[k].toLowerCase()).equals("top") ){
					followTopViewArea = true;
				} else {
					vbString = args[k];
				}
			} else if ( (args[k].toLowerCase()).equals("-proj") ){
				++k;
				if ( (args[k].toLowerCase()).equals("center")){
					hba.projCenter = -999;
				} else {
					hba.projCenter = Double.parseDouble(args[k]);
				}
			} else if ( (args[k].toLowerCase()).equals("-append") ){
				hba.appendMode = true;
			} else if ( (args[k].toLowerCase()).equals("-rootattr") ){
				++k;
				String[] kvs = args[k].split(",");
				for ( int l = 0 ; l < kvs.length ; l++ ){
					String[]kv = kvs[l].split("=");
					hba.dataAttributes.put(kv[0],kv[1]);
				}
			} else if ( (args[k].toLowerCase()).equals("-height") ){
				++k;
				hba.containerHeight = Double.parseDouble(args[k]);
			} else if ( (args[k].toLowerCase()).equals("-visible") ){
				++k;
				if ( hba.visibilityFile ==""){
					hba.visibilityFile = (args[k]);
					hba.visibilityFilter = 1;
				}
			} else if ( (args[k].toLowerCase()).equals("-hidden") ){
				++k;
				if ( hba.visibilityFile ==""){
					hba.visibilityFile = (args[k]);
					hba.visibilityFilter = -1;
				}
			} else if ( (args[k].toLowerCase()).equals("-range") ){
				++k;
//				System.out.println("cap: path" + layerPathList.size() + " zrl:" + ZoomRangeList.size());
				ZoomRangeList.set(layerPathList.size() -1 , args[k]);
			} else if ( (args[k].toLowerCase()).equals("-opacity") ){
				++k;
				OpacityList.set(layerPathList.size() -1 , args[k]);
			} else if ( (args[k].toLowerCase()).equals("-global") ){
				AreaOptionList.set(layerPathList.size() -1 , "global");
			} else {
				layerPathList.add(args[k]);
				ZoomRangeList.add(null); // �J����zoomRange��ǉ�
				OpacityList.add(null); // �J����opacity��ǉ�
				AreaOptionList.add(null); // �J����areaOpt��ǉ�
			}
		}
		
		
		// viewBox���I�v�V�������瓾��
		Rectangle2D.Double vb = null;
		if ( vbString.length() > 0 ){
			vb = hba.getGeoBBox( vbString );
			System.out.println( "Specified geoViewBox:" + hba.getRectStr(vb));
		}
		
		
		
		// �C���|�[�g���C���[�̃f�[�^���擾���A�R���e�i�쐬�̏���������
		
		ArrayList<Rectangle2D.Double> ImageGeoBBox = new ArrayList<Rectangle2D.Double>();
		ArrayList<String> ImagePath = new ArrayList<String>();
		
		
		hba.contG2S = new Transform( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 );
		
		System.out.println("");
		// �܂����X�g�\���Ɋe�C���|�[�gSVG�v�f�̏������W����
		Rectangle2D.Double bb= new Rectangle2D.Double( -1000.0 , -1000.0 , 1000.0 , 1000.0 );
		for ( int k = 0 ; k < layerPathList.size() ; k++){
			
			
			// Path����������
			String lpath= layerPathList.get(k);
//			System.out.println("dbg0:" + lpath );
			File path = (new File (lpath)).getAbsoluteFile() ;
			lpath = path.getAbsolutePath() ;
//			System.out.println("dbg0.1:" + path.getParent() );
			if ( k == 0 && (hba.rootPath).equals("") ){
				hba.rootPath = path.getParent() + File.separator + containerName;
			}
			String relPath = (PathUtil.getRelativePath( lpath , hba.rootPath )).replace(File.separator , "/" );
//			System.out.println("dbg1:" + lpath + " : " +  hba.rootPath);
			
			// �X�̃C���|�[�gSVG�t�@�C����ǂݍ���
			FileInputStream fis = new FileInputStream( path );
			hba.layerG2S = new Transform();
			
			
			// �C���|�[�gSVG�t�@�C���̒n�����W���BBOX�𓾂�
			// ������layer(Document)�I�u�W�F�N�g���ꎞ�I�ɐ��������
			bb = hba.getLayer(fis);
			
			// �g�b�v�̃C���|�[�gSVG�̐}�@(CRS transform)�A�h�L�������g��ێ����Ă���
			if( k == 0 ){
				hba.contG2S = hba.layerG2S;
				System.out.println("contG2S:"+hba.contG2S );
				hba.topLayer = hba.layer;
			}
			System.out.println("layer" + (k+1) + " Path:" + relPath + " geoBBOX:" + hba.getRectStr(bb));
			
			// �f�[�^���ݔ͈͂̒n�����WBBOX���\�z
			if ( k == 0){
				hba.contGeoBBOX = bb;
			} else if ( ! followTopViewArea ) {
				hba.contGeoBBOX = (Rectangle2D.Double)((hba.contGeoBBOX).createUnion( bb ));
			}
			
			// ���X�g�ɒǉ�����
			ImageGeoBBox.add(bb);
			ImagePath.add(relPath);
		}
		
		// �w�肳��Ă��Ȃ��Ƃ���viewBox��geoBBOX�ɐݒ肷��
		if ( vb == null ){
			vb = hba.contGeoBBOX ;
		}
		
		// �R���e�i��BBOX�𓾂�
		// �ŏ��̃C���|�[�gSVG�t�@�C����CRS�p�����[�^���R���e�i��CRS�p�����[�^�ɂ���
		
		if ( ! hba.appendMode ){
			// �ŏ��Ɏw�肳�ꂽ�C���|�[�gSVG�t�@�C�����Q�l�ɁA�R���e�i�𐶐�����
			hba.buildContainer( hba.topLayer );
		} else {
			// �ŏ��Ɏw�肳�ꂽ�C���|�[�gSVG�t�@�C���ɒǉ�����
			// �d�ˏ��͍Ō�̂��̂���ԉ�(SVG�����I�ɂ͍ŏ��̍s)�ɂȂ�
			hba.buildContainerFromLayer( hba.topLayer );
			hba.projCenter = 999;
			hba.containerHeight = 0;
		}
		
		
		//�W���ܐ�����}�@�ϊ��p�����[�^��ݒ肷��
		//projCenter �W���ܐ��̈ʒu 999:�ŏ��̃C���|�[�gSVG�̂܂�, -999:���S ���̑�:���̈ܓx
		if ( hba.projCenter != 999 ){
			double centerLat;
			if ( hba.projCenter == -999 ){
				centerLat = vb.y + vb.height / 2.0;
			} else {
				centerLat = hba.projCenter;
			}
			double sign = Math.signum(hba.contG2S.d * hba.contG2S.a);
			double ratio = Math.cos(centerLat * Math.PI / 180.0 ) * sign;
			hba.contG2S.a = hba.contG2S.d * ratio;
			
			hba.contG2S.e = - hba.contG2S.a * hba.contGeoBBOX.x + (Math.signum(hba.contG2S.a) - 1) / 2 * hba.contG2S.a * hba.contGeoBBOX.width;
			hba.contG2S.f = - hba.contG2S.d * hba.contGeoBBOX.y + (Math.signum(hba.contG2S.d) - 1) / 2 * hba.contG2S.d * hba.contGeoBBOX.height;
		}
		
		// ��ʂ̃T�C�Y����}�@�ϊ��̃p�����[�^��ݒ肷��
		if ( hba.containerHeight > 0 ){
			double aspect = hba.contG2S.a / hba.contG2S.d;
			hba.contG2S.d = hba.containerHeight / hba.contGeoBBOX.height * Math.signum(hba.contG2S.d);
			hba.contG2S.a = hba.contG2S.d * aspect;
			
			hba.contG2S.e = - hba.contG2S.a * hba.contGeoBBOX.x + (Math.signum(hba.contG2S.a) - 1) / 2 * hba.contG2S.a * hba.contGeoBBOX.width;
			hba.contG2S.f = - hba.contG2S.d * hba.contGeoBBOX.y + (Math.signum(hba.contG2S.d) - 1) / 2 * hba.contG2S.d * hba.contGeoBBOX.height;
		}
		
		
		// CRS��ݒ肷��
		if ( ! hba.appendMode ){ // appandMode�ł͂Ȃ��ꍇ
			hba.setCRS( hba.container , hba.contG2S , hba.contCRSURI );
		}
		
		// data-�J�X�^��������ݒ肷��
		if ( hba.dataAttributes.size() > 0 ){ 
			hba.setDataAttributes( hba.dataAttributes );
		}
		
		// �R���e�i�ɃC���[�W���Z�b�g����
		for ( int k = ImageGeoBBox.size() -1 ; k >= 0  ; k-- ){
			
			// �R���e�i�̍��W�n�ɂ�����BBOX�𓾂�
			Rectangle2D.Double imageBBox = hba.contG2S.getTransformedBBox(ImageGeoBBox.get(k));
			String[] visibleRange = null;
			String opacity = null;
			if ( ZoomRangeList.get(k) != null ){
				visibleRange = hba.getZoomRange( ZoomRangeList.get(k) );
//				System.out.print ( "zr:" + visibleRange );
			}
			
			if ( OpacityList.get(k) != null ){
				opacity = OpacityList.get(k);
			}
			
			if ( AreaOptionList.get(k) != null && (AreaOptionList.get(k)).indexOf("global") >= 0 ){
				imageBBox = hba.contG2S.getTransformedBBox(new Rectangle2D.Double(-180,-90,360,180));
			}
			
			//�R���e�i��image���Z�b�g����
			if ( !(hba.appendMode && k == 0) ){ // appendMode�̏ꍇ�͍ŏ��̃��C���͎������g�Ȃ̂œ���Ȃ�
				
				boolean visibility = hba.getVisibility( hba.visibilityFilter , hba.visibilityFile , ImagePath.get(k) );
				
				hba.setImage( imageBBox , ImagePath.get(k) , visibleRange , opacity , visibility);
			}
			
//			System.out.println("Layer:" + k + " path:" + ImagePath.get(k) +" IMG BBox:" + hba.getRectStr(imageBBox));
		}
		
		hba.setContainerViewBox( hba.contG2S.getTransformedBBox( vb ) ) ;
		hba.setContainerBoundingBox( hba.contG2S.getTransformedBBox( hba.contGeoBBOX ) ) ;
		System.out.println("\nSave Container to: " + hba.rootPath );
//		System.out.println("Container ViewBox:" + hba.getRectStr(hba.contG2S.getTransformedBBox( hba.contGeoBBOX )));
		hba.serialize( hba.rootPath );

	}
	
	HyperBuilderAppl(  )  {
		fmt.setMaximumFractionDigits(7);
		fmt.setGroupingUsed(false);
	}
	
	boolean getVisibility( int visibilityFilter , String visibilityFile , String path ){
		boolean ans = true;
		if ( visibilityFilter == 1 ){ // �ݒ肳�ꂽ���̂̂�visible
			if ( path.indexOf(visibilityFile) >=0 ){
				ans = true;
			} else { 
				ans = false;
			}
		} else if ( visibilityFilter == -1 ){ // �ݒ肳��Ă����̂̂�hidden
			if ( path.indexOf(visibilityFile) >=0 ){
				ans = false;
			} else { 
				ans = true;
			}
		}
		return ( ans );
	}
	
	Rectangle2D.Double getLayer( InputStream is  ) throws Exception {
		Rectangle2D.Double docBB;
		//System.out.println("IsSvg2Map");
		layer= DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse( is );
		Element rootNode = layer.getDocumentElement();
		docBB = getSvgBBox( rootNode );
		layerG2S = getSvgMapCRS( rootNode );
		Transform S2G = new Transform();
		S2G.setInv(layerG2S);
		Rectangle2D.Double globalBBox =  S2G.getTransformedBBox( docBB );
//		System.out.println(" SVG BBox:" + getRectStr(docBB) + "\n GEO BBox:" + getRectStr(globalBBox));
		return ( globalBBox );
	}
	
	
	void setContainerViewBox( Rectangle2D.Double rect ) throws Exception{
		Element rt = container.getDocumentElement();
		rt.setAttribute( "viewBox" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
	}
	void setContainerBoundingBox( Rectangle2D.Double rect ) throws Exception{
		Element rt = container.getDocumentElement();
		rt.setAttribute( "go:dataArea" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
	}
	
	void buildContainer( Document document ) throws Exception{
		Element rootNode = document.getDocumentElement();
		// �o�͂���h�L�������g�̐ݒ�
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace( true );
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation domImpl = builder.getDOMImplementation();
		container = domImpl.createDocument(rootNode.getAttribute("xmlns"), rootNode.getNodeName(), null);
		NamedNodeMap atts = rootNode.getAttributes();
		Element rt = container.getDocumentElement();
		for (int k = 0 ; k < atts.getLength() ; k++ ){
//					System.out.println(atts.item(k).getNodeName() + "=" + atts.item(k).getNodeValue() );
			rt.setAttribute( atts.item(k).getNodeName() ,  atts.item(k).getNodeValue() );
		}
//		rt.setAttribute( "viewBox" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
//		System.out.println( "Rect:" + rect );
		rt.setAttribute( "xmlns:xlink" , "http://www.w3.org/1999/xlink" );
		Element contRoot = container.getDocumentElement();
		
	}
	
	void setDataAttributes( Map<String, String> attrMap ) throws Exception {
		Element rt = container.getDocumentElement();
		
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			// �L�[���擾
			String key = entry.getKey();
			// �l���擾
			String val = entry.getValue();
			rt.setAttribute("data-"+key, val);
		}
	}
	
	void buildContainerFromLayer( Document document ) throws Exception{
		container = document;
	}
	
	void setCRS(Document document , Transform trans ,  String uri ){
		Node rootNode=document.getFirstChild();
		
		
		
		// metadata�m�[�h��ǉ�
		rootNode.appendChild(document.createTextNode("\n"));
		Element metaNode=document.createElement("metadata");
		rootNode.appendChild(metaNode);
		rootNode.appendChild(document.createTextNode("\n"));
		
		// metadata�m�[�h�̎q����rdf:RDF��ǉ�
		Element rdfNode = document.createElement("rdf:RDF");
		Attr rdfNsAttr=document.createAttribute("xmlns:rdf");
		rdfNsAttr.setNodeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		Attr crsNsAttr=document.createAttribute("xmlns:crs");
		crsNsAttr.setNodeValue("http://www.ogc.org/crs");
		Attr svgNsAttr=document.createAttribute("xmlns:svg");
		svgNsAttr.setNodeValue("http://www.w3.org/2000/svg");
		rdfNode.setAttributeNode(rdfNsAttr);
		rdfNode.setAttributeNode(crsNsAttr);
		rdfNode.setAttributeNode(svgNsAttr);
		
		metaNode.appendChild(document.createTextNode("\n"));
		metaNode.appendChild(rdfNode);
		metaNode.appendChild(document.createTextNode("\n"));

		rdfNode.appendChild(document.createTextNode("\n"));
		Element crsNode=document.createElement("crs:CoordinateReferenceSystem");
		rdfNode.appendChild(crsNode);
		rdfNode.appendChild(document.createTextNode("\n"));
		Attr crsURIAttr=document.createAttribute("rdf:resource");
		crsURIAttr.setNodeValue( uri );
		crsNode.setAttributeNode(crsURIAttr);
		
		Attr crsTransAttr=document.createAttribute("svg:transform");
		crsTransAttr.setNodeValue( "matrix(" + trans.a + "," + trans.b + "," + trans.c + "," 
			+ trans.d + "," + trans.e + "," + trans.f + ")"  );
		crsNode.setAttributeNode(crsTransAttr);
		
		// SVG Tiling & Layering�d�l�ɑΉ�
		Element gcsNode=document.createElement("globalCoordinateSystem");
		rootNode.appendChild(gcsNode);
		rootNode.appendChild(document.createTextNode("\n"));
		gcsNode.setAttribute("srsName",uri);
		gcsNode.setAttribute("transform","matrix(" + trans.a + "," + trans.b + "," + trans.c + "," 
			+ trans.d + "," + trans.e + "," + trans.f + ")");
		
	}
	
	void setImage(Rectangle2D.Double rect , String path , String[] visibleRange , String opacity , boolean visibility ){
		Element contRoot = container.getDocumentElement();
//		if ( breforeNode == null ){
//			Text topCr = container.createTextNode("\n") ;
//			breforeNode = contRoot.appendChild(topCr);
//		}
		Text cr = container.createTextNode("\n") ;
		contRoot.appendChild(cr);
		Element imageElement;
		if ( isSvgTL ) {
			imageElement = container.createElement("animation");
		} else {
			imageElement = container.createElement("image");
		}
		
		if ( !visibility ){
			imageElement.setAttribute("visibility","hidden");
		}
		
		Attr xAtt = container.createAttribute("x");
		xAtt.setNodeValue(Double.toString(rect.x));
		imageElement.setAttributeNode(xAtt);
		
		Attr yAtt = container.createAttribute("y");
		yAtt.setNodeValue(Double.toString(rect.y));
		imageElement.setAttributeNode(yAtt);
		
		Attr wAtt = container.createAttribute("width");
		wAtt.setNodeValue(Double.toString(rect.width));
		imageElement.setAttributeNode(wAtt);
		
		Attr hAtt = container.createAttribute("height");
		hAtt.setNodeValue(Double.toString(rect.height));
		imageElement.setAttributeNode(hAtt);
		
		Attr refAtt = container.createAttribute("xlink:href");
		refAtt.setNodeValue(path);
		imageElement.setAttributeNode(refAtt);
		
		if ( visibleRange != null ){
			Attr vzrAtt = container.createAttribute("go:figure-visibility");
			vzrAtt.setNodeValue( visibleRange[0] );
			imageElement.setAttributeNode(vzrAtt);
			// add for SVGT&L
			if ( visibleRange[1].length() > 0 ){
				Attr vzrAttMin = container.createAttribute("visibleMinZoom");
				vzrAttMin.setNodeValue( visibleRange[1] );
				imageElement.setAttributeNode(vzrAttMin);
			}
			if ( visibleRange[2].length() > 0 ){
				Attr vzrAttMax = container.createAttribute("visibleMaxZoom");
				vzrAttMax.setNodeValue( visibleRange[2] );
				imageElement.setAttributeNode(vzrAttMax);
			}
			
		}
		
		if ( opacity != null ){
			Attr opAtt =  container.createAttribute("opacity");
			opAtt.setNodeValue( opacity );
			imageElement.setAttributeNode(opAtt);
		}
		
		contRoot.appendChild( imageElement );
		Text cr2 = container.createTextNode("\n") ;
		contRoot.appendChild(cr2);
		
		if ( debug ){
			Element rectElement = container.createElement("rect");
			rectElement.setAttribute("x" , Double.toString(rect.x));
			rectElement.setAttribute("y" , Double.toString(rect.y));
			rectElement.setAttribute("width" , Double.toString(rect.width));
			rectElement.setAttribute("height" , Double.toString(rect.height));
			rectElement.setAttribute("fill","none");
			rectElement.setAttribute("stroke","red");
			rectElement.setAttribute("stroke-width","0.0005");
			contRoot.appendChild( rectElement );
			Text cr3 = container.createTextNode("\n") ;
			contRoot.appendChild(cr3);
		}
		
	}
		
	public void serialize( String outs) throws Exception{
		
		// �R���e�i�̏o��
		FileOutputStream osFile = new FileOutputStream( outs  );
		OutputStreamWriter fos = new OutputStreamWriter( osFile , "UTF-8" );
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		DOMSource source = new DOMSource(container);
		StreamResult result = new StreamResult(fos);
		transformer.transform(source, result);
		
		
	}
	private Rectangle2D.Double getSvgBBox(Node svgr ){
		Rectangle2D.Double BB = new Rectangle2D.Double( 0.0 , 0.0 , 0.0 , 0.0 );
		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;
		boolean falure = false;
		
		// �悸�Ago:dataArea�����Ă��̎���go:boundingBox�̃p�����[�^�����Ă݂�E�E���ꂪ��Ԋm���炵���͂��Ȃ̂�
		// 20101022�ύX(go:boundingBox��SVGMapTK�œƓ��̓���������̂ŁE�E)
		try {
			String bbox = ((Element)svgr).getAttribute("go:dataArea");
//			System.out.println("boundingBox:" + bbox);
			StringTokenizer st = new StringTokenizer(bbox, " ,");
			x = Double.parseDouble(st.nextToken());
			y = Double.parseDouble(st.nextToken());
			w = Double.parseDouble(st.nextToken());
			h = Double.parseDouble(st.nextToken());
//			System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
		} catch (Exception e) {
			falure = true;
		}
		
		if ( falure ){
			falure = false;
			try {
				String bbox = ((Element)svgr).getAttribute("go:boundingBox");
	//			System.out.println("boundingBox:" + bbox);
				StringTokenizer st = new StringTokenizer(bbox, " ,");
				x = Double.parseDouble(st.nextToken());
				y = Double.parseDouble(st.nextToken());
				w = Double.parseDouble(st.nextToken());
				h = Double.parseDouble(st.nextToken());
	//			System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
		
//		System.out.println(falure);
		
		// ���݂��Ȃ��ꍇ�́AviewBox�����Ă݂� 20100802�ύX
		if ( falure ){
			falure = false;
			try {
				String bbox = ((Element)svgr).getAttribute("viewBox");
//				System.out.println("viewBox:" + bbox);
				StringTokenizer st = new StringTokenizer(bbox, " ,");
				x = Double.parseDouble(st.nextToken());
				y = Double.parseDouble(st.nextToken());
				w = Double.parseDouble(st.nextToken());
				h = Double.parseDouble(st.nextToken());
//				System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
//		System.out.println(falure);
		
		// ����ł������ꍇ��width,height���g��
		if ( falure ){
			falure = false;
			try {
				x = 0.0;
				y = 0.0;
				w = Double.parseDouble(((Element)svgr).getAttribute("width"));
				h = Double.parseDouble(((Element)svgr).getAttribute("height"));
//		System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
		
//		System.out.println(falure);
		if ( !falure ) {
			BB.x = x;
			BB.y = y;
			BB.width = w;
			BB.height = h;
		}
		
		System.out.println( "SVG_BBOX x:" + BB.x + " y:" + BB.y + " w:" + BB.width + " h:" + BB.height );
		return ( BB );
	}
	
	
	
	private Transform getSvgMapCRS(Element svgr ){
		Transform crsp = new Transform();
		String transform="";
		try{
			NodeList nl = svgr.getElementsByTagName("globalCoordinateSystem");
			Element crse = (Element)nl.item(0);
			transform = crse.getAttribute("transform");
		} catch ( Exception e ) {
			// ���������ȏ����ł����E�E globalCoordinateSystem�����������ꍇ�́Acrs:...��T��
//			System.out.println("no gcs: " + e );
			NodeList nl = svgr.getElementsByTagName("crs:CoordinateReferenceSystem");
			Element crse = (Element)nl.item(0);
			transform = crse.getAttribute("svg:transform");
		}
		
		String tfValueI=transform.trim().substring(7);
		tfValueI=tfValueI.substring(0,tfValueI.length()-1); 
	// System.out.println( "CRST:" + tfValueI);
		StringTokenizer stI = new StringTokenizer(tfValueI,", ");
		try {
			crsp.a= Double.parseDouble(stI.nextToken());
			crsp.b= Double.parseDouble(stI.nextToken());
			crsp.c= Double.parseDouble(stI.nextToken());
			crsp.d= Double.parseDouble(stI.nextToken());
			crsp.e= Double.parseDouble(stI.nextToken());
			crsp.f= Double.parseDouble(stI.nextToken());
		} catch  (NumberFormatException e ) {
		}
//		System.out.println("tf:" + crsp);
		return ( crsp );
	}
	
	public String getRectStr( Rectangle2D.Double rect ){
		return ( "x:" + fmt.format(rect.x) + " y:" + fmt.format(rect.y) + " w:" + fmt.format(rect.width) + " h:" + fmt.format(rect.height) );
	}
	
	
	String[] getZoomRange( String rangeStr ) throws Exception { // [0]:�J���}��؂�minmax, [1]:min,[2]:max (�����ꍇ��-1)
		String[] ans = new String[3];
		// contG2S
//		double scale = Math.sqrt( contG2S.a *  contG2S.a + contG2S.d * contG2S.d ); // |ad-bc?|
		StringTokenizer st = new StringTokenizer(rangeStr , "-" , true );
		int tkC = st.countTokens();
//		System.out.println("tkc:" + tkC );
		String tk = st.nextToken();
		double p1 , p2;
		if ( tk.indexOf("-") >= 0 ){ // -1000�̂悤�ȋL�q
			p1 = 0;
			p2 = Double.parseDouble(st.nextToken());
		} else {
			p1 = Double.parseDouble(tk);
			if ( tkC <= 2 ){ // 100- �� 100�̂悤�ȋL�q
				p2 = -1; // -1:����
			} else {
				tk = st.nextToken(); // - ��ǂݔ�΂�
				if ( tk.indexOf("inf") >=0 ){
					p2 = -1;
				} else {
					p2 = Double.parseDouble(st.nextToken());
				}
			}
		}
		
		String maxp , minp;
		
		if ( p1 > 0 ){
			p1 = getZoom( p1 );
		}
		if ( p2 > 0 ){
			p2 = getZoom( p2 );
		}
		if ( p1 > 0 && p2 > 0 && p2 > p1 ){
			double tmp = p1;
			p1 = p2;
			p2 = tmp;
		}
		
//		p2 = getZoom(Double.parseDouble(st.nextToken()));
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(0);
		
		if ( p2 > 0 ){
			minp = nFmt.format(p2);
		} else {
			minp ="";
		}
		ans[1] = minp;
		
		ans[2] ="";
		if ( p1 > 0 ){
			maxp = "," + nFmt.format(p1);
			ans[2] = nFmt.format(p1);
		} else {
			maxp ="";
		}
		
		ans[0] = minp + maxp;
		
		return ( ans );
	}
	
	double getZoom( double range ){
		return( 40000000.0 * 100.0 * 100.0 / ( range * 360.0 * Math.abs(contG2S.d) ) );
	}
	
	Rectangle2D.Double getGeoBBox ( String areaStr ) throws Exception {
		double radius;
		ISO6709 point1 , point2;
		StringTokenizer st = new StringTokenizer(areaStr , "/" );
		if ( st.countTokens() != 2 ){
			throw new IllegalArgumentException("viewarea�̃p�����[�^���Ⴂ�܂�");
		}
		
		point1 = new ISO6709(st.nextToken());
		String p2 = st.nextToken();
		if ( p2.indexOf("+") >= 0 || p2.indexOf("-") >= 0 ){
			point2 = new ISO6709( p2 );
			if (point1.latitude > point2.latitude){
				double tmp = point1.latitude;
				point1.latitude = point2.latitude;
				point2.latitude = tmp;
			}
			if (point1.longitude > point2.longitude){
				double tmp = point1.longitude;
				point1.longitude = point2.longitude;
				point2.longitude = tmp;
			}
		} else {
			radius = Double.parseDouble(p2);
			double rLon = ( 360.0 / ( 40000000.0 * Math.cos( point1.latitude * Math.PI/180.0 ) ) ) * radius;
			double rLat = ( 360.0 / 40000000.0 ) * radius;
			point1.latitude = point1.latitude - rLat;
			point1.longitude = point1.longitude - rLon;
			point2 = new ISO6709( (point1.latitude + rLat * 2.0) , (point1.longitude + rLon * 2.0) );
		}
		return ( new Rectangle2D.Double( point1.longitude , point1.latitude ,point2.longitude - point1.longitude , point2.latitude - point1.latitude ));
	}

}
