package cc.lixiaohui.DistributedLock.DistributedLock;

import io.lock.Lock;
import io.lock.example.IDGenerator;
import io.lock.redis.RedisReentrantLock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class IDGeneratorTest {
	
	static Set<String> generatedIds = new HashSet<String>();
	
	static final String HOST = "localhost";
	static final int PORT = 6379;
	static final String LOCK_KEY = "lock.lock";
	static final long LOCK_EXPIRE = 5 * 1000;
	static final SocketAddress ADDR = new InetSocketAddress("localhost", 9999);
	
	static final long RUN_TIME = 20 * 1000;
	
	private static final Logger logger = LoggerFactory.getLogger(IDGeneratorTest.class);
	
	@Test
	public void testReentrant() throws Exception {
		// create (availableProcessors + 1) threads to consume id.
		int count = Runtime.getRuntime().availableProcessors() + 1;
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < count; i++) {
			Jedis jedis = new Jedis(HOST, PORT);
			Lock lock = new RedisReentrantLock(jedis, LOCK_KEY, LOCK_EXPIRE);
			IDGenerator generator = new IDGenerator(lock);
			IDConsumeTask consumer = new IDConsumeTask(generator, "consumer" + i);
			Thread thread = new Thread(consumer);
			threads.add(thread);
		}
		
		// start all threads created.
		for (Thread t : threads) {
			t.start();
		}
		
		Thread.sleep(RUN_TIME); // run for a specified period of time
		
		IDConsumeTask.stopAll(); // tell all threads to stop
		
		// wait for all threads to finish.
		for (Thread t : threads) {
			t.join();
		}
	}
	
	static String time() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}
	
	static class IDConsumeTask implements Runnable {

		private IDGenerator idGenerator;
		
		private String name;
		
		private static volatile boolean stop;
		
		public IDConsumeTask(IDGenerator idGenerator, String name) {
			this.idGenerator = idGenerator;
			this.name = name;
		}
		
		public static void stopAll() {
			stop = true;
		}
		
		public void run() {
			logger.debug("{} : consumer {} start", time(), name);
			while (!stop) {
				String id = null;
				try {
					id = idGenerator.getAndIncrement();
				} catch (NullPointerException e) {
					e.printStackTrace();
					stopAll();
				}
				if (id != null) {
					if(generatedIds.contains(id)) {
						logger.error("{} : duplicate id generated, id = {}", time(), id);
						stop = true;
						continue;
					} 
					
					generatedIds.add(id);
					logger.debug("{} add {}", Thread.currentThread().getName(), id);
				}
			}
			// 释放资源
			idGenerator.release();
			logger.debug("{} : consumer {} done", time(), name);
		}
		
	}
	
}
