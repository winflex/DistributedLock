package cc.lixiaohui.DistributedLock.DistributedLock;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.Jedis;
import cc.lixiaohui.lock.Lock;
import cc.lixiaohui.lock.RedisBasedDistributedLockV_1_0;
import cc.lixiaohui.lock.RedisBasedDistributedLockV_1_1;
import cc.lixiaohui.lock.RedisBasedDistributedLockV_1_2;
import cc.lixiaohui.lock.example.IDGenerator;

public class IDGeneratorTest {
	
	private static Set<String> generatedIds = new HashSet<String>();
	
	private static final String LOCK_KEY = "lock.lock";
	private static final long LOCK_EXPIRE = 100000 * 1000;
	static final SocketAddress ADDR = new InetSocketAddress("localhost", 9999);
	
	@Test
	public void testReentrant() throws Exception {
		
		Jedis jedis1 = new Jedis("localhost", 6379);
		Lock lock1 = new RedisBasedDistributedLockV_1_2(jedis1, LOCK_KEY, LOCK_EXPIRE, ADDR);
		IDGenerator g1 = new IDGenerator(lock1);
		IDConsumeTask consume1 = new IDConsumeTask(g1, "consume1");
		
		Jedis jedis2 = new Jedis("localhost", 6379);
		Lock lock2 = new RedisBasedDistributedLockV_1_2(jedis2, LOCK_KEY, LOCK_EXPIRE, ADDR);
		IDGenerator g2 = new IDGenerator(lock2);
		IDConsumeTask consume2 = new IDConsumeTask(g2, "consume2");
		
		Thread t1 = new Thread(consume1);
		Thread t2 = new Thread(consume2);
		t1.start();
		t2.start();
		
		Thread.sleep(20 * 1000); //让两个线程跑20秒
		
		IDConsumeTask.stopAll();
		
		t1.join();
		t2.join();
	}
	@Test
	public void test() throws Exception {
		
		Jedis jedis1 = new Jedis("localhost", 6379);
		Lock lock1 = new RedisBasedDistributedLockV_1_0(jedis1, LOCK_KEY, LOCK_EXPIRE);
		IDGenerator g1 = new IDGenerator(lock1);
		IDConsumeTask consume1 = new IDConsumeTask(g1, "consume1");
		
		Jedis jedis2 = new Jedis("localhost", 6379);
		Lock lock2 = new RedisBasedDistributedLockV_1_0(jedis2, LOCK_KEY, LOCK_EXPIRE);
		IDGenerator g2 = new IDGenerator(lock2);
		IDConsumeTask consume2 = new IDConsumeTask(g2, "consume2");
		
		Thread t1 = new Thread(consume1);
		Thread t2 = new Thread(consume2);
		t1.start();
		t2.start();
		
		Thread.sleep(20 * 1000); //让两个线程跑20秒
		
		IDConsumeTask.stopAll();
		
		t1.join();
		t2.join();
	}
	@Test
	public void testTime() throws Exception {
		
		SocketAddress addr = new InetSocketAddress("localhost", 9999);
		
		Jedis jedis1 = new Jedis("localhost", 6379);
		Lock lock1 = new RedisBasedDistributedLockV_1_1(jedis1, LOCK_KEY, LOCK_EXPIRE, addr);
		IDGenerator g1 = new IDGenerator(lock1);
		IDConsumeTask consume1 = new IDConsumeTask(g1, "consume1");
		
		Jedis jedis2 = new Jedis("localhost", 6379);
		Lock lock2 = new RedisBasedDistributedLockV_1_1(jedis2, LOCK_KEY, LOCK_EXPIRE, addr);
		IDGenerator g2 = new IDGenerator(lock2);
		IDConsumeTask consume2 = new IDConsumeTask(g2, "consume2");
		
		Thread t1 = new Thread(consume1);
		Thread t2 = new Thread(consume2);
		t1.start();
		//t2.start();
		
		Thread.sleep(20 * 1000); //让两个线程跑20秒
		
		IDConsumeTask.stopAll();
		
		t1.join();
		t2.join();
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
			System.out.println(time() + ": consume " + name + " start ");
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
						System.out.println(time() + ": duplicate id generated, id = " + id);
						stop = true;
						continue;
					} 
					
					generatedIds.add(id);
					System.out.println(String.format("----------------- %s add %s", Thread.currentThread().getName(), id));
				}
			}
			// 释放资源
			idGenerator.release();
			System.out.println(time() + ": consume " + name + " done ");
		}
		
	}
	
}
