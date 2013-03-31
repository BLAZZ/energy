package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 删除的MongoShell配置 ，用于配置查询的类Shell语句：
 * 
 * <pre>
 * 例：
 * <code>@MongoRemove("{id::id}")
 * <code>	@MongoCollection("user")
 * public void removeUser(@Param("id") Long id);</code>
 * 调用removeUser(1L)后将会执行：user.remove({'id':1L})
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoRemove {
	/**
	 * Shell语句
	 * 
	 * @return
	 */
	String value();
}
