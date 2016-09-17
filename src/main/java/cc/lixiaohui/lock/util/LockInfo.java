package cc.lixiaohui.lock;

import cc.lixiaohui.lock.util.PlatformUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

/**
 * 
 * @author lixiaohui
 * @date 2016年9月14日 上午10:22:39
 *
 */
public class LockInfo {
	
	private long expires;
	
	private String mac;
	
	private long jvmPid;
	
	private long threadId;
	
	private int count;
	
	private static final transient String LOCAL_MAC = PlatformUtils.MACAddress();
	
	private static final transient int CURRENT_PID = PlatformUtils.JVMPid();
	
	private static final transient SimplePropertyPreFilter FILTER = new SimplePropertyPreFilter();
	
	static {
		FILTER.getExcludes().add("currentThread");
	}
	
	
	public LockInfo incCount() {
		if (count == Integer.MAX_VALUE) {
			throw new Error("Maximum lock count exceeded");
		}
		++count;
		return this;
	}
	
	public LockInfo decCount() {
		--count;
		return this;
	}
	
	public boolean isCurrentThread() {
		return mac.equals(LOCAL_MAC) && jvmPid == CURRENT_PID && Thread.currentThread().getId() == threadId;
	}
	
	public static LockInfo fromString(String lockInfo) {
		try {
			return JSON.parseObject(lockInfo, LockInfo.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static LockInfo newForCurrThread(long expires) {
		LockInfo lockInfo = new LockInfo();
		lockInfo.setThreadId(Thread.currentThread().getId());
		lockInfo.setCount(1);
		lockInfo.setExpires(expires);
		lockInfo.setJvmPid(CURRENT_PID);
		lockInfo.setMac(LOCAL_MAC);
		return lockInfo;
	}

	public long getExpires() {
		return expires;
	}

	public LockInfo setExpires(long expires) {
		this.expires = expires;
		return this;
	}

	public String getMac() {
		return mac;
	}

	public LockInfo setMac(String mac) {
		this.mac = mac;
		return this;
	}

	public long getJvmPid() {
		return jvmPid;
	}

	public LockInfo setJvmPid(long jvmPid) {
		this.jvmPid = jvmPid;
		return this;
	}

	public long getThreadId() {
		return threadId;
	}

	public LockInfo setThreadId(long threadId) {
		this.threadId = threadId;
		return this;
	}

	public int getCount() {
		return count;
	}

	public LockInfo setCount(int count) {
		this.count = count;
		return this;
	}
	
	public static String toString(LockInfo lockInfo) {
		return JSON.toJSONString(lockInfo, FILTER);
	}
	
	@Override
	public String toString() {
		return toString(this);
	}
	
	public boolean isSame(Object obj) {
		LockInfo info = (LockInfo) obj;
		return info.getMac().equals(mac) 
				&& info.getJvmPid() == jvmPid 
				&& info.getThreadId() == threadId
				&& info.getExpires() == expires
				&& info.getCount() == count;
	}
	
}
