package net.energy.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.energy.utils.Page;

/**
 * 缓存配置，主要用于配置缓存的key值pattern和缓存所属版本的key值pattern：
 * 
 * <pre>
 * 1、缓存框架根据key值的pattern在执行时拼装出缓存的key值，并通过此key值获取缓存；
 * 2、缓存框架根据vkey值的pattern在执行时拼装出当前版本号在缓存中的key值，用于和取出缓存的版本号进行比较；
 * 3、需要通过expire最长配置有效时间,此时长会同样作用于版本号的对象；
 * 
 * 例1:
 * 简单的key值生成：
 * <code>@Cache(key="album-d-:id")
 * public Album queryAlbumDetailById(@Param("id") long albumId);</code>
 * 调用queryAlbumDetailById("1")后即可将结果存放于album-d-1对应的缓存中。
 * 
 * 例2：
 * 基于Bean的key值生成：
 * <code>@Cache(key="album-d-:album.id")
 * public Album queryAlbumDetailById(@Param("album") Album album);</code>
 * 调用queryAlbumDetailById(album)（其中album.getId()=1）后即可将结果存放于album-d-1对应的缓存中。
 * 
 * 例3：
 * 集合的vkey生成：
 * <code>@Cache(key="photo-l-d-album:albumId", vkey="PHOTO-:albumId-v")</code>
 * public List<Photo> queryPhotos(@Param("albumId") long albumId);
 * 调用queryPhotos("1")后即可将结果存放于photo-l-d-album1对应的缓存中。
 * 并且将PHOTO-1-v对应版本值，作为此集合版本，每次查询后都会将此集合对应的版本号，和PHOTO-1-v对应的值（当前版本）做比较，如果一致则认为缓存有效
 * 
 * 注意:如果返回为集合类型，必须配置vkey。
 * 
 * 
 * 例4:
 * 单个对象的版本值：
 * <code>@Cache(key="photo-latest-album:albumId", vkey="PHOTO-:result.albumId-v")
 * public Photo queryLatestPhotoOfAlbum(@Param("albumId") long albumId);</code>
 * 调用queryLatestPhotoOfAlbum("1")后即可将结果存放于photo-latest-album1对应的缓存中。
 * 并且将PHOTO-1-v对应版本值（如果结果photo.getAlbumId()=1），作为此对象的版本，每次查询后都会将此对象对应的版本号和PHOTO-1-v对应的值（当前版本）做比较，如果一致则认为缓存有效
 * 从例4中可见，单个对象的缓存比较特殊，vkey有且只能有一个变量，那就是关键字“result”，它来指向返回值。就像在返回结果上加了一个@Param("result")
 * 
 * 支持分页数据的缓存：
 * 只需要向Dao接口传入{@link Page}的子类，并设置每页记录数{@link Page#setSize(int)}，当前页码（从1开始，默认为1）{@link Page#setCurpage(int)}，就可以实现分页
 * 对于分页的集合系统会自动在原有的key值后面加上-pN用于区分页码，比如：解析后的key为photo-l-d-album1，而又是第2页，则实际key值将会是：photo-l-d-album1-p2
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
	/**
	 * 缓存池名
	 * 
	 * @return
	 */
	String pool() default "DEFAULT_CACHE";

	/**
	 * 缓存键
	 * 
	 * @return
	 */
	String key();

	/**
	 * 缓存过期时间
	 * 
	 * @return
	 */
	long expire() default 1800000L; // 30min=1000*60*30

	/**
	 * 版本号键
	 * 
	 * @return
	 */
	String vkey() default "";
}
