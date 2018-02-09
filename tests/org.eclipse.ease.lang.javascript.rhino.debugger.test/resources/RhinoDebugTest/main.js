print("Start of script");
var primitiveInteger = 42;
var primitiveString = "Hello world";
var nullValue = null;
var nativeArray = [1, "foo", null]
var bigArray = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
var nativeObject = {firstname: "John", lastname: "Doe"};
var file = org.eclipse.ease.tools.ResourceTools.resolve("workspace://Rhino Debug Test/RhinoDebugTest.js")

function testMethod(a, b) {
	var primitiveInteger = -42;
	return a + b;
}


var result = testMethod(2, 3);
print("Result of testMethod = " + result);

print("End of script");