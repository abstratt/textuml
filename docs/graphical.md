---
---
#Configuring Graphical Rendering

Note that diagram rendering is optional. If you don't require the
feature, these steps are not needed.

### Download and install Graphviz

If you don't have it yet, download and install
[Graphviz](http://graphviz.org/Download.php "http://graphviz.org/Download.php")
for your platform.

***Note to Windows users:** you might need to download and install the
"[Microsoft Visual C++ 2005 Redistributable Package
(x86)](http://www.microsoft.com/downloads/details.aspx?familyid=32bc1bee-a3f9-4c13-9c99-220b62a191ee&displaylang=en "http://www.microsoft.com/downloads/details.aspx?familyid=32bc1bee-a3f9-4c13-9c99-220b62a191ee&displaylang=en")"
as mentioned in the Graphviz for Windows [download
page](http://graphviz.org/Download_windows.php "http://graphviz.org/Download_windows.php").
From user reports, it seems XP usually requires that package to be
installed, while Vista does not*.

### Configure EclipseGraphviz

Once Graphviz is installed, you need to tell EclipseGraphviz where to
find it. Open the Graphviz preference page in Eclipse and enter the
location for the dot executable.


### How are diagrams rendered?

EclipseGraphviz provides an "Image Viewer" view that will show a
rendered class diagram whenever a .uml file is selected or a TextUML
Viewer editor is currently selected. To open the Image Viewer, go Window
\> Show view \> Other... \> EclipseGraphviz \> Image viewer.

{% include footer.md %}
