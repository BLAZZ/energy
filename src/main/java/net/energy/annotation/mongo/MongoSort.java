package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置Mongo查询操作对应的sort的值。value为BSON，例如"{createTime: -1,user:1}"
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSort {
	/**
	 * Mongo查询Shell中的sort值，是一个符合BSON格式的String，例如"{createTime: -1,user:1}"
	 * 
	 * @return
	 */
	String value();
}
