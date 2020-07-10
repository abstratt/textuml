<html>

<head>
<meta http-equiv="Content-Language" content="en-us">
<title>textuml.compile Task</title>
</head>

<body>

<h2>textuml.compile</h2>
<h3>Description</h3>
<p>Compiles *.tuml resources. Can be executed for a file, or one or more nested
   <a href="../CoreTypes/resources.html#collection">Resource Collection</a>s.
<h3>Parameters</h3>
<table border="1" cellpadding="2" cellspacing="0">
  <tr>
    <td valign="top"><b>Attribute</b></td>
    <td valign="top"><b>Description</b></td>
    <td align="center" valign="top"><b>Required</b></td>
  </tr>
  <tr>
    <td valign="top">dest</td>
    <td valign="top">Destination directory.</td>
    <td valign="top" align="center">No</td>
  </tr>
  <tr>
    <td valign="top">file</td>
    <td valign="top">Single file to compile.</td>
    <td valign="top" >No.</td>
  </tr>
  <tr>
    <td valign="top">clean</td>
    <td valign="top">Set mode to ICompilationDirector.CLEAN. Default is true.</td>
    <td valign="top">No</td>
  </tr>
  <tr>
    <td valign="top">fullBuild</td>
    <td valign="top">Set mode to ICompilationDirector.FULL_BUILD. Default is true.</td>
    <td valign="top">No</td>
  </tr>
  <tr>
    <td valign="top">debug</td>
    <td valign="top">Set mode to ICompilationDirector.DEBUG. Default is false.</td>
    <td valign="top">No</td>
  </tr>
</table>

<h3>Parameters specified as nested elements</h3>

<h4>Resource Collections</h4>
<p>You can include resources via nested Resource Collection's.</p>

<h3>Examples</h3>

<pre> 
       &lt;textuml.compile  dest="." clean="false"&gt;
        	&lt;fileset dir="." includes="*.tuml"/&gt;
       &lt;/textuml.compile&gt;

</pre>
<p>Compiles all *.tuml files in current direcoty.</p>

</body>
</html>

