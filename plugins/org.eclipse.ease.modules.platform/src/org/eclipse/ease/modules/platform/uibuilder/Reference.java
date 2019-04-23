/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform.uibuilder;

public class Reference {
	private final Object fDelegate;

	public Reference(Object delegate) {
		fDelegate = delegate;
	}

	public Object getDelegate() {
		return fDelegate;
	}

	@Override
	public String toString() {
		return "@" + fDelegate.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((fDelegate == null) ? 0 : fDelegate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Reference other = (Reference) obj;
		if (fDelegate == null) {
			if (other.fDelegate != null)
				return false;
		} else if (!fDelegate.equals(other.fDelegate))
			return false;
		return true;
	}
}
