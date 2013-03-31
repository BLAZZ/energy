package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 插入的MongoShell配置 ，用于配置插入的类Shell语句：
 * 
 * <pre>
 * 例：
 * <code>@MongoInsert("{id::user.id}")
 * 	public void insertUser(@Param("user") User user, @MongoCollection() String collection);</code>
 * 调用insertUser(u1)（u1.id=1L）后将会执行：user.insert({'id':1L})
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoInsert {
	/**
	 * Shell语句
	 * 
	 * @return
	 */
	String value();
}
