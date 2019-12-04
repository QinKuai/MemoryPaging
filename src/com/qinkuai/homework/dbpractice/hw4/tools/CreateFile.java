package com.qinkuai.homework.dbpractice.hw4.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateFile {

	public static void main(String[] args) {
		String line = "Furture will be better! QinKuai is a SB\n";
		File file = new File("res/test.txt");
		
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			int counter = 0;
			
			while(counter++ < 1700) {
				fos.write((counter + " " + line).getBytes());
			}
			
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("END!");
	}

}
