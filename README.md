# codegen
Maven plugin that generates classes from a given interface. The master branch generates code to set up a chain of responsibilities. The given interface should only have methods with return type void. The plugin generates a delegator class that delegates a call on the interface to a chain of handlers. A handler is an interface that is derived from the original interface, but the handler's methods return boolean and they have an extra context argument. The delegator calls each handler until a handler returns true.

When calling a handler, the delegator passes the original arguments and passes a context object. The implementation of each handler method can use the context to add extra handlers or remove handlers. These changes are applied after visiting handlers is done.

From a given interface, the following classes are generated:
* An implementation that delegates to instances of a handler interface.
* The handler interface.
* A default implementation of the handler interface.

To see how to use the plugin, see the artifact codegenTest within this project. In the pom.xml, use the <build> section to invoke the codegenPlugin. Configure the full class names of the source interface and the generated classes there. In the <dependencies> section, add as a dependency the codegenRuntime artifact. The generated code appears in the target/generated-sources folder of the calling project.
