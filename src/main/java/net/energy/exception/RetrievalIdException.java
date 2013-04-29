package net.energy.exception;

/**
 * 插入记录后，获取插入数据的ID时可能会抛出此异常
 * 
 * @author wuqh
 * 
 */
public class RetrievalIdException extends DataAccessException {
	private static final long serialVersionUID = -7892294811658168650L;

	public RetrievalIdException() {
		super();
		
	}

	public RetrievalIdException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public RetrievalIdException(String message) {
		super(message);
		
	}

	public RetrievalIdException(Throwable cause) {
		super(cause);
		
	}

}
