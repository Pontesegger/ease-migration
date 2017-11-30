package org.eclipse.ease.debugging.events;

public abstract class AbstractEvent implements IDebugEvent {

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
