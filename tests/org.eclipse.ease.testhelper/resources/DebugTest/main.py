print("Start of script")
primitiveInteger = 42.0
primitiveString = "Hello world"
nullValue = None
nativeArray = [1.0, "foo", None]
bigArray = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
nativeObject = {"firstname": "John", "lastname": "Doe"}
file = org.eclipse.ease.tools.ResourceTools.resolve("workspace://Debug Test/DebugTest.js")

def testMethod(a, b):   # testMethod-def-hook
    primitiveInteger = -42.0
    return a + b    # testMethod-result-hook

result = testMethod(2, 3)   # testMethod-call-hook
print("Result of testMethod = " + str(result))
print("End of script")
