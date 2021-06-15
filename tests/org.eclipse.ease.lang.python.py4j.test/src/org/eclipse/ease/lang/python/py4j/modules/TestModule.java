/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j.modules;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

/**
 * Test module for py4j array conversions.
 */
public class TestModule extends AbstractScriptModule {
	/**
	 * Join strings by ", ".
	 *
	 * @param strings
	 *            Strings to be joined.
	 * @return Joined strings.
	 */
	@WrapToScript
	public String stringArray(@ScriptParameter String[] strings) {
		return String.join(", ", strings);
	}

	/**
	 * Calculate sum of Integers.
	 *
	 * @param ints
	 *            Integers to be summed.
	 * @return sum of Integers
	 */
	@WrapToScript
	public int integerArray(@ScriptParameter Integer[] ints) {
		return Arrays.stream(ints).collect(Collectors.summingInt(Integer::intValue));
	}

	/**
	 * Calculate the sum of ints.
	 *
	 * @param ints
	 *            ints to be summed.
	 * @return sum of ints.
	 */
	@WrapToScript
	public int intArray(@ScriptParameter int[] ints) {
		return Arrays.stream(ints).sum();
	}

	/**
	 * Calculate the sum of doubles.
	 *
	 * @param doubles
	 *            doubles to be summed.
	 * @return sum of doubles.
	 */
	@WrapToScript
	public double doubleArray(@ScriptParameter double[] doubles) {
		return Arrays.stream(doubles).sum();
	}

	/**
	 * Calculate "all" over booleans.
	 *
	 * @param booleans
	 *            booleans to calculate "all" over.
	 * @return "all" of booleans.
	 */
	@WrapToScript
	public boolean booleanArray(@ScriptParameter boolean[] booleans) {
		for (final boolean b : booleans) {
			if (!b) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Calculate the sum of shorts.
	 *
	 * @param shorts
	 *            shorts to be summed.
	 * @return sum of shorts.
	 */
	@WrapToScript
	public short shortArray(@ScriptParameter short[] shorts) {
		short sum = 0;
		for (final short s : shorts) {
			sum += s;
		}
		return sum;
	}

	/**
	 * Calculate hexadecimal string for given bytes.
	 *
	 * @param bytes
	 *            bytes to be get hex string for.
	 * @return hex string.
	 */
	@WrapToScript
	public String byteArray(@ScriptParameter byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : bytes) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}

	/**
	 * Calculate the sum of longs.
	 *
	 * @param longs
	 *            longs to be summed.
	 * @return sum of longs.
	 */
	@WrapToScript
	public long longArray(@ScriptParameter long[] longs) {
		return Arrays.stream(longs).sum();
	}

	/**
	 * Calculate the sum of floats.
	 *
	 * @param floats
	 *            floats to be summed.
	 * @return sum of floats.
	 */
	@WrapToScript
	public float floatArray(@ScriptParameter float[] floats) {
		float sum = 0.0f;
		for (final float f : floats) {
			sum += f;
		}
		return sum;
	}

	/**
	 * Calculate a chain of relative Path objects.
	 *
	 * @param paths
	 *            paths to make relative chain for.
	 * @return relative path chain.
	 */
	@WrapToScript
	public IPath javaObjectArray(@ScriptParameter Path[] paths) {
		IPath concatenated = new Path(".");
		for (final Path p : paths) {
			concatenated = concatenated.append(p);
		}
		return concatenated;
	}

	/**
	 * Calculate a chain of relative Path objects.
	 *
	 * @param paths
	 *            paths to make relative chain for.
	 * @return relative path chain.
	 * @see #javaObjectArray(Path[])
	 */
	@WrapToScript
	public IPath varargs(@ScriptParameter Path... paths) {
		return javaObjectArray(paths);
	}
}
