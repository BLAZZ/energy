package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 更新操作的MongoShell配置 ，用于配置更新的类Shell语句：
 * 
 * <pre>
 * 例：
 * <code>@MongoUpdate(query="{'id'::user.id}",modifier="{$set:{'nickname'::user.nickname}}")
 * @MongoCollection("user")
 * public void updateUser(@Param("user") User user);</code>
 * 调用updateUser(u1)（u1.id=1L,u1.nickname='a'）后将会执行：user.update({{'id':1L}},{$set:{'nickname':'a'}},false,true)
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoUpdate {
	/**
	 * 查询Shell语句
	 * 
	 * @return
	 */
	String query();

	/**
	 * 更新Shell语句
	 * 
	 * @return
	 */
	String modifier();

	/**
	 * 不存在时是否插入
	 * 
	 * @return
	 */
	boolean upsert() default false;

	/**
	 * 是否更新多条
	 * 
	 * @return
	 */
	boolean multi() default true;
}
