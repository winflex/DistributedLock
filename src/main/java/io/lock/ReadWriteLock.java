package io.lock;

/**
 * 读写锁
 * 
 * <ul>
 * 
 * <li>写锁与写锁不互斥, 写锁与读锁互斥, 读锁与读锁互斥</li>
 * <li>给读锁加锁时, 若当前写锁已被加锁, 则需等到写锁被释放才能给读锁加锁 </li>
 * <li>给写锁加锁时, 若当前读锁被被加锁(可被多次加锁), 则需等到所有持有读锁的节点释放读锁后才能给写锁加锁</li>
 * 
 * </ul>
 * <p>
 * 一个读写锁同时只能有一个写者或多个读者, 但不能同时既有读者又有写者
 * </p>
 * @author lixiaohui
 *
 */
public interface ReadWriteLock {
	
	/**
	 * 获取读锁
	 * @return
	 */
	Lock readLock();
	
	/**
	 * 获取写锁
	 * @return
	 */
	Lock writeLock();
	
}
