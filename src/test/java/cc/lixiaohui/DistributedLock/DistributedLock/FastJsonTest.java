/**
 * 
 */
package cc.lixiaohui.DistributedLock.DistributedLock;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cc.lixiaohui.lock.util.LockInfo;
import cc.lixiaohui.lock.util.ReadWriteLockInfo;

import com.alibaba.fastjson.JSON;

/**
 * @author lixiaohui
 * @date 2016年9月14日 上午10:25:46
 * 
 */
public class FastJsonTest {
	
	@Test
	public void test() {
		User user = new User();
		user.setName("lixiaohui");
		String json = JSON.toJSONString(user);
		System.out.println(json);
	}
	
	@Test
	public void test1() {
		Set<LockInfo> infos = new HashSet<LockInfo>();
		infos.add(LockInfo.newForCurrThread(System.currentTimeMillis()));
		infos.add(LockInfo.newForCurrThread(System.currentTimeMillis()));
		
		User user = new User();
		user.setName("lll");
		user.setInfos(infos);
		
		System.out.println(JSON.toJSONString(user));
	}
	
	@Test
	public void test2() {
		String json = "{\"infos\":[{\"count\":1,\"currentThread\":true,\"expires\":1474086421769,\"jvmPid\":11636,\"mac\":\"28-D2-44-0E-0D-9A\",\"threadId\":1},{\"count\":1,\"currentThread\":true,\"expires\":1474086421658,\"jvmPid\":11636,\"mac\":\"28-D2-44-0E-0D-9A\",\"threadId\":1}],\"name\":\"lll\"}";
		User user = JSON.parseObject(json, User.class);
		System.out.println(user);
	}
	
	@Test
	public void test3() {
		Set<LockInfo> readInfos = new HashSet<LockInfo>();
		readInfos.add(LockInfo.newForCurrThread(System.currentTimeMillis()));
		
		ReadWriteLockInfo info = new ReadWriteLockInfo();
		info.setReadInfos(readInfos);
		info.setWriteInfo(LockInfo.newForCurrThread(System.currentTimeMillis()));
		
		System.out.println(info.toString());
		System.out.println(info.isLegal());
	}
}
class User {
	private String name;
	
	private Set<LockInfo> infos;
	
	public static final String DEFAULT_NAME = "name";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String getDefaultName() {
		return DEFAULT_NAME;
	}

	public Set<LockInfo> getInfos() {
		return infos;
	}

	public void setInfos(Set<LockInfo> infos) {
		this.infos = infos;
	}

}
