# Table of Contents

* [Overview](#overview)
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

### Property Group

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

### Property

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
            <td>Default Valure</td>
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
