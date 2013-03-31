package net.energy.queue;

/**
 * 任务直销结果，包含是否完成信息，以及方法执行的返回值
 * 
 * @author wuqh
 *
 * @param <T>
 */
public class TaskResult<T> {
	public T result;
	public boolean done;

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	
}
