package net.energy.cache;

public abstract class MultiLevelCacheManager implements CacheManager {
	private CacheManager highLevelManager;
	
	public void setHighLevelManager(CacheManager highLevelManager) {
		this.highLevelManager = highLevelManager;
	}
	
	
	@Override
	public MultiLevelCache getCache(String pool) {
		MultiLevelCache cache = getCurrentLevelCache(pool);
		
		if(highLevelManager != null) {
			Cache highLevelCache = highLevelManager.getCache(pool);
			cache.setHighLevelCache(highLevelCache);
		}
		
		return cache;
	}
	
	protected abstract MultiLevelCache getCurrentLevelCache(String pool);
}
