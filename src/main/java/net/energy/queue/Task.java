package net.energy.queue;

/**
 * 队列中的任务，是需要处理事件的一次执行
 * 
 * @author wuqh
 * 
 */
public abstract class Task {
	@SuppressWarnings("rawtypes")
	private TaskResult taskResult;

	/**
	 * 执行任务
	 * 
	 * @param <T>
	 * @param parameters
	 * @return
	 */
	public abstract <T> TaskResult<T> process(TaskParameters parameters);

	/**
	 * 设置执行结果
	 * 
	 * @param <T>
	 * @param taskResult
	 */
	public <T> void setTaskResult(TaskResult<T> taskResult) {
		this.taskResult = taskResult;
	}

	/**
	 * 获取执行结果
	 * 
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> TaskResult<T> getTaskResult() {
		return this.taskResult;
	}
}
