package net.energy.utils;

import java.io.Serializable;

/**
 * 分页信息
 * 
 * @author wuqh
 */
public class Page implements Serializable {
	private static final long serialVersionUID = 8526602444853514919L;
	// 保持大小不变
	private int curpage = 1;
	private int size;
	private int total;
	private int totalPage;

	public int getCurpage() {
		return curpage;
	}

	public void setCurpage(int curpage) {
		this.curpage = curpage;
		this.total = -1;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
		int pages = total / size;
		boolean hasMore = (0 != total % size);
		if (0 == pages) {
			totalPage = 1;
		} else {
			if (hasMore) {
				totalPage = pages + 1;
			} else {
				totalPage = pages;
			}
		}
		if (curpage > totalPage) {
			curpage = totalPage;
		}
		if (curpage < 1) {
			curpage = 1;
		}
	}

	public boolean isFirst() {
		return (curpage <= 1);
	}

	public boolean isLast() {
		return (curpage >= totalPage);
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getStartIndex() {
		return (curpage - 1) * size;
	}

}
