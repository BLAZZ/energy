package net.energy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果方法增加了此配置，表示方法只返回值为一个ID，当前仅JDBC支持而且不支持联合主键，且返回值必须为Number子类，
 * 或者符合此规则的Primitive值
 * 
 * <pre>
 * 例：
 * <code>@Update("insert into album(albumName,ownerId) values (:a.albumName,:a.ownerId)")</code>
 * <code>@ReturnId
 * public long insertAlbumReturnId(@Param("a") Album album);</code>
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReturnId {
}
