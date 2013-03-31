package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * MongoDB的count操作配置。
 * 
 * <pre>
 * 例：
 * <code>@MongoCount("{'id':{$gte::id}}")</code>
 * <code>@MongoCollection("user")
 * public int countUser(@Param("id") Long id);</code>
 * 调用countUser(1L)后将会执行：user.count({'id':{$gte:1L}})
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoCount {
	/**
	 * Shell语句
	 * 
	 * @return
	 */
	String value();
}
