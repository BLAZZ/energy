package net.energy.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.energy.exception.TaskNotReturnException;
import net.energy.utils.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务仓库，存放所有的任务队列
 * 
 * @author wuqh
 * 
 */
public class TaskRepository {
	private static final Map<TaskParameters, List<TaskDoneListener>> tasks = new ConcurrentHashMap<TaskParameters, List<TaskDoneListener>>();

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRepository.class);

	/**
	 * 根据任务参数集，将任务增加到其应当在的那个队列中
	 * 
	 * @param task
	 * @param taskParameters
	 */
	public static void addTask(Task task, TaskParameters taskParameters) {
		List<TaskDoneListener> listeners = getTaskDoneListeners(taskParameters);

		listeners = initListeners(listeners, taskParameters);

		synchronized (listeners) {
			listeners.add(new TaskDoneListener(task));
		}
	}

	/**
	 * 执行队列中的任务。 任务执行过程为判断任务是否为队列中的第一个任务，如果是则执行任务的process方法，执行完后将结果通知队列中的其他任务。
	 * 如果不是第一个任务则等待第一个任务的执行结果，他本身并不执行。
	 * 
	 * @param <T>
	 * @param task
	 * @param taskParameters
	 * @param timeout
	 *            任务执行超时时间
	 * @return
	 */
	public static <T> T processTask(Task task, TaskParameters taskParameters, long timeout) {
		boolean isFirst = isFirstTask(task, taskParameters);
		TaskResult<T> result = null;

		if (isFirst) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Thread" + Thread.currentThread().getId() + ":当前线程是任务队列的第一个任务，将会被执行");
			}
			result = task.process(taskParameters);
			List<TaskDoneListener> currentListeners = getTaskDoneListeners(taskParameters);

			Assert.notEmpty(currentListeners, "获取任务队列失败，无法将任务完成的消息通知到队列中的其他任务");

			synchronized (currentListeners) {
				for (TaskDoneListener listener : currentListeners) {
					listener.taskDone(result);
				}
				clearListeners(taskParameters);
			}

		} else {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("Thread" + Thread.currentThread().getId() + ":当前线程不是任务队列的第一个任务，开始等待一个任务处理结果");
			}
			try {
				synchronized (task) {
					if (timeout > 0) {
						task.wait(timeout);
					} else {
						task.wait();
					}
				}
			} catch (InterruptedException e) {
				LOGGER.debug("等待时遇到InterruptedException", e);
			}
		}

		TaskResult<T> finalResult = task.getTaskResult();
		if (finalResult == null || !finalResult.isDone()) {
			throw new TaskNotReturnException("任务[" + task.toString() + "]结束但没有返回任何结果");
		}

		return finalResult.getResult();
	}

	/**
	 * 初始化任务队列
	 * 
	 * @param list
	 * @param taskParameters
	 * @return
	 */
	private static synchronized List<TaskDoneListener> initListeners(List<TaskDoneListener> list,
			TaskParameters taskParameters) {
		if (list == null) {
			list = new ArrayList<TaskDoneListener>();
			tasks.put(taskParameters, list);
		}

		return list;
	}

	/**
	 * 清空任务队列
	 * 
	 * @param taskParameters
	 */
	private static void clearListeners(TaskParameters taskParameters) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thread" + Thread.currentThread().getId() + ":清空任务队列");
		}
		tasks.remove(taskParameters);
	}

	/**
	 * 判断当前任务是否为队列中的第一个任务
	 * 
	 * @param task
	 * @param taskParameters
	 * @return
	 */
	private static boolean isFirstTask(Task task, TaskParameters taskParameters) {
		List<TaskDoneListener> listeners = getTaskDoneListeners(taskParameters);

		Assert.notEmpty(listeners, "listener为空，无法判断是否为队列的第一个任务");

		TaskDoneListener listener = listeners.get(0);

		return (task == listener.getTask());
	}

	/**
	 * 根据参数集获取任务队列
	 * 
	 * @param taskParameters
	 * @return
	 */
	private static List<TaskDoneListener> getTaskDoneListeners(TaskParameters taskParameters) {
		return tasks.get(taskParameters);
	}

	// public static class P implements TaskParameters {
	// private final int j;
	//
	// public P(int j) {
	// this.j = (j % 10);
	// }
	//
	// public int getJ() {
	// return j;
	// }
	//
	// @Override
	// public boolean equals(Object obj) {
	// P p = (P) obj;
	// return (p != null && p.getJ() == j);
	// }
	//
	// @Override
	// public int hashCode() {
	// return new Integer(j).hashCode();
	// }
	//
	// }
	//
	// public static class T implements Task {
	// private TaskResult result;
	//
	// @Override
	// public TaskResult process(TaskParameters parameters) {
	// P p = (P) parameters;
	// try {
	// Thread.sleep(6000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// result = new TaskResult();
	// result.setDone(true);
	// result.setResult(p.getJ());
	//
	// return result;
	// }
	//
	// @Override
	// public void setTaskResult(TaskResult taskResult) {
	// this.result = taskResult;
	// }
	//
	// @Override
	// public TaskResult getTaskResult() {
	// return this.result;
	// }
	//
	// }
	//
	// public static class C {
	// private int i = 0;
	//
	// public synchronized void add() {
	// i++;
	// }
	//
	// public int getI() {
	// return i;
	// }
	// }
	//
	// public static void main(String... args) throws InterruptedException {
	// final C c = new C();
	// final List<Object> results = new ArrayList<Object>();
	//
	// long start = System.currentTimeMillis();
	//
	// for (int i = 0; i < 1000; i++) {
	// final int j = i;
	// Thread th = new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// long start = System.currentTimeMillis();
	//
	// Task task = new T();
	// TaskParameters p = new P(j);
	//
	// addTask(task, p);
	//
	// Object result = processTask(task, p, 0);
	//
	// synchronized (results) {
	// results.add(result);
	// }
	//
	// long end = System.currentTimeMillis();
	//
	// System.out.println("Thread" + Thread.currentThread().getId() + ":" +
	// result + ". process end in "
	// + (end - start) + "ms");
	//
	// c.add();
	// }
	// });
	// th.start();
	// }
	//
	//
	// while(c.getI() != 1000) {
	// System.out.println("C:"+c.getI() + ".result size:"+results.size());
	// Thread.sleep(1);
	// }
	//
	// long end = System.currentTimeMillis();
	// System.out.println("total process end in " + (end - start) + "ms");
	// }

}
