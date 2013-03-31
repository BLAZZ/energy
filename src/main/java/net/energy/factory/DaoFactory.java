package net.energy.factory;

/**
 * DAO工厂接口
 * 
 * @author wuqh
 * 
 */
public interface DaoFactory {
	/**
	 * 获取DAO实例
	 * 
	 * @param <T>
	 *            DAO的接口类
	 * @param clazz
	 * @return
	 */
	<T> T createDao(Class<T> clazz);

}
