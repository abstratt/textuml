Running the standalone tests
============================

TextUML is implemented as a set of Eclipse plugins and it's dependent on
various other Eclipse plugins, most notably the org.eclipse.uml2.uml one.

Therefore, it's non-trivial to get it running outside of an Eclipse/OSGI
environment.

The strategy taken here is to take the complete Eclipse product and
convert it to Maven modules by using the eclipse:to-maven goal from the
[Maven Eclipse Plugin](http://maven.apache.org/plugins/maven-eclipse-plugin/to-maven-mojo.html).

Beyond that, the following setups have to be performed (see [StandaloneUtil](http://sourceforge.net/p/textuml/code/HEAD/tree/trunk/contrib/standalone/src/test/java/textuml/contrib/standalone/StandaloneUtil.java)):

- An IExtensionRegistry has to be provided
- The OCLPlugin must be initialized
- Repository instances must be instrumented to make use of a properly configured ResourceSet

The only changes to production code:

- Repository to support an overridable factory method for ResourceSet (https://github.com/ThiporKong/textuml/commit/fb4b0e33aabe0737c619438a44fc1be9e7557f66)
- Plugin com.abstratt.mdd.core.tests included into the product for the sake of Maven eclipse:to-maven (https://github.com/ThiporKong/textuml/commit/53935f7072ceff7004e49669c98e278a4620d2f7)

The only changes to test code:

- AbstractRepositoryTests to take the repository creation strategy from a system property (https://github.com/ThiporKong/textuml/commit/11dd9e540a7ca729fb237204b2b7b2ec3c03102b)
- BuiltInStereotypeTests.testEntryPointPersistence() to use the repository creation strategy from AbstractRepositoryTests (https://github.com/ThiporKong/textuml/commit/10f851edd1c0ee9192a458bf5304064b59ec3d4c)

Building and running the [standalone test](http://sourceforge.net/p/textuml/code/HEAD/tree/trunk/contrib/standalone/src/test/java/textuml/contrib/standalone/StandaloneTest.java):

``` sh
# change current working directory to top-level directory (com.abstratt.mdd.parent)

# build the product
mvn integration-test

# install the product's plugins in local Maven repistory
(cd products/com.abstratt.mdd.frontend.cli.product.test/target/products/com.abstratt.mdd-cli-test/linux/gtk/x86_64/; mvn eclipse:to-maven -DeclipseDir=. -DstripQualifier=true)

# potentially need to fix version numbers for Eclipse/TextUML dependencies in contrib/standalone/pom.xml

# Run standalone unit tests
(cd contrib/standalone.parent; mvn test)
```
