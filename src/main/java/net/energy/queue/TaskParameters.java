package net.energy.queue;

/**
 * 任务参数集,必须重写Object的hashCode和equals方法
 * 
 * @author wuqh
 *
 */
public interface TaskParameters {
	public int hashCode();
	
	public boolean equals(Object obj);
}
