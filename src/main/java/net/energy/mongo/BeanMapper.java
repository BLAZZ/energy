package net.energy.mongo;

import com.mongodb.DBObject;

/**
 * 和RowMapper类似的接口，用于MongoDB的ORM映射
 * 
 * @author wuqh
 *
 * @param <T>
 */
public interface BeanMapper<T> {
	/**
	 * ORM映射方法
	 * 
	 * @param o
	 * @return
	 */
	T mapper(DBObject o);
}
