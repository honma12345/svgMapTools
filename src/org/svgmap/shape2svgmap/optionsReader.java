package org.svgmap.shape2svgmap;

// �w�肵���t�@�C������A�R�}���h���C���̃I�v�V���������̏����擾����B
// �R�}���h���C���̒��������Ɉ���������قǒ����I�v�V������java�ɓ�������Ƃ��̃A�V�X�^���g�N���X
// �󔒕�����������͉��s�ɂ���ċ�؂�ꂽ�I�v�V������ǂݎ��AgetOptions()��String[]�Ƃ��ĕԋp����
//
// ���� Shape2svgmap, shape2imagesvgmap �y�ю��Ӄw���p�[�A�v�������ׂ�GPL3�ŃI�[�v���\�[�X������\��ł��B
// 2016.8.2 Programmed by Satoru Takagi

import java.io.*;
import java.util.*;

public class optionsReader {

	public static void main(String args[]) {
		optionsReader or = new optionsReader( new File("optionsTest.txt") );
		String[] ans = or.getOptions();
		for ( int i = 0 ; i < ans.length ; i++ ){
			System.out.print(ans[i] + " , " );
		}
	}
	
	String[] ans;
	optionsReader(File inputFile){
		ArrayList<String> ansList = new ArrayList<String>();
		try{
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineResult = line.split("[\\s]+");
				for ( int i = 0 ; i < lineResult.length ; i++ ){
					ansList.add(lineResult[i]);
				}
			}
			ans = (String[])(ansList.toArray(new String[0]));
			
		} catch (IOException ex) {
			//��O����������
			ex.printStackTrace();
		}
	}
	
	String[] getOptions(){
		return ( ans );
	}
	

}

