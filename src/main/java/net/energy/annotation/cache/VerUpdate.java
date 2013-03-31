package net.energy.annotation.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存版本更新配置，缓存框架根据vkey值的pattern在执行时拼装出当前版本号在缓存中的key值，并更新此版本号为最新的版本。
 * 
 * <pre>
 * 例：
 * <code>@VerUpdate(vkey="ALBUM-:a.ownerId-v")
 * public void updateAlbumInfo(@Param("a") Album newAlbum);</code>
 * 调用updateAlbumInfo(album)（其中album.getOwnerId()=1）后即可会将ALBUM-1-v对应的版本号设置为当前时间（版本号）。
 * </pre>
 * 
 * @author wuqh
 * 
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerUpdate {
	/**
	 * 缓存池名
	 * 
	 * @return
	 */
	String pool() default "DEFAULT_CACHE";

	/**
	 * 更新的版本键
	 * 
	 * @return
	 */
	String vkey();

	/**
	 * 版本号缓存过期时间
	 * 
	 * @return
	 */
	long expire() default 1800000L; // 30min=1000*60*30
}
