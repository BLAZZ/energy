package net.energy.exception;

/**
 * JDBC数据访问异常
 * 
 * @author wuqh
 * 
 */
public class JdbcDataAccessException extends DataAccessException {
	private static final long serialVersionUID = 1173641670975074781L;

	public JdbcDataAccessException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public JdbcDataAccessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public JdbcDataAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public JdbcDataAccessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
