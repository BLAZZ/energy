package net.energy.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一组批量版本更新和单个缓存删除的配置集合。 有与方法上只能放一个CacheDelete，和一个VerUpdate，
 * 但是如果需要更新或者删除的缓存对象多于1个时就需要使用CacheUpdate来配置
 * 
 * <pre>
 * 例：
 * <code>CacheUpdate(delete={@CacheDelete(key="album-d-:a.id"),@CacheDelete(key="album-d2-:a.id")},
 * 		update={@VerUpdate(vkey="ALBUM-:a.ownerId-v"),@VerUpdate(vkey="ALBUM-:a.ownerId-v2")})
 * public void updateAlbumInfo(@Param("a") Album newAlbum);</code>
 * 调用updateAlbumInfo(album)（其中album.getId()=1,album.getOwnerId()=1）后即可会将album-d-1和album-d2-1对应的缓存删除。
 * 并将ALBUM-1-v，ALBUM-1-v2（对应的版本号设置为当前时间（版本号）。
 * </pre>
 * 
 * 
 * @author wuqh
 * @see CacheDelete
 * @see VerUpdate
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheUpdate {

	/**
	 * 需要删除缓存的注解
	 * 
	 * @return
	 */
	CacheDelete[] delete() default {};

	/**
	 * 需要更新版本号的注解
	 * 
	 * @return
	 */
	VerUpdate[] update() default {};

}
