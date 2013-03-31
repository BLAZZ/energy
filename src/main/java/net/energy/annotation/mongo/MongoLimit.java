package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置Mongo查询操作对应的limit的值。如果用于Method上value必须填，如果用于parameter上即取value值
 * 
 * @author wuqh
 * 
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoLimit {
	/**
	 * Mongo查询Shell中的limit值
	 * 
	 * @return
	 */
	int value() default -1;
}
