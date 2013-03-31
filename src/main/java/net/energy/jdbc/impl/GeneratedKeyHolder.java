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

//	/**
//	 * Create a new GeneratedKeyHolder with a given list.
//	 * 
//	 * @param keyList
//	 *            a list to hold maps of keys
//	 */
//	public GeneratedKeyHolder(List<Map<String, Object>> keyList) {
//		this.keyList = keyList;
//	}

	public Number getKey() {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1 || this.keyList.get(0).size() > 1) {
			throw new RetrievalIdException("The getKey method should only be used when a single key is returned.  "
					+ "The current key entry contains multiple keys: " + this.keyList);
		}
		Iterator<Object> keyIter = this.keyList.get(0).values().iterator();
		if (keyIter.hasNext()) {
			Object key = keyIter.next();
			if (!(key instanceof Number)) {
				throw new RetrievalIdException("The generated key is not of a supported numeric type. "
						+ "Unable to cast [" + (key != null ? key.getClass().getName() : null) + "] to ["
						+ Number.class.getName() + "]");
			}
			return (Number) key;
		} else {
			throw new RetrievalIdException("Unable to retrieve the generated key. "
					+ "Check that the table has an identity column enabled.");
		}
	}

	public Map<String, Object> getKeys() {
		if (this.keyList.size() == 0) {
			return null;
		}
		if (this.keyList.size() > 1)
			throw new RetrievalIdException(
					"The getKeys method should only be used when keys for a single row are returned.  "
							+ "The current key list contains keys for multiple rows: " + this.keyList);
		return this.keyList.get(0);
	}

	public List<Map<String, Object>> getKeyList() {
		return this.keyList;
	}
}
