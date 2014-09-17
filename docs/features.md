---
---

{% include header.md %}

#Features

Since 2.0
----------------

-   Released: in progress
-   Requirements: Eclipse 4.4 or later, Java 6 or later.

### New textual notation features
-   Type inference for local vars
-   Complex expressions for [parameter default values](https://github.com/abstratt/textuml/issues/36)


Since 1.9
----------------

-   Released: 2013
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
----------------

-   Released: June 2012
-   Requirements: Eclipse 3.6 or later, Java 6 or later.

### New textual notation features

-   try/catch (ExceptionHandler)

Since 1.7
----------------

-   Released: February 2011
-   Requirements: Eclipse 3.5 or later, Java 6 or later.

### Auto-format on save

Now, the TextUML Toolkit can auto-reformat your source files as you save
them. You can turn on this new feature on the new TextUML preference
page.

### Outline options

Now you can control what the editor outline shows: operations,
attributes, classes, data types, associations etc (contributed by Attila
Bak - see
[demo](http://www.youtube.com/watch?v=F3crWuW9yZk "http://www.youtube.com/watch?v=F3crWuW9yZk")).

### New textual notation features

-   stereotypes on operation parameters (see
    [discussion](https://groups.google.com/forum/#!forum/textuml-toolkit))

<!-- -->

    stereotype myParamStereotype extends uml::Parameter end;

    operation op1([myParamStereotype] param1 : Real);

-   readonly modifier on attributes

-   attributes are now public by default (they used to be private)

Since 1.6
----------------

-   Released: July 2010
-   Requirements: Eclipse 3.5 or later.

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
----------------

-   Released: December 2009

### Content assist

There is now (very) early support for content assist (contributed by Attila
Bak), with initial support for stereotype applications.

### Element aliasing

You can now enable
aliasing
by creating [repository
properties](repository_properties.html)
in the form:

    mdd.aliases.<source-qualified-name>=<target-qualified-name>

For instance:

    mdd.aliases.base\:\:Real=mypackage\:\:MyReal

### New textual notation features

There is now textual notation support for decimal
literals.

Since 1.4
----------------

-   Released: November 2009

### New textual notation features

Notation support for applying stereotypes to generalizations and
realizations.

Since 1.3
----------------

-   Released: June 2009

### Integration with diagramming tools

[Stable
ids](http://abstratt.com/blog/2009/04/13/new-in-13-m1-better-integration-with-diagramming-tools/ "http://abstratt.com/blog/2009/04/13/new-in-13-m1-better-integration-with-diagramming-tools/")
allow external diagrams to remain valid as the Toolkit regenerates a UML
model

### New textual notation features

There is now textual notation support for:

-   inter-class
    dependencies
-   enumeration literals in stereotype
    applications

### Compatibility with both Eclipse 3.4 (Ganymede) and 3.5 (Galileo)

A lot of effort was put into making the TextUML Toolkit compatible with
both last year's and this year's versions of Eclipse. Please see
[Install
Instructions](install.html)
for instructions on what update site to use for your Eclipse version.

Since 1.2
----------------

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
----------------

-   Released: September 2008

### More control on how diagrams are rendered

The UML layout preference page (Window \> Preferences... \> Graphviz \>
UML) allows you to control whether structural feature compartments
should be shown and whether to show related elements across packages.

### Export diagram as images

You can now save a rendered UML diagram as a JPG or PNG file. Look for
the new action on the Image Viewer. This is an example of an image file
exported.

### New textual notation features

There is now textual notation support for:

-   abstract operations
-   parameter direction kind modifiers (in, out, inout)

Since 1.0
----------------

-   Released: July 2008

### Automatic compilation

When you save a TextUML source file, your source code is automatically
validated and the corresponding UML model is generated. In case of
errors, problem markers describe any errors that might have occurred.

### Textual browsing

Double-click any [Eclipse UML2 compatible UML
model](http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility "http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility")
(including those generated by the TextUML Toolkit) and browse it using
the TextUML notation.

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
    
{% include footer.md %}
