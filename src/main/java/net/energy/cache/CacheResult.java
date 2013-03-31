package net.energy.cache;

/**
 * 缓存操作结果，包含缓存的客户端实例，当前的对象版本，版本的key值，分页对象的缓存key值，缓存是否命中，数据访问操作返回的结果
 * 
 * @author wuqh
 * 
 */
public class CacheResult {
	private Cache cache;
	private long currentVersion = 0;
	private String versionKey;
	private String pageKey;
	private String key;
	private boolean hit;
	private Object retVal;

	public CacheResult(boolean hit) {
		this.hit = hit;
	}

	/**
	 * @return 当前版本号
	 */
	public long getCurrentVersion() {
		return currentVersion;
	}

	/**
	 * @param currentVersion
	 */
	public void setCurrentVersion(long currentVersion) {
		this.currentVersion = currentVersion;
	}

	/**
	 * @return
	 */
	public String getVersionKey() {
		return versionKey;
	}

	/**
	 * @param versionKey
	 */
	public void setVersionKey(String versionKey) {
		this.versionKey = versionKey;
	}

	/**
	 * @return
	 */
	public String getPageKey() {
		return pageKey;
	}

	/**
	 * @param pageKey
	 */
	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	/**
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	public boolean isHit() {
		return hit;
	}

	public void setHit(boolean hit) {
		this.hit = hit;
	}

	public Object getRetVal() {
		return retVal;
	}

	public void setRetVal(Object retVal) {
		this.retVal = retVal;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

}
