package cc.lixiaohui.lock.redis;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import cc.lixiaohui.lock.AbstractLock;
import cc.lixiaohui.lock.Lock;
import cc.lixiaohui.lock.ReadWriteLock;
import cc.lixiaohui.lock.time.nio.client.TimeClient;
import cc.lixiaohui.lock.util.ReadWriteLockInfo;

/**
 * 基于Redis的分布式读写可重入锁(读读不互斥, 读写互斥, 写写互斥)
 * <pre>
 * 锁设计: 读锁和写锁的锁信息分开存储
 * read lock: {
 * 	expires:
 * }
 * </pre>
 * 
 * @author lixiaohui
 * @date 2016年9月17日 上午11:52:06
 *
 */
public class ReentrantReadWriteLock implements ReadWriteLock {

	private ReentrantReadWriteLock.ReadLock readLock;
	
	private ReentrantReadWriteLock.WriteLock writeLock;
	
	private TimeClient timeClient;
	
	private Jedis jedis;
	
	public ReentrantReadWriteLock(Jedis jedis, String lockKey, long readLockExpires, long writeLockExpires, SocketAddress timeServerAddr) throws IOException {
		this.jedis = jedis;
		timeClient = new TimeClient(timeServerAddr);
		readLock = new ReadLock(this, jedis, lockKey, readLockExpires);
		writeLock = new WriteLock(jedis, lockKey, writeLockExpires);
	}
	
	/**
     * Returns the lock used for reading.
     *
     * @return the lock used for reading
     */
	public Lock readLock() {
		return readLock;
	}

	/**
     * Returns the lock used for writing.
     *
     * @return the lock used for writing
     */
	public Lock writeLock() {
		return writeLock;
	}
	
	private void checkInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
	}

	private boolean isTimeExpired(long time) {
		return time < serverTimeMillis();
	}

	private boolean isTimeout(long start, long timeout) {
		// 这里拿本地的时间来比较
		return start + timeout < System.currentTimeMillis();
	}

	private long serverTimeMillis() {
		return timeClient.currentTimeMillis();
	}

	private long localTimeMillis() {
		return System.currentTimeMillis();
	}
	
	public static class ReadLock extends AbstractLock {

		private Jedis jedis;
		
		private final String lockKey;
		
		private long readLockExpires;
		
		private ReentrantReadWriteLock readWriteLock;
		
		public ReadLock(ReentrantReadWriteLock readWriteLock, Jedis jedis, String lockKey, long readLockExpires) {
			this.readWriteLock = readWriteLock;
			this.jedis = jedis;
			this.lockKey = lockKey;
			this.readLockExpires = readLockExpires;
		}

		/* 
		 * @see cc.lixiaohui.lock.Lock#tryLock()
		 */
		public boolean tryLock() {
			/*
			 * walkthrough
			 * 1. 判断写锁是否被持有, 若被持有, return false
			 * 2. 
			 */
			long lockExpireTime = readWriteLock.serverTimeMillis() + readLockExpires + 1;
			//String stringOflockExpireTime = String.valueOf(lockExpireTime);
			
			String newReadInfoJson = ReadWriteLockInfo.newReadForCurrThread(lockExpireTime).toString();

			if (jedis.setnx(lockKey, newReadInfoJson) == 1) {
				locked = true;
				return true;
			}
			
			String json = jedis.get(lockKey);
			if (json == null) {
				// 再一次尝试获取
				if (jedis.setnx(lockKey, newReadInfoJson) == 1) {
					locked = true;
					return true;
				} else {
					locked = false;
					return false;
				}
			}
			ReadWriteLockInfo info = ReadWriteLockInfo.fromString(json);
			
			if (info.isWriteLocked()) { // 写锁被加锁
				locked = false;
				return false;
			}
			
			
			
			
			return false;
		}

		/* 
		 * @see cc.lixiaohui.lock.Releasable#release()
		 */
		public void release() {
		}

		/* 
		 * @see cc.lixiaohui.lock.AbstractLock#lock(boolean, long, java.util.concurrent.TimeUnit, boolean)
		 */
		@Override
		protected boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt) throws InterruptedException {
			return false;
		}

		/* 
		 * @see cc.lixiaohui.lock.AbstractLock#unlock0()
		 */
		@Override
		protected void unlock0() {
		}

		/* 
		 * @see cc.lixiaohui.lock.AbstractLock#isHeldByCurrentThread()
		 */
		@Override
		protected boolean isHeldByCurrentThread() {
			return false;
		}
		
		
		
	}
	
	public static class WriteLock implements Lock {
		
		private Jedis jedis;
		
		private String writeLockKey;
		
		private long writeLockExpires;
		
		public WriteLock(Jedis jedis, String writeLockKey, long writeLockExpires) {
			this.jedis = jedis;
			this.writeLockKey = writeLockKey;
			this.writeLockExpires = writeLockExpires;
		}

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
