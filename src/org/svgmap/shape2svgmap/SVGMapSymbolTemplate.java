package org.svgmap.shape2svgmap;

import java.io.*;
import java.io.IOException;

public class SVGMapSymbolTemplate {
	// �P�Ƀe�L�X�g�t�@�C���ǂݍ���Œ��g��symbolFile�ɓ���邾��
	// 2017.9.15 �}�C�i�[�X�V�@�C�ɂ��邱�Ƃ͂��Ԃ񉽂��Ȃ�
	public void readSymbolFile( String file )throws Exception{
		BufferedReader sreader = null;
		try{
	//		BufferedReader sreader = new BufferedReader(new FileReader(new File(file)));
			sreader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"UTF-8"));
			
			String line;
			while((line=sreader.readLine())!=null){
				symbolFile += line + "\n";
			}
			sreader.close();
		} catch ( Exception e ){
			e.printStackTrace();
			sreader.close();
		}
	}
	
	public String symbolFile ="";

}