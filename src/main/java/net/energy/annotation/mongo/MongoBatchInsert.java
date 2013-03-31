package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 批量插入的MongoShell配置 ，用于配置批量插入的类Shell语句：
 * 
 * <pre>
 * 例：
 * <code>@MongoBatchInsert("{id::user.id}")
 * public void insertUsers(@BatchParam("user") User[] user, @MongoCollection() String collection);</code>
 * 调用insertUsers(new User[]{u1,u2})（u1.id=1L,u2.id=2L）后将会执行：user.insert([{'id':1L},{'id':1L}])
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoBatchInsert {
	/**
	 * Shell语句
	 * 
	 * @return
	 */
	String value();
}
