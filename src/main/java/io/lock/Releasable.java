package io.lock;

/**
 * 代表持有资源的对象, 例如
 * <ul>
 * <li> 基于jedis的锁自然持有与redis server的连接 </li>
 * <li> 基于时间统一的的锁自然持有与time server的连接</li>
 * </ul>
 * 因此锁应该实现该接口, 并在{@link Releasable#resease() release} 方法中释放相关的连接
 * 
 * @author lixiaohui
 *
 */
public interface Releasable {
	
	/**
	 * 释放持有的所有资源
	 */
	void release();
	
}
