package net.energy.annotation.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 更新SQL配置 ，用于配置批量更新的SQL语句，格式类似Spring的NamedSQL：
 * 
 * <pre>
 * 例：
 * <code>@Update("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
 * <code>@ReturnId
 * public long insertPhotoReturnId(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
 * 这样调用insertPhotosReturnIds(user,album,photos)时（其中user.getId()=1,album.getId()=1,photo.getFile()分别为f1,f2）。
 * 相当于执行SQL批量更新：insert into photo(ownerId, albumId, file) values (?,?,?)=>insert into photo(ownerId, albumId, file) values ('1','1','f1')
 * 而参数就是这样两批：[1,1,f1
 * 
 * 当然，BatchUpdate也支持动态表查询，具体参考{@link Query}中的例2，动态表名查询
 * </pre>
 * 
 * @author wuqh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Update {
	/**
	 * SQL语句
	 * 
	 * @return
	 */
	String value();
}
