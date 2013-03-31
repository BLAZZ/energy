package net.energy.annotation.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 批量更新SQL配置 ，用于配置批量更新的SQL语句，格式类似Spring的NamedSQL：
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
 * 注意：
 * 1、由于Java对于泛型的类型擦除，导致无法支持将@BatchParam配置于集合类上，所以暂时只支持将@BatchParam配置于数组之上
 * 2、而对于配置了@ReturnId的返回值没有过多限制，只要是Number子类的List，或者Number子类、基本类型的数组都是可以的。
 * 
 * 当然，BatchUpdate也支持动态表查询，具体参考{@link Query}中的例2，动态表名查询
 * </pre>
 * 
 * @author wuqh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchUpdate {
	/**
	 * SQL语句
	 * 
	 * @return
	 */
	String value();
}
