/**
 * 
 */
package cc.lixiaohui.lock.util;

import java.util.Set;

/**
 * 读写锁信息
 * 
 * @author lixiaohui
 * @date 2016年9月17日 下午12:09:28
 * 
 */
public class ReadWriteLockInfo {
	// ------------- read lock fields ------------
	
	private Set<LockInfo> readInfos;
	
	// ------------- write lock fields -----------
	
	private LockInfo writeInfo;

	public Set<LockInfo> getReadInfos() {
		return readInfos;
	}

	public void setReadInfos(Set<LockInfo> readInfos) {
		this.readInfos = readInfos;
	}

	public LockInfo getWriteInfo() {
		return writeInfo;
	}

	public void setWriteInfo(LockInfo writeInfo) {
		this.writeInfo = writeInfo;
	}
	
}
