package net.energy.jdbc.impl;

import net.energy.queue.TaskParameters;

/**
 * JDBC查询任务参数集
 * 
 * @author wuqh
 * @see TaskParameters
 */
public class JdbcQueryTaskParameters implements TaskParameters {
	private final String sql;
	private final int fetchSize;
	private final boolean returnKeys;
	private final PreparedStatementCallback<?> action;
	private final Object[] args;
	private final int hashCode;

	public JdbcQueryTaskParameters(String sql, int fetchSize, boolean returnKeys, PreparedStatementCallback<?> action,
			Object... args) {
		int hashCode = 0;
		
		this.sql = sql;
		hashCode += sql.hashCode();
		
		this.fetchSize = fetchSize;
		hashCode += fetchSize;
		
		this.returnKeys = returnKeys;
		hashCode +=  (returnKeys ? 1231 : 1237);
		
		this.action = action;
		
		this.args = args;
		
		if(args != null) {
			for(Object arg : args) {
				hashCode +=  (arg == null? 0 : arg.hashCode());
			}
		}
		
		this.hashCode = 17 + 23 * hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		if(obj instanceof JdbcQueryTaskParameters == false){
			return false;
		}
		
		if (obj == this) {
			return true;
		}

		JdbcQueryTaskParameters parameters = (JdbcQueryTaskParameters) obj;

		boolean equals = (sql.equals(parameters.getSql()) && fetchSize == parameters.getFetchSize() && returnKeys == parameters
				.isReturnKeys());

		if (equals) {
			Object[] paramArgs = parameters.getArgs();
			if (args == null && paramArgs == null) {
				return true;
			}

			int currentLen = (args == null ? 0 : args.length);
			int paramLen = (paramArgs == null ? 0 : paramArgs.length);

			if (currentLen == paramLen) {
				for (int i = 0; i < currentLen; i++) {
					Object arg = args[i];
					Object paramArg = paramArgs[i];
					if ((arg == null && paramArg == null) || (arg != null && arg.equals(paramArg))) {
						continue;
					}

					return false;
				}

				return true;
			}

		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	public String getSql() {
		return sql;
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public boolean isReturnKeys() {
		return returnKeys;
	}

	public PreparedStatementCallback<?> getAction() {
		return action;
	}

	public Object[] getArgs() {
		return args;
	}

}
