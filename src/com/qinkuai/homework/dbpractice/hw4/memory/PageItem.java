package com.qinkuai.homework.dbpractice.hw4.memory;

public class PageItem {
	//页码
	private int pageNo;
	//实际内存位置
	private int memBlockNo;
	//该页对应在文件中的位置顺序
	private int position;
	
	public PageItem() {
	}
	
	public PageItem(int pageNo, int position) {
		this.pageNo = pageNo;
		this.position = position;
	}
	
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	
	public int getPageNo() {
		return pageNo;
	}
	
	public void setMemBlockNo(int memBlockNo) {
		this.memBlockNo = memBlockNo;
	}
	
	public int getMemBlockNo() {
		return memBlockNo;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
}
