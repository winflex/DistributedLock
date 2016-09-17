package cc.lixiaohui.lock;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import cc.lixiaohui.lock.time.nio.client.TimeClient;


public class JedisBasedDistributedReadWriteLock implements ReadWriteLock {

	private Lock readLock;
	
	private Lock writeLock;
	
	public JedisBasedDistributedReadWriteLock(Jedis jedis, String readLockKey, String writeLockKey
			, long readLockExpires, long writeLockExpires, SocketAddress timeServerAddr) throws IOException {
		
	}
	
	public Lock readLock() {
		return null;
	}

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
