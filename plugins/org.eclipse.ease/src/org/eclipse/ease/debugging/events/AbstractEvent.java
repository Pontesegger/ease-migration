package org.eclipse.ease.debugging.events;

public abstract class AbstractEvent implements IDebugEvent {

	private final Object fThread;

	protected AbstractEvent(Object thread) {
		fThread = thread;
	}

	protected AbstractEvent() {
		this(Thread.currentThread());
	}

	public Object getThread() {
		return fThread;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + getThread() + ")";
	}
}
