package org.eclipse.ease.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PlatformExtensionTest {

	private static final String EXTENSION_POINT = "org.eclipse.ease.test";

	@Test
	@DisplayName("createFor('{ease}.notThere') returns empty collection")
	public void createFor_returns_empty_collection() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor("org.eclipse.ease.notthere");
		assertNotNull(extensions);
		assertTrue(extensions.isEmpty());
	}

	@Test
	@DisplayName("createFor('{ease}.test') returns populated collection")
	public void createFor_returns_non_empty_collection() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);
		assertEquals(2, extensions.size());
	}

	@Test
	@DisplayName("createForName('{ease}.test', 'entry') returns populated collection")
	public void createForName_returns_non_empty_collection() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createForName(EXTENSION_POINT, "entry");
		assertEquals(2, extensions.size());
	}

	@Test
	@DisplayName("getConfigurationElement() != null")
	public void getConfigurationElement_is_not_null() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);

		for (final PlatformExtension extension : extensions)
			assertNotNull(extension.getConfigurationElement());
	}

	@Test
	@DisplayName("getAttribute() != null for known attribute")
	public void getAttribute_is_not_null_for_known_attribute() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);
		final PlatformExtension extension = extensions.iterator().next();

		assertNotNull(extension.getAttribute("ID"));
	}

	@Test
	@DisplayName("getAttribute() == null for unknown attribute")
	public void getAttribute_is_null_for_unknown_attribute() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);
		final PlatformExtension extension = extensions.iterator().next();

		assertNull(extension.getAttribute("unknown"));
	}

	@Test
	@DisplayName("createInstance() != null for valid type")
	public void createInstance_returns_class_instance() throws CoreException {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);
		final PlatformExtension extension = extensions.stream().filter(e -> Objects.equals("stringEntry", e.getAttribute("ID"))).findAny().orElseThrow();

		assertNotNull(extension.createInstance("class", String.class));
	}

	@Test
	@DisplayName("createInstance() throws ClassCastException for invalid type")
	public void createInstance_throws_for_invalid_type() throws CoreException {
		final Collection<PlatformExtension> extensions = PlatformExtension.createFor(EXTENSION_POINT);
		final PlatformExtension extension = extensions.stream().filter(e -> Objects.equals("stringEntry", e.getAttribute("ID"))).findAny().orElseThrow();

		assertThrows(ClassCastException.class, () -> extension.createInstance("class", StringBuilder.class));
	}
}
