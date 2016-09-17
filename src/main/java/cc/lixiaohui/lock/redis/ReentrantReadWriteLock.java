package cc.lixiaohui.lock.redis;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import cc.lixiaohui.lock.Lock;
import cc.lixiaohui.lock.ReadWriteLock;
import cc.lixiaohui.lock.time.nio.client.TimeClient;

/**
 * 基于Redis的分布式读写可重入锁(读读不互斥, 读写互斥, 写写互斥)
 * <pre>
 * 锁设计: 读锁和写锁的锁信息分开存储
 * read lock: {
 * 	expires:
 * }
 * </pre>
 * @author lixiaohui
 * @date 2016年9月17日 上午11:52:06
 *
 */
public class ReentrantReadWriteLock implements ReadWriteLock {

	private ReadLock readLock;
	
	private WriteLock writeLock;
	
	public ReentrantReadWriteLock(Jedis jedis, String readLockKey, String writeLockKey
			, long readLockExpires, long writeLockExpires, SocketAddress timeServerAddr) throws IOException {
		
	}
	
	/**
     * Returns the lock used for reading.
     *
     * @return the lock used for reading
     */
	public Lock readLock() {
		return null;
	}

	/**
     * Returns the lock used for writing.
     *
     * @return the lock used for writing
     */
	public Lock writeLock() {
		return null;
	}
	
	public static class ReadLock implements Lock {

		private Jedis jedis;
		
		private TimeClient timeClient;
		
		private String lockKey;
		
		private long lockExpires;
		
		public ReadLock(Jedis jedis, String lockKey, long lockExpires, SocketAddress timeServerAddress) throws IOException {
			this.jedis = jedis;
			timeClient = new TimeClient(timeServerAddress);
		}
		
		public void lock() {
			jedis.get(lockKey);
		}

		public void lockInterruptibly() throws InterruptedException {
			
		}

		public boolean tryLock() {
			return false;
		}

		public boolean tryLock(long time, TimeUnit unit) {
			return false;
		}

		public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
			return false;
		}

		public void unlock() {
			
		}
		
		public void release() {
			jedis.close();
			timeClient.close();
		}
		
	}
	
	public static class WriteLock implements Lock {

		public void release() {
			
		}

		public void lock() {
			
		}

		public void lockInterruptibly() throws InterruptedException {
			
		}

		public boolean tryLock() {
			return false;
		}

		public boolean tryLock(long time, TimeUnit unit) {
			return false;
		}

		public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
			return false;
		}

		public void unlock() {
			
		}
		
	}

}
