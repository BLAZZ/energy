package net.energy.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 删除单个缓存配置，缓存框架根据key值的pattern在执行时拼装出缓存的key值，并通过此key值删除指定缓存。
 * 
 * <pre>
 * 例：
 * <code>@CacheDelete(key="album-d-:a.id")
 * public void updateAlbumInfo(@Param("a") Album newAlbum);</code>
 * 调用updateAlbumInfo(album)（其中album.getId()=1）后即可会将album-d-1对应的缓存删除。
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheDelete {
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

}
