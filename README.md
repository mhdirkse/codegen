# codegen
Maven plugin that generates classes from given interfaces and Velocity templates. The actual plugin is in sub-project codegenPlugin. To use the plugin, add it to the `<build><plugins>` section of your pom.xml. You need to write a small Java class that tells the Codegen plugin what Java interfaces to use as source data, what output classes to generate and what Velocity templates to use. This is the Codegen program. The codegenTest sub-project does an integration test of the Codegen plugin. Please see this code to learn how to use the plugin.

Here is a quick list of the capabilities of codegenPlugin:
1. Generate Java classes based on Velocity templates that are populated using your own Java program, you Codegen program.
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

# Tips and tricks

* To inject metadata about a Java interface, define a public field of type `ClassModel` and annotate as `@Input`. The value of the annotation is the name of the interface.
* To inject a list of all class names in a dependency hierarchy, define a public field of type `ClassModelList`. This type is just a non-parameterized alias of `List<ClassModel>`. Add annotation `@TypeHierarchy` with the base class name as value.
* You can add an additional parameter to the `@TypeHierarchy` annotation to filter by inheritance from an interface. Use property `filterIsA`.
* The `@TypeHierarchy` annotation expects all classes to be in the same package and the same class loader.
* To apply a VelocityTemplate, define a public field of type `VelocityContext`. Annotate with `@Output` to define the template that has to be filled with this `VelocityContext`. The output `.java` file is based on the class name. The class name should be in a `ClassModel` object in the `VelocityContext` under key `"target"`.
* To refresh your generated files, you can do a clean Maven build with `mvn clean install`. The Maven clean removes the target directory and the Maven install regenerates them.
* Alternatively, you can delete your `target/generated-sources` folder and do a Maven update in Eclipse. The Codegen plugin was build to cooperate with Eclipse.
* To analyze errors, you will probably need the console output of Maven. However, a Mojo exception is always thrown when Codegen encounters errors. Eclipse will therefore always show a trace of the issue in its Problems window.

# Testing

For the users of this plugin, I do my very best to tag only versions that work. For this reason, please work with a tagged version of this plugin.
<p>
In the remainder of this section, I write some notes on how to test future versions of this plugin.
<p>
Much code is covered by unit tests, but I remember the following ideas for manual tests.

### Code reviews

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

### Integration tests

1. When all work is committed, introduce some errors in `codegenTestProgram/.../InputProgram.java`. Then run the build and check whether your errors are detected. You can remove your errors by checking out the original code again.
2. In CodegenTest remove `target/generated-sources`. A Maven update in Eclipse should be sufficient to rebuild the project.
