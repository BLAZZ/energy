package net.energy.cache.memcached;

import java.util.HashMap;
import java.util.Map;

import net.energy.cache.CacheManager;
import net.energy.cache.MultiLevelCache;
import net.energy.cache.MultiLevelCacheManager;
import net.energy.exception.CacheUnreachableException;
import net.energy.utils.Assert;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

/**
 * MemCached客户端的管理类，用于获取MemCached客户端对象，此类包含了MemCached参数配置以及默认值：
 * 
 * <pre>
 * 初始连接数（initConn）：默认1，
 * 最小连接数（minConn）：默认1，
 * 最大连接数（maxConn）: 默认100 ，
 * 连接最大空闲时间（单位：毫秒）（maxIdle）: 默认6小时 ，
 * 主线程睡眠时间，每隔这些时间会醒来一次（单位：秒）（maintSleep）：默认30秒 ，
 * 是否启用nagle算法（nagle）：默认false ，
 * 读取超时时间（单位：毫秒）（readTimeout）：默认3秒 ，
 * 连接超时时间（单位：毫秒）（connectTimeout）：默认不超时 ，
 * </pre>
 * 
 * @author wuqh
 * @see SockIOPool
 */
public class MemcachedManager extends MultiLevelCacheManager implements CacheManager {
	private final Map<String, MemcachedCache> clientPool = new HashMap<String, MemcachedCache>();
	private static final long DEFAULT_IDLE = 1000 * 60 * 60 * 6;

	private String[] servers;
	private Integer[] weights;
	private int initConn = 1;
	private int minConn = 1;
	private int maxConn = 100;
	private long maxIdle = DEFAULT_IDLE;
	private int maintSleep = 30;
	private boolean nagle = false;
	private int readTimeout = 3000; // 3 secs
	private int connectTimeout = 0; // no time out

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public void setWeights(Integer[] weights) {
		this.weights = weights;
	}

	public void setInitConn(int initConn) {
		this.initConn = initConn;
	}

	public void setMinConn(int minConn) {
		this.minConn = minConn;
	}

	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	public void setMaxIdle(long maxIdle) {
		this.maxIdle = maxIdle;
	}

	public void setMaintSleep(int maintSleep) {
		this.maintSleep = maintSleep;
	}

	public void setNagle(boolean nagle) {
		this.nagle = nagle;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	@Override
	protected MultiLevelCache getCurrentLevelCache(String poolName) {
		String upperCase = poolName.toUpperCase();
		SockIOPool pool = SockIOPool.getInstance(upperCase);
		if (!pool.isInitialized()) {
			try {
				initializePool(pool);
			} catch (Exception e) {
				throw new CacheUnreachableException("创建Memcached实例失败");
			}

		}

		return initializeClient(upperCase);
	}

	public void initializePool(SockIOPool pool) throws Exception {
		Assert.notNull(servers, "Servers必须设置");
		pool.setServers(servers);
		if (weights != null) {
			pool.setWeights(weights);
		}
		pool.setInitConn(initConn);
		pool.setMinConn(minConn);
		pool.setMaxConn(maxConn);
		pool.setMaxIdle(maxIdle);

		pool.setMaintSleep(maintSleep);

		pool.setNagle(nagle);
		pool.setSocketTO(readTimeout);
		pool.setSocketConnectTO(connectTimeout);

		// initialize the connection pool
		pool.initialize();
	}

	public MemcachedCache initializeClient(String poolName) {
		MemcachedCache cache = clientPool.get(poolName);
		if (cache == null) {
			// I don't know why binary protocol is invalide in my pc, so just
			// use tcp ascii;
			MemCachedClient client = new MemCachedClient(poolName, true, false);
			cache = new MemcachedCache(client);
		}
		return cache;
	}

}
