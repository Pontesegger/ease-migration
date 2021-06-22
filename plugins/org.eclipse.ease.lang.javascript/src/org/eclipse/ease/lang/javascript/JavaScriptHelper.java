package org.eclipse.ease.lang.javascript;

import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;

public final class JavaScriptHelper {

	/** Script type identifier for JavaScript. Must match with the script type 'name' from plugin.xml. */
	public static final String SCRIPT_TYPE_JAVASCRIPT = "JavaScript";

	@Deprecated
	private JavaScriptHelper() {
		throw new IllegalArgumentException("Not meant to be called");
	}

	/**
	 * Get the {@link ScriptType} for java script.
	 *
	 * @return script type definition
	 */
	public static ScriptType getScriptType() {
		final IScriptService scriptService = ScriptService.getInstance();
		return scriptService.getAvailableScriptTypes().get(SCRIPT_TYPE_JAVASCRIPT);
	}
}
