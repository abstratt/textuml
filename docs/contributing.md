---
---
# Contributor Guide

Note this project is migrating to Github. Old issues are still on the Sourceforge repository.

# Important links

  * **Documentation** - make sure you are familiar with the TextUML Toolkit Documentation. 
  * **Forum** - if you have problems doing any of what is described below, don't hesitate to ask for help on the [support forum](http://abstratt.com/forum/). 
  * **Bug reports** - found a bug or looking for areas to contribute? The [issue tracker](https://github.com/abstratt/textuml/issues) is the place to go. 

#  Info for testers

  1. keep the Error Log view open so you can monitor any logged errors during operation. You can also set it to activate on log events by default - it is available on the drop down menu on the top right side of the view. 
  2. please report any problems you might find using the [issue tracker](https://github.com/abstratt/textuml/issues)
  3. please provide any exceptions you see (they will be in the log, and some might generate error dialogs too). 
  4. attach screenshots if necessary/helpful. 
  5. the documentation on the [ tool](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=TextUML_Toolkit_Features) and [ notation](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=TextUML_Guide) features provide a good basis for testing. 
  6. we are not only looking for error reports, but general feedback too. Feel free to share your opinion on what you think sucks, or missing features (given the tool goal, which is to support creating models that are suitable as input for code generation). Feedback on the [web site](http://abstratt.com/) and [documentation](http://abstratt.com/docs) is very welcome too. 

#  Info for developers

This section is intended for people developing or contributing patches to the
TextUML Toolkit project, not for end users. It describes how to configure a
development environment in order to properly build the TextUML Toolkit from
the source code.

##  Requirements

  * Java 6 or later 
  * Maven 3.0.x 
  * For 1.8, Eclipse SDK 3.7 (Indigo). For 1.9, Eclipse SDK 3.8 or 4.2 (Juno). For 1.120.x, Eclipse SDK 4.3.2 (Kepler) ([download](http://www.eclipse.org/downloads/)) 
  * Since 1.120.x, a git client, including Eclipse Git 
  * M2E - Maven Integration for Eclipse 

You can easily obtain M2E and Subversive from the Eclipse Marketplace. If you
don't have the Marketplace client installed (check Help &gt; Eclipse
Marketplace), install it from here:
[https://www.eclipse.org/mpc/](https://www.eclipse.org/mpc/)

##  Checking out the source code

The code has now moved to Bitbucket. See: [https://bitbucket.org/abstratt/text
uml](https://bitbucket.org/abstratt/textuml)

##  Importing the source code into Eclipse

Use the M2E import wizard (Import... &gt; Maven &gt; Existing Maven Projects) and
point it to the parent directory for the textuml and eclipsegraphviz
directories. It should find all TextUML Toolkit and EclipseGraphviz modules
inside that directory.

After the sources are imported, you should choose the target definition file
textuml/textuml-dependencies/textuml-dependencies.target as your target
platform (Window &gt; Preferences &gt; Plug-n Development &gt; Target Platform &gt;
TextUML Dependencies Target).

##  Running the TextUML Toolkit as an Eclipse application

Just launch a runtime workbench from your development Eclipse ([instructions](
http://help.eclipse.org/stable/index.jsp?topic=/org.eclipse.platform.doc.user/
tasks/tasks-121.htm)). Again, allow enough memory for the VM with -Xmx300m or
more.

##  Contributing code

Things to keep in mind when submitting patches or checking in code:

  * any code you submit must be licensed under the Eclipse Public License 
  * only submit code you wrote yourself or EPL code, if written by others 
  * every source file must have a [Copyright and License Notice](http://www.eclipse.org/legal/copyrightandlicensenotice.php)
  * note that if your employer is the owner of any IP you create, the employer should appear as the contributor on the copyright notice 
  * when submitting code originally written by others (EPL only), preserve the copyright notice found in the original code 

See also: [Eclipse Legal Resources](http://www.eclipse.org/legal/).

##  Continuous builds

Continuous builds run on [Jenkins at
CloudBees](http://textuml.ci.cloudbees.com/). Artifact repository is also
available at [CloudBees Forge](http://repository-
textuml.forge.cloudbees.com/snapshot/).
