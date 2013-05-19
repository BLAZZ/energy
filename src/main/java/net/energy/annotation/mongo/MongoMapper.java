package net.energy.annotation.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.energy.mongo.BeanMapper;
import net.energy.mongo.impl.AutoDetectBeanMapper;

/**
 * ORM映射接口配置，这个和SpringJdbcTemplate中使用的RowMapper是类似的，不解释。
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoMapper {
	/**
	 * 映射接口类（必须包含无参构造方法）
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends BeanMapper> value() default AutoDetectBeanMapper.class;
}
