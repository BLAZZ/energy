package net.energy.jdbc.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.energy.exception.RetrievalIdException;
import net.energy.jdbc.KeyHolder;

/**
 * KeyHolder的一个默认实现
 * 
 * @author wuqh
 * 
 */
public class GeneratedKeyHolder implements KeyHolder {
	private final List<Map<String, Object>> keyList;

	public GeneratedKeyHolder() {
		this.keyList = new LinkedList<Map<String, Object>>();
	}

	// /**
	// * Create a new GeneratedKeyHolder with a given list.
	// *
	// * @param keyList
	// * a list to hold maps of keys
	// */
	// public GeneratedKeyHolder(List<Map<String, Object>> keyList) {
	// this.keyList = keyList;
	// }

	public Number getKey() {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1 || this.keyList.get(0).size() > 1) {
			throw new RetrievalIdException("getKey方法只适用于单个主键的表结构，但当前表结构包含多个主键: " + this.keyList);
		}
		Iterator<Object> keyIterator = this.keyList.get(0).values().iterator();
		if (keyIterator.hasNext()) {
			Object key = keyIterator.next();
			if (!(key instanceof Number)) {
				throw new RetrievalIdException("生成的主键非数值类型，无法将[" + (key != null ? key.getClass().getName() : null)
						+ "]类型转换为[" + Number.class.getName() + "]类型");
			}
			return (Number) key;
		} else {
			throw new RetrievalIdException("无法获取主键，请检查表结构中是否包含主键");
		}
	}

	public Map<String, Object> getKeys() {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1) {
			throw new RetrievalIdException("getKeys方法只适用于单个主键的表结构，但当前表结构包含多个主键:" + this.keyList);
		}
		return this.keyList.get(0);
	}

	public List<Map<String, Object>> getKeyList() {
		return this.keyList;
	}
}
