package cc.lixiaohui.lock.time.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 提供简单时间服务的服务器
 * 
 * @author lixiaohui
 */
public class TimeServer {
	
	public static void main(String[] args) throws IOException {
		new TimeServer().start(9999);
	}

	private ServerSocketChannel serverChannel;

	private Selector selector;

	private volatile boolean alive = true;

	private static final String TIME_CMD = "time";
	private static final String HALT_CMD = "halt";

	private static final String ERROR = "error";

	private static final Logger logger = LoggerFactory.getLogger(TimeServer.class);

	public void start(int port) throws IOException {
		selector = Selector.open();

		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false); // non-blocking mode
		serverChannel.bind(new InetSocketAddress(port));

		// interested only in accept event
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (alive) {
			try {
				if (selector.select() < 0) { // no events
					continue;
				}
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					try {
						if (!key.isValid()) 
							continue;
						
						if (key.isAcceptable()) { // new channel incoming
							SocketChannel ch = ((ServerSocketChannel) key.channel()).accept();
							// ignore if register failed
							if (!registerChannel(selector, ch, SelectionKey.OP_READ)) {
								continue;
							}
							logger.info("new channel registered {}", ch.getRemoteAddress().toString());
						}
						// client request
						if (key.isReadable()) {
							handleRead(key);
						}
						if (key.isWritable()) {
							handleWrite(key);
						}
					} catch (IOException e) {
						logger.error("{} exception: {}", key.channel(), e);
						if (key != null) {
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("{}", e);
			}
		}
		
		if (selector != null) {
			try {
			selector.close();
			} catch (Exception e) {
				logger.error("error occurred when closing selector: e", e);
			}
		}
	}

	private void handleWrite(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		try {
			ByteBuffer buf = (ByteBuffer) key.attachment();
			if (buf != null) {
				writeBytesToChannel(ch, buf, key);
			}
		} catch (ClassCastException e) {
			logger.error("{}", e);
		}
	}

	private void handleRead(SelectionKey key) throws IOException {
		SocketChannel ch = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(16);
		int read = ch.read(buffer);
		if (read < 4) { // not a full command, write error back,
						// meaning client will send command
						// again.
			writeBytesToChannel(ch, ERROR.getBytes(), key);
		} else {
			String cmd = extractCommand(buffer);
			logger.info("recieve {} request from {}", cmd, ch.getRemoteAddress().toString());
			if (TIME_CMD.equalsIgnoreCase(cmd)) {
				// 回写时间
				writeBytesToChannel(ch, String.valueOf(time()).getBytes(), key);
				logger.info("write time to {}", ch.getRemoteAddress().toString());
			} else if (HALT_CMD.equalsIgnoreCase(cmd)) {
				// 停止服务
				logger.info("stopping timeserver");
				stop();
				logger.info("timeserver stopped");
			} else {
				writeBytesToChannel(ch, ERROR.getBytes(), key);
				logger.warn("unreconized command {}, will discard it.", cmd);
			}
		}
	}

	private String extractCommand(ByteBuffer buffer) {
		buffer.flip();
		byte[] array = buffer.array();
		byte[] newArray = new byte[buffer.remaining()];
		System.arraycopy(array, buffer.position(), newArray, 0, buffer.remaining());
		return new String(newArray);
	}

	private void writeBytesToChannel(SocketChannel ch, byte[] bs, SelectionKey key) throws IOException {
		ByteBuffer buf = ByteBuffer.wrap(bs);
		int total = buf.remaining();
		int write = ch.write(buf);
		if (write < total) { // didn't wrote all, then write rest when next
								// event triggered
			key.attach(buf);
		}
	}

	private void writeBytesToChannel(SocketChannel ch, ByteBuffer buf, SelectionKey key) throws IOException {
		if (!buf.hasRemaining()) {
			return;
		}
		int total = buf.remaining();
		int write = ch.write(buf);
		if (write < total) { // didn't wrote all, then write rest when next
								// event triggered
			key.attach(buf);
		}
	}

	protected void stop() {
		alive = false;
		try {
			serverChannel.close();
			selector.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean registerChannel(Selector sel, SocketChannel sc, int ops) {
		try {
			sc.configureBlocking(false);
			sc.register(sel, ops);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private long time() {
		return System.currentTimeMillis();
	}

}
