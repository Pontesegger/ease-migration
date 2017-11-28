package org.eclipse.ease;

import org.eclipse.ease.adapters.ScriptableAdapterTest;
import org.eclipse.ease.sign.SignatureHelperTest;
import org.eclipse.ease.tools.ResourceToolsTest;
import org.eclipse.ease.tools.RunnableWithResultTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ResourceToolsTest.class, RunnableWithResultTest.class, AbstractCodeParserTest.class, ScriptResultTest.class, ScriptTest.class,
		AbstractReplScriptEngineTest.class, ScriptableAdapterTest.class, SignatureHelperTest.class })
public class AllTests {

}
