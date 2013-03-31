package net.energy.annotation.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.energy.jdbc.Dialect;
import net.energy.utils.Page;

/**
 * SQL查询配置，用于配置查询的SQL语句，格式类似Spring的NamedSQL，
 * 
 * <pre>
 * 例1：
 * 基本SQL查询：
 * <code>@Query("select * from album where id=:id limit 1")</code>
 * <code>@MapperBy(AlbumMapper.class)</code>
 * <code>@Unique
 * public Album queryAlbumDetailById(@Param("id") long albumId);</code>
 * 调用queryAlbumDetailById("1")后将会执行：select * from album where id=? limit 1=>select * from album where id='1' limit 1
 * 
 * 例2：
 * 动态表名查询：
 * <code>@Query("select * from {0} where id=:id")</code>
 * <code>public List queryCommentByResource(@Param("id") Object resourceId, @GenericTable(index=0) String commentType);</code>
 * 调用queryCommentByResource("1","userComment")后将会执行：select * from userComment where id=?=>select * from userComment where id='1'
 * PS：小窍门，@GenericTable也可配置为字段，如:@Query("select * from {0} where {1}=:id")
 * 
 * 例3： 
 * 分页查询，只需要向Dao接口传入{@link Page}的子类，就可以实现分页： 
 * <code>@Query("select * from album where ownerId=:ownerId")</code>
 * <code>@MapperBy(AlbumMapper.class)
 * public List<Album> queryAlbums(@Param("ownerId") long userId, Page page);</code>
 * 调用 queryAlbums("1",page)（其中page.getStartIndex()=1，page.getSize()=20）后将会执行下面两段sql：
 * 1、统计查询：select count(1) from (select * from album where ownerId=?) cntTbl=>select count(1) from (select * from album where ownerId='1') cntTbl。
 * 2、select * from album where ownerId=? limit 1,20=>select * from album where ownerId='1' limit 1,20
 * 当然这是基于MySql的语法，用于也可以通过实现自己的{@link Dialect}接口来产生属于自己数据库统计SQL，和分页查询SQL
 * 
 * 采用这种调用方式的优势：
 * 其中分页时我们调用前只需要传入curpage，和size两个属性，page对象就会自动计算出起始记录偏移量等信息。
 * 调用方法后就可以从page对象中获取总记录数，总页数等信息，结合SpringMVC3.0以及自定义的分页显示tag，即可让分页和查询松散耦合。
 * 
 * </pre>
 * 
 * @author wuqh
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
	/**
	 * SQL语句
	 * 
	 * @return
	 */
	String value();

	/**
	 * 单次返回量
	 * 
	 * @return
	 */
	int fetchSize() default 40;
}
