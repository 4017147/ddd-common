package cn.mljia.ddd.common.lock;

public interface Lock {

	public boolean tryLock(String lockName) throws Exception;

	public boolean unLock(String lockName) throws Exception;

	public int waitTimeOut();

	public int lockExpireTime();

}
