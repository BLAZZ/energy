package net.energy.cache;

import java.io.Serializable;

/**
 * 缓存对象封装类，包含Cache的内容实例，以及版本号
 * 
 * @author wuqh
 * 
 */
public class CacheObject implements Serializable {
	private static final long serialVersionUID = 2284022570013430991L;
	private final Serializable cacheObject;
	private final long version;

	public Serializable getCacheObject() {
		return cacheObject;
	}

	public CacheObject(Serializable object, long version) {
		this.cacheObject = object;
		this.version = version;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public String toString() {
		String cacheObjectStr = "NULL";
		if (cacheObject != null) {
			cacheObjectStr = cacheObject.toString();
		}
		return "实际缓存对象[" + cacheObjectStr + "]，版本[" + version + "]";
	}
}
