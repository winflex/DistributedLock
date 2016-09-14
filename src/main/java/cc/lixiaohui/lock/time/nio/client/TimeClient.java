package cc.lixiaohui.lock.time.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 时间获取客户端
 * @author lixiaohui
 *
 */
public class TimeClient {
	
	private static final String TIME_CMD = "time";
	
	private final SocketAddress address;
	
	private SocketChannel channel;
	
	public TimeClient(SocketAddress address) throws IOException {
		this.address = address;
		channel = SocketChannel.open(address);
		channel.configureBlocking(true); // blocking mode
	}
	
	/**
	 * @throws TimeClientException when connection with time server is closed.
	 * @return currentTimeMillis in server
	 */
	public long currentTimeMillis() {
		try {
			channel.write(ByteBuffer.wrap(TIME_CMD.getBytes()));
			
			ByteBuffer buf = ByteBuffer.allocate(64);
			channel.read(buf);
			
			buf.flip(); // flip for use of read
			byte[] bytes = new byte[buf.limit() - buf.position()];
			System.arraycopy(buf.array(), buf.position(), bytes, 0, bytes.length);
			
			return Long.parseLong(new String(bytes));
		} catch(NumberFormatException e) {
			System.err.println(e);
			return System.currentTimeMillis();
		} catch (IOException e) {
			throw new TimeClientException(address);
		}
	}
	
	/**
	 * close the client, along with its connection with server.
	 */
	public void close() {
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		TimeClient client = new TimeClient(new InetSocketAddress("localhost", 9999));
		System.out.println(client.currentTimeMillis());
		//client.close();
		System.in.read();
	}

	
}
