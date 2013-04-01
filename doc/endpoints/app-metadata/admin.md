# Table of Contents

* [Overview](#overview)
    * [Deployed Components](#deployed-components)
    * [Templates](#templates)
        * [Property Groups](#property-groups)
        * [Properties](#properties)
        * [Property Types](#property-types)
        * [Value Types](#value-types)
        * [Validators](#validators)
        * [Rules](#rules)
        * [Rule Arguments](#rule-arguments)
        * [Data Objects](#data-objects)
        * [Data Object Multiplicity](#data-object-multiplicity)
        * [Data Sources](#data-sources)
        * [Info Types](#info-types)
        * [Data Formats](#data-formats)
    * [Apps](#apps)
        * [Transformation Activities](#transformation-activities)
        * [Transformation Activity References](#transformation-activity-references)
        * [Ratings](#ratings)
        * [Suggested Groups](#suggested-groups)
        * [Transformation Steps](#transformation-steps)
        * [Transformations](#transformations)
        * [Input/Output Mappings](#input/output-mappings)
        * [Data Object Mappings](#data-object-mappings)
        * [Transformation Values](#transformation-values)
        * [Template Groups](#template-groups)
* [App Metadata Administration Services](#app-metadata-administration-services)
    * [Exporting a Template](#exporting-a-template)
    * [Exporting an Analysis](#exporting-an-analysis)
    * [Exporting Selected Deployed Components](#exporting-selected-deployed-components)
    * [Permanently Deleting an Analysis](#permanently-deleting-an-analysis)
    * [Logically Deleting an Analysis](#logically-deleting-an-analysis)
    * [Previewing Templates](#previewing-templates)
    * [Previewing Analyses](#previewing-analyses)
    * [Updating an Existing Template](#updating-an-existing-template)
    * [Updating an Analysis](#updating-an-analysis)
    * [Forcing an Analysis to be Updated](#forcing-an-analysis-to-be-updated)
    * [Importing a Template](#importing-a-template)
    * [Importing an Analysis](#importing-an-analysis)
    * [Importing Tools](#importing-tools)
    * [Updating Top-Level Analysis Information](#updating-top-level-analysis-information)
    * [Updating the Favorite Analyses List](#updating-the-favorite-analyses-list)

# Overview

The app metadata model used by the DE has three major types of components:
_deployed components_, _templates_ and _apps_.

Deployed components represent tools that have been deployed within the Discovery
Environment. Currently, these refer to command-line tools that can be executed
in the Discovery Environment, either from within the Discovery Environment's
Condor cluster or on the HPC resources at TACC.

Templates represent a single use of a deployed component, including command-line
arguments and options that the deployed component supports. One important thing
to keep in mind is that a template does not have to describe every possible use
of a deployed component and it is common for multiple templates to be used to
describe distinct usages of a single deployed component. For example, it would
be perfectly reasonable to have two templates for the Unix utility, `tar`: one
for extracting files from a tarball and another for building a tarball. The
structure of a template is fairly deep and complex, and will be described in
more detail later.

Apps, represent groups of one or more templates that can be run by a user from
within the Discovery Environment. A template cannot be used directly by a user
without being included in an app. And a single app may contain multple templates
strung together into a pipeline. Note that the `/import-template` service in
metadactyl automatically generates a single-step app containing that template.
This is done as a convenience because single-step apps are common.

A fourth type of component, _notification sets_, was supported at one time, but
it is no longer supported as of DE version 1.8. Some vestiges of notification
sets still exist, but they are no longer used by the DE. All remaining support
for them will be removed at some point in the future.

It should be noted that a lot of tables in the database have elements that
aren't being used in the Discovery Environments. They may be populated using the
app metadata import services, but they're ignored. It would be nice to get rid
of these fields, but they're being retained for the time being because the
Hibernate object mappings that we're using currently requires them to be there.
The unused fields will be removed if and when we get around to revamping the
database schema.

## Deployed Components

Each deployed component contains information about a command-line tool that is
deployed in the Discovery Environment. This includes the path to the directory
containing the executable file, the name of the executable file and several
pieces of information to determine how the tool is executed:

<table border="1">
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the executable file.</td>
        </tr>
        <tr>
            <td>Location</td>
            <td>The path to the directory containing the executable file.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the tool.</td>
        </tr>
        <tr>
            <td>Version</td>
            <td>The tool version.</td>
        </tr>
        <tr>
            <td>Attribution</td>
            <td>
                Information about the people or entities that created the tool.
            </td>
        </tr>
        <tr>
            <td>Integration Data</td>
            <td>Information related to the tool installation request.</td>
        </tr>
        <tr>
            <td>Tool Type</td>
            <td>The type of the tool.</td>
        </tr>
    </tbody>
</table>

The integration data and tool type both deserve special attention. The
integration data includes the name and email address of the person who requested
that the tool be installed along with example input files and expected output
files for a test run of the tool. The tool type indicates where the utility
runs. There are currently two available tool types: `executable`, which
indicates that the tool runs on the Discovery Environment's Condor cluster, and
`fAPI`, which indicates that the job is submitted to the Foundation API.

## Templates

As mentioned above, each template describes one possible use of a deployed
component. This includes descriptions of all of the options and command-line
arguments required for that use of the deployed component. The template
structure is nested fairly deeply, so we'll start with a brief description of
each level in the structure:

<table>
    <thead>
        <tr>
            <th>Structure Level</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Template</td>
            <td>The top level of the template structure</td>
        </tr>
        <tr>
            <td>Property Group</td>
            <td>
                Represents a group of related options or command-line arguments.
            </td>
        </tr>
        <tr>
            <td>Property</td>
            <td>Represents a single option or command-line argument.</td>
        </tr>
        <tr>
            <td>Property Type</td>
            <td>Indicates the type of information accepted by the property.</td>
        </tr>
        <tr>
            <td>Value Type</td>
            <td>
                Indicates the type of value associated with a property type.
            </td>
        </tr>
        <tr>
            <td>Validator</td>
            <td>Indicates how property values should be validated.</td>
        </tr>
        <tr>
            <td>Rule</td>
            <td>Represents one rule for validating a property value.</td>
        </tr>
        <tr>
            <td>Rule Type</td>
            <td>Indicates how rule arguments should be interpreted.</td>
        </tr>
        <tr>
            <td>Rule Argument</td>
            <td>Provies an argument to a rule.</td>
        </tr>
        <tr>
            <td>Data Object</td>
            <td>Represents one or more input or output files.</td>
        </tr>
        <tr>
            <td>Multiplicity</td>
            <td>
                Indicates the number of input or output files accepted or
                produced by a tool for a specific data object.
            </td>
        </tr>
        <tr>
            <td>Data Source</td>
            <td>
                Indicates where the data for an output data object originates.
            </td>
        <tr>
            <td>Info Type</td>
            <td>
                Represents the type of information in an input or output file.
            </td>
        </tr>
        <tr>
            <td>Data Format</td>
            <td>Represents the format of an input or output file.</td>
        </tr>
    </tbody>
</table>

There are several fields associated with the top level of the template
structure, most of which are largely ignored. Aside from the identifier, the
only field that is commonly used is the deployed component identifier. The name
field is used by the import service in some cases, which will be described
later, but it is not used otherwise.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        <tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the template.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the template.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>A display label for the template.</td>
        </tr>
        <tr>
            <td>Type</td>
            <td>The type of the template.</td>
        </tr>
        <tr>
            <td>Deployed Component Identifier</td>
            <td>A reference to the deployed component.</td>
        </tr>
    </tbody>
</table>

### Property Groups

A property group is nothing more than a way to group related options and
command-line arguments. This allows the Discovery Environment to group related
things together in the user interface.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the property group.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the property group.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The display label for the property group.</td>
        </tr>
        <tr>
            <td>Property Group Type</td>
            <td>The type of the property group.</td>
        </tr>
        <tr>
            <td>Visibility Flag</td>
            <td>Indicates whether or not the group is displayed in the DE.</td>
        </tr>
    </tbody>
</table>

The property group label is used as the label of an accordion panel in the UI
that is generated for the template. The visibility flag indicates whether or not
the property group should be displayed in the UI. Hidden property groups can be
useful for grouping properties that are not configurable by the end user.

### Properties

A property represents a single option or command-line argument for a tool. This
is where things start to get a little more interesting. Properties come in
several different types, which indicate the type of information accepted by the
property.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The command-line flag to use for the property.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the property.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>
                The display label for the property. This field determines how
                the widget associated with the property will be labeled in the
                UI.
            </td>
        </tr>
        <tr>
            <td>Default Value</td>
            <td>
                The value used for the property if none is provided. If this
                field is blank then the default value is also blank.
            </td>
        </tr>
        <tr>
            <td>Visibility Flag</td>
            <td>
                Indicates if the property is visible in the UI. Hidden
                properties are useful for settings that have to be passed to
                the tool but are not configruable by end users.
            </td>
        </tr>
        <tr>
            <td>Order Index</td>
            <td>
                Indicates the relative command-line order of the property. For
                example, if a property with an order index of 1 will be included
                first on the command line and a property with an order index of
                2 will be second.
            </td>
        </tr>
        <tr>
            <td>Property Type</td>
            <td>
                The type of information accepted by the property. In the
                database, this field is a foreign key into a table that lists
                all of the property types that are supported by the DE.
            </td>
        </tr>
        <tr>
            <td>Validator</td>
            <td>
                Information about how to validate property values, if
                applicable.
            </td>
        </tr>
        <tr>
            <td>Data Object</td>
            <td>
                Information about an output or input file associated with the
                property, if applicable.
            </td>
        </tr>
        <tr>
            <td>Omit if Blank Flag</td>
            <td>
                Indicates whether or not the property should be omitted from the
                command line if its value is blank.
            </td>
        </tr>
    </tbody>
</table>

### Property Types

A property type indicates both how an option or command-line argument is
presented to the user and what type of information is accepted by it. There must
be one property type per user interface widget available in the Discovery
Environment. Because code has to be written for each property type, users may
not enter new property types. Instead, property types are only added by database
initialization and conversion scripts.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name display name of the property type.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the property type.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>Not used.</td>
        </tr>
        <tr>
            <td>Deprecated</td>
            <td>
                Indicates whether or not the property type may be used in new
                apps.
            </td>
        </tr>
        <tr>
            <td>Display Order</td>
            <td>
                Determines the order in which property types are listed in the
                DE.
            </td>
        </tr>
        <tr>
            <td>Value Type</td>
            <td>
                Indicates the type of value associated with the property type,
                which determines what types of rules can be applied to
                proerties of this type.
            </td>
        </tr>
    </tbody>
</table>

### Value Types

A value type indicates what type of value is associated with a specific type of
property, which determines which types of rules may be used to validate
properties of a given type. As with property types, value types may not be added
to the Discovery Environment by end users.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the value type.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the value type.</td>
        </tr>
    </tbody>
</table>

### Validators

A validator determines how user input for a property is validated. Not all
properties have to be validated, so some of them will not have validators
associated with them.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the validator.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the validator.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The validator label.</td>
        </tr>
        <tr>
            <td>Required</td>
            <td>
                Indicates whether or not the property associated with the
                validator requires a non-blank value.
            </td>
        </tr>
        <tr>
            <td>Rules</td>
            <td>
                The list of validation rules associated with this validator.
            </td>
    </tbody>
</table>

### Rules

A rule provides a way to validate a property value. Multiple rules may be
associated with a single validator so that property values may be validated in
multiple steps or in multiple ways.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the rule.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the rule.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The rule label.</td>
        </tr>
        <tr>
            <td>Rule Type</td>
            <td>Indicates how the validation is performed.</td>
        </tr>
        <tr>
            <td>Value Types</td>
            <td>The types of values that the rule may be applied to.</td>
        </tr>
        <tr>
            <td>Rule Arguments</td>
            <td>Arguments that are passed to the rule.</td>
        </tr>
    </tbody>
</table>

### Rule Arguments

A rule argument provides a single argument to a rule. For example, a property
whose value should be an integer greater than 5 could be validated using the
rule type `IntAbove` with a single argument of `5`.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Argument Value</td>
            <td>The value used for this argument.</td>
        </tr>
        <tr>
            <td>Hibernate ID</td>
            <td>
                This field is misnamed; it determines the order in which the
                arguments are passed to the rule.
            </td>
        </tr>
    </tbody>
</table>

### Data Objects

A data object describes either an input file that is accepted by a deployed
component or an output file that is produced by one. Data objects are closely
related to properties, but were listed separately from properties at one point
in time. Because of that, there are a lot of redundant fields in properties and
data objects.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>
                This field is overloaded. It'll be described in more detail
                later.
            </td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The data object label.</td>
        </tr>
        <tr>
            <td>Order</td>
            <td>
                The relative order in which the data object appears on the
                command line.
            </td>
        </tr>
        <tr>
            <td>Switch</td>
            <td>The command line flag used for the data object.</td>
        </tr>
        <tr>
            <td>Info Type</td>
            <td>
                Indicates the type of information represented by the data
                object.
            </td>
        </tr>
        <tr>
            <td>Data Format</td>
            <td>
                Indicates the format of the information contained in the data
                object.
            </td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the data object.</td>
        </tr>
        <tr>
            <td>Required</td>
            <td>
                Indicates whether an input data object is required or a name for
                an output data object must be provided for an app to run.
            </td>
        </tr>
        <tr>
            <td>Multiplicity</td>
            <td>
                Indicates the number of files accepted or produced by the app
                for a data object.
            </td>
        </tr>
        <tr>
            <td>Retain</td>
            <td>
                Indicates whether or not an input data object should be retained
                by the Discovery Environment.
            </td>
        </tr>
        <tr>
            <td>Implicit</td>
            <td>
                Indicates that the data object is not specified on the command
                line.
            </td>
        </tr>
        <tr>
            <td>Data Source</td>
            <td>
                Indicates where the data for an output data object originates.
            </td>
        </tr>
    </tbody>
</table>

### Data Object Multiplicity

The multiplicity indicates how many files are accepted or produced by an app for
a data object. The currently available multiplicities are `Many`, `Single` and
`Folder`. `Many` indicates that multiple files are accepted or produced by the
app. For input files, it must be possible to pass the names of the files to the
app on the command line. For output files, the names of the files must match a
glob expression so that the DE can retrieve them after the job completes.
`Single` indicates that only one file is either accepted or produced by the app.
`Folder` indicates that the contents of a folder on the file system are either
accepted or produced by the app.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the multiplicity.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The dislpay label for the multiplicity</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the multiplicity.</td>
        </tr>
        <tr>
            <td>Type Name</td>
            <td>The name of the control type used for the multiplicity.</td>
        </tr>
    </tbody>
</table>

### Data Sources

The data source associated with an output data source indicates where the data
in the data object originates. The currently available data sources are `File`,
`Standard Output` and `Standard Error Output`. A data source of `File` indicates
that the app produces the file directly. A data source of `Standard Output`
indicates that the app sends the data to the standard output stream. Finally, a
data source of `Standard Error Output` indicates that the app sends the data to
the standard error output stream.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <body>
        <tr>
            <td>name</td>
            <td>The name of the data source.</td>
        </tr>
        <tr>
            <td>label</td>
            <td>The display name for the data source.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the data source.</td>
        </tr>
    </tbody>
</table>

### Info Types

Info types represent the type of information contained in a data object. Some
examples of this are phylogenetic tree files, and plain text files.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the information type.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>The display label for the information type.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the information type.</td>
        </tr>
        <tr>
            <td>Deprecated</td>
            <td>
                Indicates whether the information type may be used in new apps.
            </td>
        </tr>
        <tr>
            <td>Display Order</td>
            <td>The order in which info types appear in drop-down lists.</td>
        </tr>
    </tbody>
</table>

### Data Formats

Data formats indicate the format of data in a data object. Some examples of this
are FASTA and Genbank.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the data format.</td>
        </tr>
        <tr>
            <td>Label</td>
            <td>the display label for the data format.</td>
        </tr>
        <tr>
            <td>Display Order</td>
            <td>The order in which data formats appear in drop-down lists.</td>
        </tr>
    </tbody>
</table>

## Apps

In the discovery environment, an app is a runnable collection of one or more
templates. If an app contains more than one template, typically the output files
from all but the last step are fed into subsequent steps as input files so that
the data being processed may be transformed in multiple steps.  Haps contain the
following components:

<table>
    <thead>
        <tr>
            <th>Component Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Transformation Activity</td>
            <td>The component that describes the app itself.</td>
        </tr>
        <tr>
            <td>Transformation Activity References</td>
            <td>Links to references related to the app.</td>
        </tr>
        <tr>
            <td>Ratings</td>
            <td>Information about ratings that users have given to apps.</td>
        </tr>
        <tr>
            <td>Suggested Groups</td>
            <td>
                References to app categories that the user suggests for an app.
            </td>
        </tr>
        <tr>
            <td>Transformation Steps</td>
            <td>A single step in an app.</td>
        </tr>
        <tr>
            <td>Transformation</td>
            <td>A data transformation performed in a step.</td>
        </tr>
        <tr>
            <td>Input/Output Mapping</td>
            <td>
                Maps outputs of one step to inputs of a subsequent step.
            </td>
        </tr>
        <tr>
            <td>Data Object Mapping</td>
            <td>
                Maps a single output of one step to a single input of a
                subsequent step.
            </td>
        </tr>
        <tr>
            <td>Transformation Values</td>
            <td>
                Values that should be applied to properties without prompting
                the user.
            </td>
        </tr>
        <tr>
            <td>Template Groups</td>
            <td>A categorization for one or more apps.</td>
        </tr>
    </tbody>
</table>

### Transformation Activities

A transformation activity is the top-level element of an app. It contains the
app name and identifier along with references to the steps that comprise the
app.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the app.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the app.</td>
        </tr>
        <tr>
            <td>Workspace</td>
            <td>The user's workspace that the app is associated with.</td>
        </tr>
        <tr>
            <td>Type</td>
            <td>A short description of what the app does.</td>
        </tr>
        <tr>
            <td>Deleted</td>
            <td>Indicates whether the app should be displayed in the DE.</td>
        </tr>
        <tr>
            <td>Integration Data</td>
            <td>Information about the user who integrated the app.</td>
        </tr>
        <tr>
            <td>Wiki URL</td>
            <td>A link to the app's documentation.</td>
        </tr>
        <tr>
            <td>Integration Date</td>
            <td>The date that the app was integrated into the DE.</td>
        </tr>
        <tr>
            <td>Disabled</td>
            <td>Indicates whether the app can be executed.</td>
        </tr>
        <tr>
            <td>Edited Date</td>
            <td>The date that the app was most recently edited.</td>
        </tr>
        <tr>
            <td>Ratings</td>
            <td>The ratings that users have given to the app.</td>
        </tr>
        <tr>
            <td>Suggested Groups</td>
            <td>A list of groups that the intergator suggests for the app.</td>
        </tr>
        <tr>
            <td>Transformation Activity Mappings</td>
            <td>A list of all of the input/output mappings for the app.</td>
        </tr>
        <tr>
            <td>Transformation Activity References</td>
            <td>A list of links to references related to the app.</td>
        </tr>
        <tr>
            <td>Transformation Steps</td>
            <td>The list of steps in the app.</td>
        </tr>
    </tbody>
</table>

A few fields here deserve some special attention. The _Deleted_ field indicates
whether or not the app should be considered deleted in the DE, which typically
does not completely remove apps from the database for a couple of different
reasons. First, the app may have been used in user's experiments in the past, so
it may be necessary to retrieve information about the app so that information
about the app is still accessible. Sedond, it may be necessary to restore an app
at some point in the future.

The _Disabled_ field indicates whether or not the app should be considered
disabled. A disabled app will appear in the DE, but cannot be executed. This
allows DE administrators to temporarilty prevent an app from being executed,
which can be useful for apps that are currently not functioning, for example.

The _Suggested Groups_ field contains the list of groups that an app integrator
thinks is appropriate for the app when an app is first made available for public
use. When the app is initially made public, it is placed in the _Beta_ category.
After some period of time, a DE administrator might decide to move the app out
of the _Beta_ category and into another, more permanent, category. This field
serves as a list of suggestions for where the DE administrator might put the
app.

The _Transformation Activity Mappings_ field contains a list of all of the
input/output mappings associated with an app. This relationship serves merely as
a convenience for the code that submits jobs for execution; the input/output
mappings can also be accessed via the list of transformation steps.

### Transformation Activity References

Transformation Activity References contain links to reference information
related to an app. For example, apps that use tools in the FASTX Toolkit tend to
reference its home page.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Reference Text</td>
            <td>The link to the reference material.</td>
        </tr>
    </tbody>
</table>

The _Reference Text_ field is a text field, so it would be possible to store
actual reference text in this field. In practice, however, this is not commonly
done. Instead, this field normally just contains a link to the actual reference
information.

### Ratings

DE Users have the ability to rate apps based on perceived usefulness. This
entity contains information about ratings that users have given to apps.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>User</td>
            <td>The user who rated the app.</td>
        </tr>
        <tr>
            <td>App</td>
            <td>The app being rated.</td>
        </tr>
        <tr>
            <td>Rating</td>
            <td>The numeric rating given to the app.</td>
        </tr>
        <tr>
            <td>Comment ID</td>
            <td>The identifier of the comment in Confluence.</td>
        </tr>
    </tbody>
</table>

### Suggested Groups

As mentioned above, suggested groups are recommendations of app categories that
the app integrator thinks might be appropriate for the app when it's moved from
the _Beta_ category. When a DE administrator moves an app from the _Beta_
category to a more permanent category, one of the categories in this list of
suggestions is typically chosen.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Transformation Activity</td>
            <td>The app that is being made public.</td>
        </tr>
        <tr>
            <td>Template Group</td>
            <td>The suggested app category</td>
        </tr>
    </tbody>
</table>

### Transformation Steps

A transformation step represents a single step in an app. Each step eventually
maps to a single template. For multi-step apps, each step is referenced by a set
of input/output mappings, which are used to map the outputs from one step to the
inputs of a subsequent step.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the step.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the step.</td>
        </tr>
        <tr>
            <td>Transformation</td>
            <td>The transformation, which refers to the template.</td>
        </tr>
    </tbody>
</table>

### Transformations

A transformation represents a way to customize a template by specifying constant
values for certain properties. The original purpose of a transformation was to
foster template reuse, but transformations tend not to be used for this purpose
in practice. Instead, transformations just serve as an empty link from a
transformation step to a template.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The name of the transformation.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the transformation.</td>
        </tr>
        <tr>
            <td>Template ID</td>
            <td>
                The external identifier of the template used by the
                transformation. This is often a point of confusion because the
                template ID is not listed as a foreign key in the database.
            </td>
        </tr>
        <tr>
            <td>Transformation Values</td>
            <td>The property values specified by the transformation.</td>
        </tr>
    </tbody>
</table>

### Input/Output Mappings

Input/output mappings only apply to multi-step apps; they indicate which outputs
of one step should be applied to which inputs of a subsequent step.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Source</td>
            <td>The source transformation step</td>
        </tr>
        <tr>
            <td>Target</td>
            <td>The target transformation step</td>
        </tr>
        <tr>
            <td>Data Object Mappings</td>
            <td>The actual data object mappings.</td>
        </tr>
    </tbody>
</table>

### Data Object Mappings

Data object mappings only apply to multi-step apps; they map an output of one
step to the input of a subsequent step.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Mapping</td>
            <td>The associated input/output mapping.</td>
        </tr>
        <tr>
            <td>Input</td>
            <td>The external data object ID for the input.</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>The external data object ID for the output.</td>
        </tr>
    </tbody>
</table>

### Transformation Values

A transformation value specifies a constant value for a property inside a
template referenced by a transformation.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Transformation</td>
            <td>The transformation to which this value applies.</td>
        </tr>
        <tr>
            <td>Value</td>
            <td>The constant property value.</td>
        </tr>
        <tr>
            <td>Property</td>
            <td>
                The identifier of the property to assign the constant value to.
            </td>
        </tr>
    </tbody>
</table>

### Template Groups

Template groups are somewhat misnamed. A template group doesn't contain a set of
templates; it contains a set of apps. Template groups can also contain other
template groups so that apps can be grouped hierarchically. Template groups are
currently restricted to containing either apps or subgroups, but not both.

<table>
    <thead>
        <tr>
            <th>Field Name</th>
            <th>Field Value</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Name</td>
            <td>The display name of the template group.</td>
        </tr>
        <tr>
            <td>Description</td>
            <td>A brief description of the template group.</td>
        </tr>
        <tr>
            <td>Workspace</td>
            <td>The workspace that the template group belongs to.</td>
        </tr>
        <tr>
            <td>Subgroups</td>
            <td>The list of subgroups in the current group.</td>
        </tr>
        <tr>
            <td>Templates</td>
            <td>The list of templates in the current group.</td>
        </tr>
    </tbody>
</table>

# App Metadata Administration Services

## Exporting a Template

*Unsecured Endpoint:* GET /export-template/{template-id}

This service exports a template in a format similar to the format required by
Tito. This service is not used by the DE and has been superceded by the secured
`/edit-template` and `/copy-template` endpoints. The response body for this
service is fairly large, so it will not be documented in this file. For all of
the gory details, see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Exporting an Analysis

*Unsecured Endpoint:* GET /export-workflow/{analysis-id}

This service exports an analysis in the format used to import multi-step
analyses into the DE. Note that this format will work for both single- and
multi-step analyses. This service is used by the export script to export
analyses from the DE. The response body for this service is fairly large, so it
will not be documented in this file. For all of the gory details, see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Exporting Selected Deployed Components

*Unsecured Endpoint:* POST /export-deployed-components

This service exports deployed components matching search criteria that are
provided in the request body. Searches can be performed by identifier, name,
location, or name and location combined. If no search criteria are provided then
all existing deployed components will be provided.

The request body should be a string representing a JSON object. The keys are:
`id` for the identifier, `name` for the name and `location` for the location.
The `name` and `location` fields may be specified either together or by
themselves. If the `id` field is specified then it must be the only field that
is specified. An empty JSON object indicates that no search criteria are
specified, meaning that all deployed components will be exported.

The response for this service is in the following format:

```json
{
    "components": [
        {
            "location": "location",
            "version": "version",
            "attribution": "attribution",
            "name": "name",
            "description": "description",
            "implementation": {
                "test": {
                    "input_files": [
                        "input-file-1",
                        "input-file-2",
                        ...,
                        "input-file-n"
                    ],
                    "output_files": [
                        "output-file-1",
                        "output-file-2",
                        ...,
                        "output-file-n"
                    ]
                },
                "implementor": {
                    "implementor": "implementor-name",
                    "implementor_email": "implementor-email"
                }
            }
            "id": "id",
            "type": "type"
        },
        ...
    ]
}
```

Here are some examples:

```
$ curl -sd '
{
    "name":"printargs"
}
' http://by-tor:8888/export-deployed-components | python -mjson.tool
{
    "components": [
        {
            "attribution": "Insane Membranes, Inc.",
            "description": "Print command-line arguments.",
            "id": "c49bccf303e7f46e0bbf4c05fd4b2d9a7",
            "implementation": {
                "implementor": "Nobody",
                "implementor_email": "nobody@iplantcollaborative.org",
                "test": {
                    "input_files": [],
                    "output_files": []
                }
            },
            "location": "/usr/local2/bin",
            "name": "printargs",
            "type": "executable",
            "version": "0.0.1"
        }
    ]
}
```

```
$ curl -sd '
{
    "name":"printargs",
    "location": "/usr/local2/bin"
}
' http://by-tor:8888/export-deployed-components | python -mjson.tool
{
    "components": [
        {
            "attribution": "Insane Membranes, Inc.",
            "description": "Print command-line arguments.",
            "id": "c49bccf303e7f46e0bbf4c05fd4b2d9a7",
            "implementation": {
                "implementor": "Nobody",
                "implementor_email": "nobody@iplantcollaborative.org",
                "test": {
                    "input_files": [],
                    "output_files": []
                }
            },
            "location": "/usr/local2/bin",
            "name": "printargs",
            "type": "executable",
            "version": "0.0.1"
        }
    ]
}
```

## Permanently Deleting an Analysis

*Unsecured Endpoint:* POST /permanently-delete-workflow

This service physically removes an analysis from the database, which allows
administrators to completely remove analyses that are causing problems. As far
as I know, this service hasn't been used in quite a while, and it can probably
be removed at some point in the near future. The request body is in the
following format for the deletion of a private analysis:

```json
{
    "analysis_id": "analysis-id",
    "full_username": "username"
}
```

This service also supports deleting analyses by name, but this practice isn't
recommended because analysis names are not guaranteed to be unique. When
deletion by name is requested, the request body is in this format for the
deletion of a private analysis:

```json
{
    "analysis_name": "analysis-name",
    "full_username": "username"
}
```

Public analyses may be deleted by this service as well, but the service has to
be explicitly told that a public analysis is being deleted. The request body for
the deletion of a public analysis by ID is in this format:

```json
{
    "analysis_id": "analysis-id",
    "root_deletion_request": true
}
```

Similarly, the request body for the deletion of a public analysis by name is in
this format:

```json
{
    "analysis_name": "analysis-name",
    "root_deletion_request": true
}
```

This service has no response body.

For more information about this service, please see the (Tool Integration
Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Logically Deleting an Analysis

*Unsecured Endpoint:* POST /delete-workflow

This service works in exactly the same way as the `/permanently-delete-workflow`
service except that, instead of permanently deleting the analysis, it merely
marks the analysis as deleted. This prevents the analysis from being displayed
in the DE, but retains its definition so that it can be restored later if
necessary. For more information about this service, please see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Previewing Templates

*Unsecured Endpoint:* POST /preview-template

Tito uses this service (indirectly) to allow users to preview the UI for a
template that is being edited. The request body for this service is in the
format required by the `/import-template` service. The response body for this
service is the in the format produced by the `/get-analysis` service. For more
information about this service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Previewing Analyses

*Unsecured Endpoint:* POST /preview-workflow

The purpose of this service is to preview the JSON that would be fed to the UI
for an analysis. The request body for this service is in the format required by
the `/import-workflow` service. The response body for this service is in the
format produced by the `/get-analysis` service. For more information about this
service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Updating an Existing Template

*Unsecured Endpoint:* POST /update-template

This service either imports a new template or updates an existing template in
the database. For more information about this service, please see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Updating an Analysis

*Unsecured Endpoint:* POST /update-workflow

This service either imports a new analysis or updates an existing analysis in
the database (as long as the analysis has not been submitted for public use).
The difference between this service and the `/update-template` service is that
this service can support multi-step analyses. For more information about this
service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Forcing an Analysis to be Updated

*Unsecured Endpoint:* POST /force-update-workflow

The `/update-workflow` service only allows private analyses to be updated.
Analyses that have been submitted for public use must be updated using this
service. The analysis import script uses this service to import analyses that
have previously been exported. For more information about this service, please
see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing a Template

*Unsecured Endpoint:* POST /import-template

This service imports a new template into the DE; it will not overwrite an
existing template. To overwrite an existing template, please use the
`/update-template` service. For more information about this service, please see
the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing an Analysis

*Unsecured Endpoint:* POST /import-workflow

This service imports a new analysis into the DE; it will not overwrite an
existing analysis. To overwrite an existing analysis, please use the
`/update-workflow` service. For more information about this service, please see
the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing Tools

*Unsecured Endpoint:* POST /import-tools

This service imports deployed components into the DE. In metadactyl, this
service is identical to the `/import-workflow` service.

## Updating Top-Level Analysis Information

*Unsecured Endpoint:* POST /update-analysis

This service updates analysis information without updating any components within
the analysis. The effect in the database is that the `transformation_activity`
table will be updated, but none of its associated tables will be updated. The
request body is in the following format:

```json
{
    "id": "analysis-id",
    "name": "analysis-name",
    "description": "analysis-description",
    "edited_date": "analysis-edited-date",
    "published_date": "analysis-published-date"
}
```

Only the "id" field is required; the rest of the fields will be left unmodified
if they're not specified.

## Updating the Favorite Analyses List

*Secured Endpoint:* POST /secured/update-favorites

Analyses can be marked as favorites in the DE, which allows users to access them
without having to search. This service is used to add or remove analyses from a
user's favorites list. The request body is in the following format:

```json
{
    "workspace_id": "workspace-id",
    "analysis_id": "analysis-id",
    "user_favorite": "favorite-flag"
}
```

The action performed by this service is controlled by the `user_favorite` field
value. If the field value is `false` then the analysis will be added to the
user's favorites list. If the field value is `true` then the analysis will be
removed from the user's favorites list. If this service fails then the response
will be in the usual format for failed service calls. If the service succeeds
then the response conntains only a success flag:

```json
{
    "success": true
}
```

Here are some examples:


```
$ curl -sd '
{
    "workspace_id": 4,
    "analysis_id": "F99526B9-CC88-46DA-84B3-0743192DCB7B",
    "user_favorite": true
}
' "http://by-tor:8888/secured/update-favorites?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "success": true
}
```

```
$ curl -sd '
{
    "workspace_id": 4,
    "analysis_id": "F99526B9-CC88-46DA-84B3-0743192DCB7B",
    "user_favorite": true
}
' "http://by-tor:8888/secured/update-favorites?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "reason": "analysis, F99526B9-CC88-46DA-84B3-0743192DCB7B, is already a favorite",
    "success": false
}
```

```
$ curl -sd '
{
    "workspace_id": 4,
    "analysis_id": "F99526B9-CC88-46DA-84B3-0743192DCB7B",
    "user_favorite": false
}
' "http://by-tor:8888/secured/update-favorites?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "success": true
}
```

```
$ curl -sd '
{
    "workspace_id": 4,
    "analysis_id": "FOO",
    "user_favorite": false
}
' "http://by-tor:8888/secured/update-favorites?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "reason": "analysis, FOO not found",
    "success": false
}
```
