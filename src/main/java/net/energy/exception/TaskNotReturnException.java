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
		
	}

	public TaskNotReturnException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public TaskNotReturnException(String message) {
		super(message);
		
	}

	public TaskNotReturnException(Throwable cause) {
		super(cause);
		
	}

}
