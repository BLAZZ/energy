package net.energy.cache;

import net.energy.exception.CacheUnreachableException;

/**
 * Cache操作中产生的异常处理类
 * 
 * @author wuqh
 * 
 */
public class CacheErrorHandler {
	/**
	 * 处理操作中抛出的异常
	 * 
	 * @param e
	 */
	public static void handleError(Throwable e) {
		throw new CacheUnreachableException(e);
	}
}
