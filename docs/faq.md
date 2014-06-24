#### Can a tool that uses a textual notation be considered UML compliant?

Yes. The UML specification separates abstract syntax (what kinds of
elements are available and how they can be used together) from concrete
syntax (how to present those elements in diagrams). The specification
states that there are two different types of compliance:

-   abstract syntax compliance (the types of elements and how they
    relate to each other)
-   concrete syntax compliance (the notation)

A UML tool must provide at least one of the types of compliance. The
TextUML Toolkit aims to be abstract syntax compliant, but not to be
concrete syntax compliant.

#### What are the differences between TextUML and the real UML?

There are no differences. TextUML is not a full language, but instead it
is just a notation for UML's abstract syntax. The semantics are exactly
the same as of UML. Note though that TextUML exposes only a subset of
UML.

#### What diagrams can I create with the TextUML Toolkit?

The TextUML Toolkit is a tool for creating models, not diagrams.
Diagrams and models are [different
things](http://abstratt.com/blog/2008/09/10/diagrams-models/ "http://abstratt.com/blog/2008/09/10/diagrams-models/").

That being said, the primary goal of the TextUML Toolkit is to support
model-driven development and as such the Toolkit can create UML models
with elements that typically appear in the context of class diagrams:
classes, operations, attributes, interfaces, associations,
generalizations, interface realizations, stereotypes, stereotype
applications etc. Since version 1.2, the TextUML Toolkit provides an
action language that produces models with elements that are portrayed in
activity diagrams. And in the future, it is the goal to support modeling
of object dynamics using state machines.

#### I thought UML was all about raising the level of abstraction. Why should I go back to coding?

The notation (syntax) does not determine the level of abstraction, the
semantics does. For the same semantics, different notations will provide
identical levels of abstraction and control. What varies is how easy it
is to understand, modify and create models.

#### Why a textual notation, what is wrong with the graphical one?

Short answer: the TextUML Toolkit is a tool for creating well-formed
**detailed** UML models that will serve as input to code generation. A
textual notation works better for describing and showing the details,
whereas the graphical notation is more suited for providing an overview
of the system (see next question). Long answer: please read
[this](http://abstratt.com/blog/2008/05/05/why-we-write-code-and-dont-just-draw-diagrams/ "http://abstratt.com/blog/2008/05/05/why-we-write-code-and-dont-just-draw-diagrams/").

#### If a textual notation is better, why supporting graphical visualization at all?

The soul of the TextUML Toolkit is the textual notation. You really
don't need anything else if all you want to do is generate code from
your UML models, which is what the tool is intended for. However, for an
overview of the relationships between the elements in the model
(inheritance, associations), the graphical notation is usually better.
Also, some level of support for the graphical notation (even if just for
visualization purposes) potentially makes the product more appealing to
a broader audience.

Moreover, the graphical visualization is not provided by the TextUML
Toolkit per se, but by an optional component the Toolkit relies upon.

#### Does the TextUML Toolkit work with tool XYZ?

The TextUML Toolkit uses [Eclipse
UML2](http://wiki.eclipse.org/MDT-UML2 "http://wiki.eclipse.org/MDT-UML2")
for reading and writing models. The TextUML Toolkit is expected to be
compatible with any tools that rely on the same component. A list of
such tools can be found
[here](http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility "http://wiki.eclipse.org/MDT-UML2-Tool-Compatibility").

#### How much does the TextUML Toolkit cost?

The TextUML Toolkit is available free of charge and it will always be.

#### Is the TextUML Toolkit open source?

Yes. The TextUML Toolkit became an open-source project (EPL) in August
2008, shortly after version 1.0 was released.

#### What is the relationship between the TextUML Toolkit and EclipseGraphviz?

The TextUML Toolkit depends on other tools for showing models using the
graphical notation. Right now, the only tool supported is the UML viewer
that is part of
[EclipseGraphviz](http://eclipsegraphviz.sf.net/ "http://eclipsegraphviz.sf.net"),
an open source project that integrates Graphviz into Eclipse. But others
are possible.

#### Still have a question?

Head to the
[forum](http://abstratt.com/forum/ "http://abstratt.com/forum/"), or add
a comment to a recent [blog
post](http://abstratt.com/blog/ "http://abstratt.com/blog/").
