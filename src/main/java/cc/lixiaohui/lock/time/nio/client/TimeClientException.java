package cc.lixiaohui.lock.time.nio.client;

import java.net.SocketAddress;

public class TimeClientException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TimeClientException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TimeClientException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public TimeClientException(SocketAddress address) {
		super(address.toString());
	}
	
}
