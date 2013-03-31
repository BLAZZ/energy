package net.energy.mongo;

import java.util.List;

import net.energy.utils.Page;

/**
 * MongoDB数据访问
 * 
 * @author wuqh
 *
 */
public interface MongoDataAccessor {
	/**
	 * 通过shell插入一个文档
	 * 
	 * @param collectionName
	 * @param shell
	 * @return
	 */
	boolean insert(String collectionName, MongoShell shell);
	
	/**
	 * 通过shell插入一组文档
	 * 
	 * @param collectionName
	 * @param shell
	 * @param argsList
	 * @return
	 */
	boolean batchInsert(String collectionName, String shell, List<Object[]> argsList);

	/**
	 * 通过shell删除一个文档
	 * 
	 * @param collectionName
	 * @param shell
	 * @return
	 */
	boolean remove(String collectionName, MongoShell shell);
	
	/**
	 * 通过shell更新一个文档
	 * 
	 * @param collectionName
	 * @param query
	 * @param update
	 * @return
	 */
	boolean update(String collectionName, MongoShell query, MongoUpdate update);

	/**
	 * 通过shell统计文档中的记录数
	 * 
	 * @param collectionName
	 * @param shell
	 * @return
	 */
	long count(String collectionName, MongoShell shell);

	/**
	 * 通过shell分页查询文档中的记录
	 * 
	 * @param <T>
	 * @param collectionName
	 * @param query
	 * @param mapper
	 * @param page
	 * @return
	 */
	<T> List<T> findPage(String collectionName, MongoQuery query, BeanMapper<T> mapper, Page page);

	/**
	 * 通过shell查询文档中的记录
	 * 
	 * @param <T>
	 * @param collectionName
	 * @param query
	 * @param mapper
	 * @return
	 */
	<T> List<T> find(String collectionName, MongoQuery query, BeanMapper<T> mapper);
}
