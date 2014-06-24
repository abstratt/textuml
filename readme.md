### 2014/06/26 - We are migrating this project from Sourceforge, which inadvertently dropped support for MediaWiki. Documentation is in the process of being imported/adapted (by salvaging it from the Google cache and translating to Markdown via pandoc), and is slowly becoming available in the docs dir (broken links abound though).


This is the new repository for the TextUML tooling and replaces what is on SourceForge.

[![Build Status](https://textuml.ci.cloudbees.com/buildStatus/icon?job=textuml-toolkit)](https://textuml.ci.cloudbees.com/job/textuml-toolkit/)

To build this code, you need git, maven and an internet connection (you will be downloading half of it):

    # the TextUML Toolkit repository per se
    git clone https://github.com/abstratt/textuml.git
 
    # this repository has some dependencies used by the Toolkit
    git clone https://github.com/abstratt/eclipsegraphviz.git
 
    # install the master POM
    mvn clean install -f eclipsegraphviz/master/pom.xml
 
    # optional: build EclipseGraphviz locally (required if you have local changes)
    mvn clean install -f eclipsegraphviz/pom.xml

    # finally build the Toolkit (pass -Dmaven.test.skip=true to skip tests)
    mvn clean install -f textuml/pom.xml

If you are using Eclipse, you can skip the use of Maven from the command line, but you need Maven support (M2E) in Eclipse. Just import the code (once for EclipseGraphviz, once for the TextUML Toolkit) into your workspace as existing Maven projects (it will find a lot of them).

That is it - if you follow these instructions, you will end up with the code for EclipseGraphviz and TextUML Toolkit as sibling directories, and you now have the entire TextUML Toolkit and EclipseGraphviz projects compiled, including a ready-to-use Eclipse update site at:

    ./textuml/repositories/com.abstratt.mdd.oss.repository/target/repository
