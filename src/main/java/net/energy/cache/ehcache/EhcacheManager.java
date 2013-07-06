package net.energy.cache.ehcache;

import java.io.InputStream;

import net.energy.cache.CacheManager;
import net.energy.cache.MultiLevelCache;
import net.energy.cache.MultiLevelCacheManager;
import net.energy.exception.CacheUnreachableException;
import net.energy.utils.ClassHelper;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ehcache的管理类，用于获取Cache对象，一般采用ehcache.xml配置，如果没有找到配置，则将采用如下配置
 * 
 * <pre>
 * 内存最多对象数（maxElementsInMemory）：10000，
 * 内存存储策略（memoryStoreEvictionPolicy）: LRU ，
 * 是否在内存对象数大于最多对象后存储于硬盘（overflowToDisk）: true ，
 * 是否永久有效（eternal）：false
 * 元素生存时间，只要到这么长时就被销毁（单位：秒）（timeToLive）：120 ，
 * 元素空闲时间，这么长时间没被访问则销毁（单位：秒）（timeToLive）：120 ，
 * 是否允许数据持久化到内存（如果是，即JVM重启缓存中的数据仍然有效，该设置影响性能，大概减低8倍性能）（diskPersistent）：false ，
 * 磁盘中数据过期检查线程的轮询时间间隔（单位：秒）（diskExpiryThreadIntervalSeconds）：120 ，
 * </pre>
 * 
 * @author wuqh
 * @see net.sf.ehcache.CacheManager
 */
public class EhcacheManager extends MultiLevelCacheManager implements CacheManager {
	private static final String CLASSPATH_PREFIX = "classpath:";
	private static final Logger LOGGER = LoggerFactory.getLogger(EhcacheManager.class);
	private net.sf.ehcache.CacheManager cacheManager;

	private String configLocation;

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	private boolean initialized = false;

	@Override
	protected MultiLevelCache getCurrentLevelCache(String pool) {
		initialize();
		net.sf.ehcache.Cache cache;
		try {
			cache = doGetCache(pool.toUpperCase());
		} catch (Exception e) {
			throw new CacheUnreachableException("创建Ehcache实例失败");
		}
		Ehcache ehcache = new Ehcache();
		ehcache.setCache(cache);
		return ehcache;
	}

	private net.sf.ehcache.Cache doGetCache(String cacheName) {
		net.sf.ehcache.Cache rawCache;
		if (this.cacheManager.cacheExists(cacheName)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("使用已有EHCache缓存[" + cacheName + "]");
			}
			rawCache = this.cacheManager.getCache(cacheName);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("创建EHCache缓存[" + cacheName + "]");
			}
			rawCache = createCache(cacheName);
			this.cacheManager.addCache(rawCache);
		}

		return rawCache;
	}

	private net.sf.ehcache.Cache createCache(String cacheName) {
		MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;
		int maxElementsInMemory = 10000;
		boolean overflowToDisk = true;
		boolean eternal = false;
		int timeToLive = 120;
		int timeToIdle = 120;
		boolean diskPersistent = false;
		int diskExpiryThreadIntervalSeconds = 120;
		net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(cacheName, maxElementsInMemory,
				memoryStoreEvictionPolicy, overflowToDisk, null, eternal, timeToLive, timeToIdle, diskPersistent,
				diskExpiryThreadIntervalSeconds, null, null);

		return cache;

	}

	private void initialize() {
		if (initialized) {
			return;
		}

		if (this.cacheManager == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("使用默认的EHCache CacheManager");
			}
			InputStream is = null;
			if (StringUtils.isNotEmpty(configLocation)) {
				configLocation = StringUtils.replaceOnce(configLocation, CLASSPATH_PREFIX, "");
				is = ClassHelper.getClassLoader().getResourceAsStream(configLocation);
				if (is == null) {
					LOGGER.info("没有在ClassPath中找到文件[" + configLocation + "]，系统将使用默认的EHCache配置");
				}
			}
			try {
				this.cacheManager = (is == null) ? net.sf.ehcache.CacheManager.getInstance()
						: net.sf.ehcache.CacheManager.create(is);
			} catch (Exception e) {
				throw new CacheUnreachableException(e);
			}
			this.initialized = true;
		}

	}

}
