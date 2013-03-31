package net.energy.jdbc;

import java.util.List;
import java.util.Map;

/**
 * 主键的容器，在@ReturnId注解存在时使用
 * 
 * @author wuqh
 * 
 */
public interface KeyHolder {
	/**
	 * 获取单个ID，这个主键是一个单一组件
	 * 
	 * @return
	 */
	Number getKey();

	/**
	 * 获取单个ID,由于可能是联合主键，所以是一个Map，key为columnName，value为值
	 * 
	 * @return
	 */
	Map<String, Object> getKeys();

	/**
	 * 插入多行数据时，返回多行主键。每个组件都可能是一个Map，key为columnName，value为值
	 * 
	 * @return
	 */
	List<Map<String, Object>> getKeyList();
}
