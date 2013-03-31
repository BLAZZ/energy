package net.energy.exception;

/**
 * MongoDB数据访问异常
 * 
 * @author wuqh
 * 
 */
public class MongoDataAccessException extends DataAccessException {
	private static final long serialVersionUID = 8178116413686990095L;

	public MongoDataAccessException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MongoDataAccessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public MongoDataAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public MongoDataAccessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
