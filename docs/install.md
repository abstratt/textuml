---
---
{% include header.md %}

# Installing the TextUML Toolkit

Requirements
------------

-   Java 8
-   Eclipse Oxygen or later

Install Eclipse
---------------

-   If you don't have Eclipse Luna or newer, install it from
    [http://eclipse.org/downloads/](http://eclipse.org/downloads/ "http://eclipse.org/downloads/")
-   Start Eclipse

Method 1: Installation via Eclipse Marketplace (preferred)
-------------------------------


If you have the [Eclipse
Marketplace](http://marketplace.eclipse.org/marketplace-client-intro "http://marketplace.eclipse.org/marketplace-client-intro")
client installed, that is the easiest way to install the TextUML
Toolkit. Just search for the Toolkit, and install it directly.

Method 2: Installation via Update Manager 
-------------------------------

-   Open the [Software
    Updates](http://help.eclipse.org/stable/topic/org.eclipse.platform.doc.user/tasks/tasks-121.htm "http://help.eclipse.org/stable/topic/org.eclipse.platform.doc.user/tasks/tasks-121.htm")
    dialog (Help \> Install New Software...), and enter the following
    JAR URL in the "Work with:" field (include jar: to the !/ at the
    end):

<pre>jar:https://dl.bintray.com/abstratt-oss/abstratt-oss/com/abstratt/mdd/com.abstratt.mdd.oss.repository/2.7.201911/com.abstratt.mdd.oss.repository-2.7.201911.zip!/</pre>

-   Select the TextUML Toolkit feature from the Modeling category.

-   Accept to restart Eclipse to make the changes effective.

Method 3: Installation via Update Manager from a local archive (last resort)
-------------------------------

If methods 1 and 2 keep failing for you due to networking issues, there is a last resort.

Manually download the [repository](https://dl.bintray.com/abstratt-oss/abstratt-oss/com/abstratt/eclipsegraphviz/com.abstratt.eclipsegraphviz.repository/2.7.201911/com.abstratt.eclipsegraphviz.repository-2.7.201911.zip), save the ZIP file to your local disk, then follow method 2 using the local file as the update site.

Keep in mind that by doing this you will lose the advantage of automatically getting future updates. You will need to repeat this step manuall in order to get a newer build.

Further steps for graphical diagram rendering
---------------------------------------------

Diagram rendering is optional. If you decided not to install
EclipseGraphviz in the previous step, skip this section, you are done.
But if you do, read [these
instructions](graphical.html "Configuring Graphical Rendering").

Installation complete - now what?
---------------------------------

After the TextUML Toolkit is installed, you must restart Eclipse. Once
that is done, you will see you can now create MDD projects in the New
Project wizard. Any files using the ".tuml" extension are considered
TextUML source files and can be edited with the TextUML editor and will
be compiled by the TextUML compiler. You are ready now to try the
TextUML
Tutorial.

Would you like to run the TextUML compiler as a regular Java application? 
---------------------------------

Check out the [standalone-textuml](http://github.com/abstratt/standalone-textuml) project.

{% include footer.md %}
