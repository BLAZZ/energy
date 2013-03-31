package net.energy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果方法增加了此配置，表示方法只返回一条记录，否则必须返回List
 * <pre>
 * 例如：
 * <code>@Query("select * from album where id=:id limit 1")</code>
 * <code>@MapperBy(AlbumMapper.class)</code>
 * <code>@Unique
 * public Album queryAlbumDetailById(@Param("id") long albumId);</code>
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
}
