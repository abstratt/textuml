
Since 1.9
=========

-   Released: in progress
-   Requirements: Eclipse 3.8 or later, Java 6 or later.

### New textual notation features

-   state machines
    ([intro](http://abstratt.com/blog/2012/03/07/adding-state-machines-to-textuml-and-alphasimple-take-1/ "http://abstratt.com/blog/2012/03/07/adding-state-machines-to-textuml-and-alphasimple-take-1/"))

-   simpler single expression blocks (breaking change)

Instead of:

    derived attribute employeeCount : Integer := ():Integer { return self->employees.size(); }; 

use:

    derived attribute employeeCount : Integer := { self->employees.size() }; 

The same syntax can be used for assigning a default value for a
non-derived attribute:

    attribute employeeStartDate : Date := { Date#today() };

Since 1.8
=========

-   Released: June 2012
-   Requirements: Eclipse 3.6 or later, Java 6 or later.

### New textual notation features

-   try/catch (ExceptionHandler)

Since 1.7
=========

-   Released: February 2011
-   Requirements: Eclipse 3.5 or later, Java 6 or later.

### Auto-format on save

Now, the TextUML Toolkit can auto-reformat your source files as you save
them. You can turn on this new feature on the new TextUML preference
page.

[![File:Preferences-1.7.png](./SourceForge.net%20%20TextUML%20Toolkit%20Features%20-%20textuml_files/Preferences-1.7.png)](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=File:Preferences-1.7.png "File:Preferences-1.7.png")

### Outline options

Now you can control what the editor outline shows: operations,
attributes, classes, data types, associations etc (contributed by Attila
Bak - see
[demo](http://www.youtube.com/watch?v=F3crWuW9yZk "http://www.youtube.com/watch?v=F3crWuW9yZk")).

### New textual notation features

-   stereotypes on operation parameters (see
    [discussion](http://sourceforge.net/projects/textuml/forums/forum/855420/topic/3841616 "http://sourceforge.net/projects/textuml/forums/forum/855420/topic/3841616"))

<!-- -->

    stereotype myParamStereotype extends uml::Parameter end;

    operation op1([myParamStereotype] param1 : Real);

-   readonly modifier on attributes

-   attributes are now public by default (they used to be private)

Since 1.6
=========

-   Released: July 2010
-   Requirements: Eclipse 3.5 or later. 3.4 supported using an
    [alternate update
    site](http://abstratt.com/update/3.4/ "http://abstratt.com/update/3.4/").

### New textual notation features

-   preconditions on operations

<!-- -->

    operation withdraw(amount : Real);
    precondition { amount > 0 and amount < self.balance; }
    begin
        self.balance := self.balance - amount;  
    end;

-   derived properties

<!-- -->

    (...)

    reference employees : Employee[*]

    /* calculated field */
    derived attribute employeeCount : Integer := ():Integer { return self->employees.size(); }; 

    (...)

-   initial values on properties

<!-- -->

    attribute available : Boolean := true;

Since 1.5
=========

-   Released: December 2009

### Content assist

There is now early support for content assist (contributed by Attila
Bak), with initial support for stereotype applications.

[![File:Content-assist.png](./SourceForge.net%20%20TextUML%20Toolkit%20Features%20-%20textuml_files/Content-assist.png)](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=File:Content-assist.png "File:Content-assist.png")

### Element aliasing

You can now enable
[aliasing](http://sourceforge.net/tracker/?func=detail&aid=2899590&group_id=236545&atid=1099987 "http://sourceforge.net/tracker/?func=detail&aid=2899590&group_id=236545&atid=1099987")
by creating [repository
properties](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=Repository_Properties "Repository Properties")
in the form:

    mdd.aliases.<source-qualified-name>=<target-qualified-name>

For instance:

    mdd.aliases.base\:\:Real=mypackage\:\:MyReal

### New textual notation features

There is now textual notation support for [decimal
literals](http://sourceforge.net/tracker/?func=detail&aid=2820250&group_id=236545&atid=1099987 "http://sourceforge.net/tracker/?func=detail&aid=2820250&group_id=236545&atid=1099987").

Since 1.4
=========

-   Released: November 2009

### New textual notation features

Notation support for [applying stereotypes to generalizations and
realizations](http://sourceforge.net/tracker/?func=detail&aid=2876108&group_id=236545&atid=1099987 "http://sourceforge.net/tracker/?func=detail&aid=2876108&group_id=236545&atid=1099987").

Since 1.3
=========

-   Released: June 2009

### Integration with diagramming tools

[Stable
ids](http://abstratt.com/blog/2009/04/13/new-in-13-m1-better-integration-with-diagramming-tools/ "http://abstratt.com/blog/2009/04/13/new-in-13-m1-better-integration-with-diagramming-tools/")
allow external diagrams to remain valid as the Toolkit regenerates a UML
model

### New textual notation features

There is now textual notation support for:

-   [inter-class
    dependencies](http://sourceforge.net/tracker/?func=detail&aid=2797252&group_id=236545&atid=1099987 "http://sourceforge.net/tracker/?func=detail&aid=2797252&group_id=236545&atid=1099987")
-   [enumeration literals in stereotype
    applications](http://sourceforge.net/tracker/?func=detail&aid=2804583&group_id=236545&atid=1099987 "http://sourceforge.net/tracker/?func=detail&aid=2804583&group_id=236545&atid=1099987")

### Compatibility with both Eclipse 3.4 (Ganymede) and 3.5 (Galileo)

A lot of effort was put into making the TextUML Toolkit compatible with
both last year's and this year's versions of Eclipse. Please see
[Install
Instructions](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=Install_Instructions "Install Instructions")
for instructions on what update site to use for your Eclipse version.

Since 1.2
=========

-   Released: February 2009

### New textual notation features

There is now textual notation support for:

-   [primitive
    types](http://abstratt.com/blog/2008/12/02/feature-primitive-types "http://abstratt.com/blog/2008/12/02/feature-primitive-types")
-   [data types (a.k.a.
    structs)](http://abstratt.com/blog/2008/11/28/feature-data-types/ "http://abstratt.com/blog/2008/11/28/feature-data-types/")
-   [required extensions for
    prototypes](http://abstratt.com/blog/2008/11/11/feature-required-extensions-for-stereotypes/ "http://abstratt.com/blog/2008/11/11/feature-required-extensions-for-stereotypes/")
-   [behaviour modeling (a.k.a. action
    semantics)](http://abstratt.com/blog/2008/11/07/executable-models-with-textuml-toolkit-12-m1/ "http://abstratt.com/blog/2008/11/07/executable-models-with-textuml-toolkit-12-m1/")
-   [shorthand notation for aggregation and
    composition](http://abstratt.com/blog/2008/09/16/feature-shorthand-notation-for-aggregation-and-composition/ "http://abstratt.com/blog/2008/09/16/feature-shorthand-notation-for-aggregation-and-composition/")
-   abstract stereotypes

### Cross-project references

Share models across projects using the "Project References" tab in the
Project Properties dialog.

Since 1.1
=========

-   Released: September 2008

### More control on how diagrams are rendered

The UML layout preference page (Window \> Preferences... \> Graphviz \>
UML) allows you to control whether structural feature compartments
should be shown and whether to show related elements across packages.

[![](./SourceForge.net%20%20TextUML%20Toolkit%20Features%20-%20textuml_files/NaN-1_1-UML-prefs.png)](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=File:NaN-1_1-UML-prefs.png "NaN-1 1-UML-prefs.png")

### Export diagram as images

You can now save a rendered UML diagram as a JPG or PNG file. Look for
the new action on the Image Viewer. This is an example of an image file
exported:

[![](./SourceForge.net%20%20TextUML%20Toolkit%20Features%20-%20textuml_files/NaN-1_1-export-image.png)](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=File:NaN-1_1-export-image.png "NaN-1 1-export-image.png")

### New textual notation features

There is now textual notation support for:

-   abstract operations
-   parameter direction kind modifiers (in, out, inout)

Since 1.0
=========

-   Released: July 2008

### Automatic compilation

When you save a TextUML source file, your source code is automatically
validated and the corresponding UML model is generated. In case of
errors, problem markers describe any errors that might have occurred.

### Textual browsing

Double-click any [Eclipse UML2 compatible UML
model](http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility "http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility")
(including those generated by the TextUML Toolkit) and browse it using
the TextUML notation. See, for example, the [UML Metamodel In
TextUML](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=UML_Metamodel_In_TextUML "UML Metamodel In TextUML").

### Use models created by other tools

You can use any [Eclipse UML2 compatible UML
model](http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility "http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility")
in your TextUML source. Just drop them at the root of the project and
they will instantly become available to your models created with the
TextUML Toolkit.

### Graphical browsing

The Image Viewer (Window \> Show view \> Other... \> EclipseGraphviz \>
Image Viewer) shows a bird's-eye view of the model you are currently
editing using the graphical notation. You can also just select a UML
file and it will be automatically rendered on the Image Viewer. Use the
UML layout preference page (Window \> Preferences... \> Graphviz \> UML)
gives you some control on the layout of class diagrams.

### Source formatter

Hit Ctrl-Shift-F in the TextUML editor and your source is automatically
formatted. Currently there are no preferences for customizing the
formatter.

### And more

-   Syntax highlighting
-   Outline view gives you an overview of the structure of your source
    file and allows you to quickly jump to any element
-   Textual comparison makes working in a team and comparing versions
    easier

See also
========

-   [Install
    Instructions](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=Install_Instructions "Install Instructions")
-   [TextUML
    Tutorial](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=TextUML_Tutorial "TextUML Tutorial")
-   [TextUML
    Guide](http://sourceforge.net/apps/mediawiki/textuml/index.php?title=TextUML_Guide "TextUML Guide")
