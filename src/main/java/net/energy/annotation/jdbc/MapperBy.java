package net.energy.annotation.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.energy.jdbc.RowMapper;
import net.energy.jdbc.impl.AutoDetectRowMapper;

/**
 * ORM映射接口配置，这个和SpringJdbcTemplate中使用的RowMapper是一样的，不解释。
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperBy {
	/**
	 * 映射接口类（必须包含无参构造方法）
	 * 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends RowMapper> value() default AutoDetectRowMapper.class;
}
