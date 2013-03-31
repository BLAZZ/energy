package net.energy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于产生批量SQL，的动态表名，此配置只能加于String类型之上，暂时只支持JDBC
 * 
 * <pre>
 * 例：
 * <code>@Query("select * from {0} where id=:id")</code>
 * <code>public List queryCommentByResource(@Param("id") Object resourceId, @GenericTable(index=0) String commentType);</code>
 * 调用queryCommentByResource("1","userComment")后将会执行：select * from userComment where id=?
 * </pre>
 * 
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenericTable {
	/**
	 * 
	 * @return
	 */
	int index() default 0;
}
