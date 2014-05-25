This is the new repository for the TextUML tooling and replaces what is on SourceForge.

To build this code, you need git, maven and an internet connection (you will be downloading half of it):

    # the TextUML Toolkit repository per se
    git clone https://abstratt@bitbucket.org/abstratt/textuml.git
 
    # this repository has some dependencies used by the Toolkit
    git clone https://abstratt@bitbucket.org/abstratt/eclipsegraphviz.git
 
    # install the master POM
    mvn clean install -f eclipsegraphviz/master/pom.xml
 
    # optional: build EclipseGraphviz locally (required if you have local changes)
    mvn clean install -f eclipsegraphviz/pom.xml

    # finally build the Toolkit (pass -Dmaven.test.skip=true to skip tests)
    mvn clean install -f textuml/pom.xml

If you are using Eclipse, you can skip the use of Maven from the command line, but you need Maven support (M2E) in Eclipse. Just import the code (once for EclipseGraphviz, once for the TextUML Toolkit) into your workspace as existing Maven projects (it will find a lot of them).

That is it - if you follow these instructions, you will end up with the code for EclipseGraphviz and TextUML Toolkit as sibling directories, and you now have the entire TextUML Toolkit and EclipseGraphviz projects compiled, including a ready-to-use Eclipse update site at:

    ./textuml/repositories/com.abstratt.mdd.oss.repository/target/repository