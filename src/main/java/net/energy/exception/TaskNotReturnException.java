package net.energy.exception;

/**
 * 队列任务未正常返回时抛出此异常
 * 
 * @author wuqh
 *
 */
public class TaskNotReturnException extends RuntimeException {
	private static final long serialVersionUID = -5775094087402819132L;

	public TaskNotReturnException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TaskNotReturnException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public TaskNotReturnException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public TaskNotReturnException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
