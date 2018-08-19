# codegen
Maven plugin that generates classes from given interfaces and Velocity templates. The actual plugin is in sub-project codegenPlugin. To use the plugin, add it to the `<build><plugins>` section of your pom.xml. You need to write a small Java class that tells the Codegen plugin what Java interfaces to use as source data, what output classes to generate and what Velocity templates to use. This is the Codegen program. The codegenTest sub-project does an integration test of the Codegen plugin. Please see this code to learn how to use the plugin.

Here is a quick list of the capabilities of codegenPlugin:
1. Generate Java classes based on Velocity templates that are populated using your own Java program, yo Codegen program.
2. Your Java program defines the data it needs through annotations and public fields. Requestable data is:
    + Metadata about classes, in particular the class name and the methods.
    + Metadata about class hierarchies, in particular the list of sub-classes of a given root. 
    + You can filter this list of sub-classes by an interface that should be implemented. This feature was introduced to generate implementations of visitors. You can for example distinguish between composite and atomic classes.
3. You can use helper code to generate a chain of responsibilities.

Here is an overview of all the sub projects:
* codegenPlugin: The Mojo you need in your pom.xml.
* codegenTest: Provides integration test and shows how to use this project.
* codegenTestProgram: Example of a Codegen program you can write.
* codegenTestInput: Example input classes accessed. The metadata of these classes is injected into the example Codegen program.
* codegenCompiletime: The Java classes and annotations needed to build a proper Codegen program.
* codegenRuntime: Java code that is needed by the generated classes, in particular when you want a chain of responsibilities.

Sub-project codegenRuntime allows you to set up a chain of responsibilities. Start with a source interface of methods returning void, and let your Codegen program generate a delegator and a handler class. The delegator implements the source interface and delegates each method call to a chain of handler objects. A handler is an interface that is derived from the source interface, but the handler's methods return boolean and they have an extra context argument. 

The delegator has a member of type `HandlerStack`, which is a class in codegenRuntime. The HandlerStack class takes care of accessing the chain nodes. It also provides the extra context argument that allows each handler to manipulate the handler chain. HandlerStack calls the handlers from the first one to the last one and stops after a handler returns true. The chain does not change while the handlers are being traversed, because HandlerStack applies the changes applied through the context object after traversing is done.

Class `VelocityContexts` provides helper methods to set this up. These methods are applied in CodegenTestProgram and you can emulate that code to set up your own chain of responsibilities. 

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
2. Check that all sub-projects have the same version number as the master pom. You can use the script `testScripts/checkVersion.sh` for this.

## Integration tests

1. When all your work is committed, introduce some errors in `codegenTestProgram/.../InputProgram.java`. Then run the build and check whether your errors are detected. You can remove your errors by checking out the original code again.
