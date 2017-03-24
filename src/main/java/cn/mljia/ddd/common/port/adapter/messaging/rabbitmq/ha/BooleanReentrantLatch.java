package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 
 * @ClassName: BooleanReentrantLatch
 * @Description: TODO 并发锁
 * @author: mljia.cn-Marker
 * @date: 2017年1月11日 下午16:43:59
 */
public class BooleanReentrantLatch {

	/**
	 * 
	 * @ClassName: Sync
	 * @Description: TODO 同步锁控制,state代表锁状态===>states: open == 0, closed == 1
	 * @author: mljia.cn-Marker
	 * @date: 2017年1月11日 下午17:43:59
	 */
	private static final class Sync extends AbstractQueuedSynchronizer {

		private static final long serialVersionUID = -7271227048279204885L;

		protected Sync(final boolean open) {
			setState(open ? 0 : 1);
		}

		@Override
		public boolean tryReleaseShared(final int releases) {

			return compareAndSetState(1, 0);
		}

		protected boolean isOpen() {
			return getState() == 0;
		}

		@Override
		protected int tryAcquireShared(final int acquires) {
			if (acquires == 0) {
				return isOpen() ? 1 : -1;
			}
			setState(1);
			return 1;
		}
	}

	private final Sync sync;

	public BooleanReentrantLatch() {
		this(true);
	}

	public BooleanReentrantLatch(final boolean open) {
		sync = new Sync(open);
	}

	public void close() {
		sync.acquireShared(1);
	}

	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof BooleanReentrantLatch)) {
			return false;
		}

		BooleanReentrantLatch rhs = (BooleanReentrantLatch) obj;
		return isOpen() == rhs.isOpen();
	}

	public boolean isClosed() {
		return !isOpen();
	}

	public boolean isOpen() {
		return sync.isOpen();
	}

	public void open() {
		sync.releaseShared(1);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + (isOpen() ? "open" : "closed") + "]";
	}

	public void waitUntilOpen() throws InterruptedException {
		sync.acquireSharedInterruptibly(0);
	}

	public boolean waitUntilOpen(final long timeout, final TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireSharedNanos(0, unit.toNanos(timeout));
	}
}
