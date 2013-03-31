package net.energy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于产生SQL，MongoShell，Cache的key值，并且可以放于primary，能调用toString()的对象，标准Bean上：
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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
	/**
	 * 
	 * @return
	 */
	String value();
}
