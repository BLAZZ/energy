package net.energy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于产生批量SQL，MongoShell，的key值。
 * 
 * <pre>
 * 例：
 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
 * <code>@ReturnId
 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
 * 这样调用insertPhotosReturnIds(user,album,photos)时（其中user.getId()=1,album.getId()=1,photo.getFile()分别为f1,f2）。
 * 相当于执行SQL批量更新：insert into photo(ownerId, albumId, file) values (?,?,?)
 * 而参数就是这样两批：['1','1','f1']和['1','1','f2']
 * 
 * 在@MongoBatchInsert中用法相同
 * 
 * 注意：
 * 由于Java对于泛型的类型擦除，导致无法支持将@BatchParam配置于集合类上，所以暂时只支持将@BatchParam配置于数组之上
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchParam {
	/**
	 * 
	 * @return
	 */
	String value();
}
