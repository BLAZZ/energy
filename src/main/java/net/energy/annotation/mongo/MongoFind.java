package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 查询的MongoShell配置 ，用于配置查询的类Shell语句：
 * 
 * <pre>
 * 例：
 * <code>@MongoFind("{id::id}")</code>
 * <code>@MongoMapper(UserBeanMapper.class)</code>
 * <code>@MongoCollection("user")</code>
 * <code>@MongoSort("{date:-1}")</code>
 * <code>public List<User> getUsers(@Param("id") Long id, @MongoSkip int skip);</code>
 * 调用getUsers(1L,20)后将会执行：user.find({'id':1L}).sort({date:-1}).skip(20)
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoFind {
	/**
	 * Shell语句
	 * 
	 * @return
	 */
	String value();

	/**
	 * Mongo查询中对应batchsize
	 * 
	 * @return
	 */
	int batchSize() default 50;
}
