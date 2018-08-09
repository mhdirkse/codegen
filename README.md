# codegen
Maven plugin that generates classes from given interfaces and Velocity templates. The actual plugin is in sub-project codegenPlugin. To use the plugin, add it to the `<build><plugins>` section of your pom.xml. You need to write a small Java class that tells the Codegen plugin what Java interfaces to use as source data, what output classes to generate and what Velocity templates to use. This is the Codegen program. The codegenTest sub-project does an integration test of the Codegen plugin. Please see this code to learn how to use the plugin.

Sub-project codegenTest uses an example Codegen program which is in sub-project codegenTestProgram. Sub-project codegenTestInput provides the source Java interface accessed by codegenTestProgram and codegenTest. Sub-project codegenCompiletime provides the Java classes and annotations needed to build a proper Codegen program. Sub-project codegenRuntime provides Java code that is needed by the classes you generate using the plugin.

Sub-project codegenRuntime allows you to set up a chain of responsibilities. Start with a source interface of methods returning void. Then use Velocity to create a delegator and a handler class. The delegator implements the source interface and delegates each method call to a chain of handler objects. A handler is an interface that is derived from the source interface, but the handler's methods return boolean and they have an extra context argument.

Please add to the delegator a member of type HandlerStack, which is in codegenRuntime. This class takes care of accessing the chain nodes. It also provides the extra context argument that allows each handler to manipulate the handler chain. HandlerStack calls the handlers from the first one to the last one and stops after a handler returns true. The chain does not change while the handlers are being traversed, because HandlerStack applies the changes applied through the context object after traversing is done. Please build all sub-projects of codegen to see how this works. 

# Testing

Much code is covered by unit tests, but it is wise to test some things manually.

## Code reviews

1. Please check the validity of all the pom.xml files, as follows:
    * Each pom.xml file must reference the same version number in the <parent> section.
    * codegenRuntime, codegenCompiletime and codegenTestInput should not reference other Codegen sub-projects.
    * codegenPlugin should only reference codegenCompiletime, and it should not depend directly on Velocity.
    * codegenTestProgram should only depend on codegenCompiletime.
    * codegenTest should have codegenRuntime as a dependency.
    * codegenTest should have codegenTestInput as a dependency.
    * codegenTest should use plugin codegenPlugin.
    * Within that <plugin> section, codegenTestProgram should be added as a dependency.
    * codegenTest should not depend directly on other sub-projects.
