package com.qinkuai.homework.dbpractice.hw4.memory;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainMemoryManager {
	private static MainMemoryManager memoryManager;
	// 内存块数
	public static final int memoryBlocksNumber = 4;	
	// 保有16个独立的内存块
	private List<MainMemoryBlock> mainMemory;
	// 文件对应的所有页表
	private List<PageItem> pages;
	// 页表栈
	// 表示当前实时在内存中的页
	private LinkedList<PageItem> pageQueue; 
	
	private MainMemoryManager() {
		mainMemory = new ArrayList<>();
		// 初始化16块独立内存块
		for (int i = 0; i < memoryBlocksNumber; i++) {
			mainMemory.add(new MainMemoryBlock(i));
		}

		//所有页表的列表
		pages = new ArrayList<>();
		
		// 栈式管理内存进出
		pageQueue = new LinkedList<>();
	}

	// 单实例化内存管理器
	public static MainMemoryManager getInstance() {
		if (memoryManager == null) {
			memoryManager = new MainMemoryManager();
		}
		return memoryManager;
	}

	//生成该文件对应的页表列表
	public void generatePages(int wholeSize) {
		for (int i = 0; i < wholeSize; i++) {
			//前提建立在
			//页码 = 文件中起始点
			pages.add(new PageItem(i, i));
		}
	}

	// 读取内存中的数据
	public byte[] readFromMemory(int pageNo, File file) {
		PageItem page = pageExists(pageNo);

		if (page != null) {
			//如果所求页已加载进内存
			//则调到栈顶
			//返回对应数据
			pageQueue.remove(page);
			pageQueue.add(page);
			return mainMemory.get(page.getMemBlockNo()).getMemBlock();
		}else {
			//如果需求页尚未加载到页栈
			page = pages.get(pageNo);
			byte[] bytes = new byte[4 * 1024];
			int length = 0;
			int memoryNo = pageQueue.size();
			
			//从磁盘中读出文件对应内容
			try {
				FileInputStream fis = new FileInputStream(file);
				fis.skip(1024 * 4 * pageNo);
				length = fis.read(bytes);
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//当队列已满时
			//将队列第一位推出队列
			if (pageQueue.size() == memoryBlocksNumber) {
				//PageItem item = pageQueue.poll();
				//PageItem item = pageQueue.pop();
				PageItem item = pageQueue.getLast();
				
				
				//还需要将推出的页的内容保存进文件
				memoryNo = item.getMemBlockNo();
				try {
					writeToDisk(file, item.getPageNo(), mainMemory.get(memoryNo).getMemBlock(), mainMemory.get(memoryNo).getPosition(), false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				pageQueue.remove(memoryBlocksNumber - 1);
			}
			page.setMemBlockNo(memoryNo);
			//添加新页进入队列
			pageQueue.add(page);
			
			//设置内存块实例
			try {
				mainMemory.get(memoryNo).setMemBlock(bytes, length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mainMemory.get(memoryNo).getMemBlock();
		}
	}

	// 更新修改了的内存页
	// 将内存中的各个内存块写入存储
	public void writeToDisk(File file, int pageNo, byte[] content, int length, boolean updateMem) throws Exception{
		PageItem page = pageExists(pageNo);
		if (page == null) {
			throw new Exception("内存中无此页数据");
		}
		
		//更新内存中的文件数据
		if (updateMem) {
			mainMemory.get(page.getMemBlockNo()).setMemBlock(content, length);
		}
		
		//更新磁盘中的文件
		try {
			File tempFile = File.createTempFile("temp", ".txt", file.getParentFile());
			FileInputStream fisTemp = new FileInputStream(file);
			FileOutputStream fosTemp = new FileOutputStream(tempFile);
			
			byte[] bytes = new byte[1024 * 4];
			int counter = fisTemp.read(bytes);
			int position = 0;
			
			while (counter != -1) {
				fosTemp.write(bytes, 0, counter);
				counter = fisTemp.read(bytes);
			}
			
			fisTemp.close();
			fosTemp.close();
			
			
			FileInputStream fis = new FileInputStream(tempFile);
			FileOutputStream fos = new FileOutputStream(file);
			counter = fis.read(bytes);
			
			while(counter != -1) {
				if (page.getPosition() == position) {
					fos.write(mainMemory.get(page.getMemBlockNo()).getMemBlock(), 0, mainMemory.get(page.getMemBlockNo()).getPosition());
				}else {
					fos.write(bytes, 0, counter);
				}
				counter = fis.read(bytes);
				position++;
			}
			
			//新添加的页
			if (page.getPosition() >= position) {
				fos.write(mainMemory.get(page.getMemBlockNo()).getMemBlock(), 0, mainMemory.get(page.getMemBlockNo()).getPosition());
			}
			
			fis.close();
			fos.close();
			tempFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//向文件对应的页表列表中添加新的一张页
	//并调入内存
	public void addNewPageToMem(File file) {
		PageItem newPage = new PageItem(pages.size(), pages.size());
		pages.add(newPage);
		
		//调进内存
		int memoryNo = pageQueue.size();
		
		//当队列已满时
		//将队列第一位推出队列
		if (pageQueue.size() == memoryBlocksNumber) {
			PageItem item = pageQueue.poll();
			//还需要将推出的页的内容保存进文件
			memoryNo = item.getMemBlockNo();
			try {
				writeToDisk(file, item.getPageNo(), mainMemory.get(memoryNo).getMemBlock(), mainMemory.get(memoryNo).getPosition(), false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		newPage.setMemBlockNo(memoryNo);
		//添加新页进入队列
		pageQueue.add(newPage);
		
		//设置内存块实例
		try {
			mainMemory.get(memoryNo).setMemBlock(new byte[10], 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//在页表栈中查看是否存在
	private PageItem pageExists(int pageNo) {
		for (PageItem pageItem : pageQueue) {
			if (pageItem.getPageNo() == pageNo) {
				return pageItem;
			}
		}
		return null;
	}
	
	public LinkedList<PageItem> getPageQueue() {
		return pageQueue;
	}
	
	public List<PageItem> getPages() {
		return pages;
	}
}
