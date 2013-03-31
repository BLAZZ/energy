package net.energy.queue;

/**
 * 任务结果监听器，用于队列中首个任务执行完成后通知队列中的其他任务
 * 
 * @author wuqh
 *
 */
public class TaskDoneListener {
	private final Task task;

	public TaskDoneListener(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	public <T> void taskDone(TaskResult<T> result) {
		synchronized (task) {
			task.setTaskResult(result);
			task.notifyAll();
		}
	}
}
