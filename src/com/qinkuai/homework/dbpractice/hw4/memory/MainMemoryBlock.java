package com.qinkuai.homework.dbpractice.hw4.memory;

import java.util.Arrays;

//对应一块内存
//这里指定一个内存块为4KB
public class MainMemoryBlock {
	//内存块号
	private int memoryNo;
	//内存块大小
	private final int blockSize = 4 * 1024;
	//4KB内存块
	private byte[] memBlock = new byte[blockSize];
	//有效数据的位置记录
	private int position = 0;
	
	
	public MainMemoryBlock(int memoryNo) {
		this.memoryNo = memoryNo;
	}
	
	public void setMemBlock(byte[] bytes, int length) throws Exception{
		if (bytes.length > blockSize) {
			throw new Exception("Too large data for single block.");
		}
		
		position = length;
		
		//length表示有效数据长度
		memBlock = Arrays.copyOf(bytes, length);
	}
	
	public int getMemoryNo() {
		return memoryNo;
	}
	
	public byte[] getMemBlock() {
		return Arrays.copyOf(memBlock, position);
	}
	
	public int getPosition() {
		return position;
	}
}
