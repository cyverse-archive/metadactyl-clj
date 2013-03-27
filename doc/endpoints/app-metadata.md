# Table of Contents

* [App Metadata Endpoints](#app-metadata-endpoints)
    * [Listing Workflow Elements](#listing-workflow-elements)
    * [Search Deployed Components](#search-deployed-components)
    * [Listing Analysis Identifiers](#listing-analysis-identifiers)
    * [Deleting Categories](#deleting-categories)
    * [Valiating Analyses for Pipelines](#validating-analyses-for-pipelines)
    * [Listing Data Objects in an Analysis](#listing-data-objects-in-an-analysis)
    * [Categorizing Analyses](#categorizing-analyses)
    * [Listing Analysis Categorizations](#listing-analysis-categorizations)
    * [Determining if an Analysis Can be Exported](#determining-if-an-analysis-can-be-exported)
    * [Adding Analyses to Analysis Groups](#adding-analyses-to-analysis-groups)
    * [Getting Analyses in the JSON Format Required by the DE](#getting-analyses-in-the-json-format-required-by-the-de)
    * [Getting Analysis Details](#getting-analysis-details)
    * [Listing Analysis Groups](#listing-analysis-groups)
    * [Listing Individual Analyses](#listing-individual-analyses)
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
    * [Getting Analysis JSON in DE Format](#geting-analysis-json-in-de-format)
    * [Rating Analyses](#rating-analyses)
    * [Deleting Analysis Ratings](#deleting-analysis-ratings)
    * [Searching for Analyses](#searching-for-analyses)
    * [Listing Analyses in an Analysis Group](#listing-analyses-in-an-analysis-group)
    * [Listing Deployed Components in an Analysis](#listing-deployed-components-in-an-analysis)
    * [Updating the Favorite Analyses List](#updating-the-favorite-analysis-list)
    * [Making an Analysis Available for Editing in Tito](#making-an-analysis-available-for-editing-in-tito)
    * [Making a Copy of an Analysis Available for Editing in Tito](#making-a-copy-of-an-analysis-available-for-editing-in-tito)
    * [Submitting an Analysis for Public Use](#submitting-an-analysis-for-public-use)
    * [Getting an App Description](#getting-an-app-description)
    * [Requesting Tool Installation](#requesting-tool-installation)
    * [Listing Tool Requests](#listing-tool-requests)
    * [Updating the Status of a Tool Request](#updating-the-status-of-a-tool-request)
    * [Obtaining Tool Request Details](#obtaining-tool-request-details)

# App Metadata Endpoints

## Listing Workflow Elements

Unsecured Endpoint: GET /get-workflow-elements/{element-type}

The `/get-workflow-elements/{element-type}` endpoint is used by Tito to obtain
lists of elements that may be included in an app. The following element types
are currently supported:

<table "border=1">
    <tr><th>Element Type</th><th>Description</th></tr>
    <tr><td>components</td><td>Registered deployed components</td></tr>
    <tr><td>formats</td><td>Known file formats</td></tr>
    <tr><td>info-types</td><td>Known types of data</td></tr>
    <tr><td>property-types</td><td>Known types of parameters</td></tr>
    <tr><td>rule-types</td><td>Known types of validation rules</td></tr>
    <tr><td>value-types</td><td>Known types of parameter values</td></tr>
    <tr><td>data-sources</td><td>Known sources for data objects</td></tr>
    <tr><td>tool-types</td><td>Known types of deployed components</td></tr>
    <tr><td>all</td><td>All workflow element types</td></tr>
</table>

The response format varies depending on the type of information that is being
returned.

Deployed components represent tools (usually, command-line tools) that can be
executed from within the discovery environment. Here's an example deployed
components listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/components | python -mjson.tool
{
    "components": [
        {
            "attribution": "Insane Membranes, Inc.",
            "description": "You'll find out!",
            "hid": 320,
            "id": "c718a4715484949a1bf0892e28324f64f",
            "location": "/usr/blah/bin",
            "name": "foo.pl",
            "type": "executable",
            "version": "0.0.1"
        },
        ...
    ],
    "success": true
}
```

The known file formats can be used to describe supported input or output formats
for a deployed component. For example, tools in the FASTX toolkit may support
FASTA files, several different varieties of FASTQ files and Barcode files, among
others. Here's an example listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/formats | python -mjson.tool
{
    "formats": [
        {
            "hid": 1,
            "id": "E806880B-383D-4AD6-A4AB-8CDD88810A33",
            "label": "Unspecified Data Format",
            "name": "Unspecified"
        },
        {
            "hid": 3,
            "id": "6C4D09B3-0108-4DD3-857A-8225E0645A0A",
            "label": "FASTX toolkit barcode file",
            "name": "Barcode-0"
        },
        ...
    ],
    "success": true
}
```

The known information types can be used to describe the type of information
consumed or produced by a deployed component. This is distinct from the data
format because some data formats may contain multiple types of information and
some types of information can be described using multiple data formats. For
example, the Nexus format can contain multiple types of information, including
phylogenetic trees. And phylogenetic trees can also be represented in PhyloXML
format, and a large number of other formats. The file format and information
type together identify the type of input consumed by a deployed component or the
type of output produced by a deployed component. here's an example information
type listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/info-types | python -mjson.tool
{
    "info_types": [
        {
            "hid": 3,
            "id": "0900E992-3BBD-4F4B-8D2D-ED289CA4E4F1",
            "label": "Unspecified",
            "name": "File"
        },
        {
            "hid": 6,
            "id": "0E3343E3-C59A-44C4-B5EE-D4501EC3A898",
            "label": "Reference Sequence and Annotations",
            "name": "ReferenceGenome"
        },
        ...
    ],
    "success": true
}
```

Property types represent the types of information that can be passed to a
deployed component. For command-line tools, a property generally represents a
command-line option and the property type represents the type of data required
by the command-line option. For example a `Boolean` property generally
corresponds to a single command-line flag that takes no arguments. A `Text`
property, on the other hand, generally represents some sort of textual
information. Some property types are not supported by all tool types, so it is
helpful in some cases to filter property types either by the tool type or
optionally by the deployed component (which is used to determine the tool type).

Here's an example that is not filtered by tool type:

```
$ curl -s http://by-tor:8888/get-workflow-elements/property-types | python -mjson.tool
{
    "property_types": [
        {
            "description": "A text box (no caption or number check)",
            "hid": 12,
            "id": "ptffeca61a-f1b9-43ba-b6ff-fa77bb34f396",
            "name": "Text",
            "value_type": "String"
        },
        {
            "description": "A text box that checks for valid number input",
            "hid": 1,
            "id": "ptd2340f11-d260-41b4-93fd-c1d695bf6fef",
            "name": "Number",
            "value_type": "Number"
        },
        ...
    ],
    "success": true
}
```

Here's an example that is filtered by tool type explicitly:

```
$ curl -s http://by-tor:8888/get-workflow-elements/property-types?tool-type=fAPI | python -mjson.tool
{
    "property_types": [
        {
            "description": "A text box that checks for valid number input",
            "hid": 1,
            "id": "ptd2340f11-d260-41b4-93fd-c1d695bf6fef",
            "name": "Number",
            "value_type": "Number"
        },
        {
            "description": "",
            "hid": 2,
            "id": "pt2cf37b0d-5463-4aef-98a2-4db63d2f3dbc",
            "name": "ClipperSelector",
            "value_type": null
        },
        ...
    ],
    "success": true
}
```

Here's an example that is filtered by component identifier:

```
$ curl -s http://by-tor:8888/get-workflow-elements/property-types?component-id=c1b9f95a766b64454a2570f5ddb255931 | python -mjson.tool
{
    "property_types": [
        {
            "description": "A text box that checks for valid number input",
            "hid": 1,
            "id": "ptd2340f11-d260-41b4-93fd-c1d695bf6fef",
            "name": "Number",
            "value_type": "Number"
        },
        {
            "description": "",
            "hid": 2,
            "id": "pt2cf37b0d-5463-4aef-98a2-4db63d2f3dbc",
            "name": "ClipperSelector",
            "value_type": null
        },
        ...
    ],
    "success": true
}
```

If you filter by both tool type and deployed component ID then the tool type
will take precedence. Including either an undefined tool type or an undefined
tool type name will result in an error:

```
$ curl -s http://by-tor:8888/get-workflow-elements/property-types?component-id=foo | python -mjson.tool
{
    "code": "UNKNOWN_DEPLOYED_COMPONENT",
    "id": "foo",
    "success": false
}
```

```
$ curl -s http://by-tor:8888/get-workflow-elements/property-types?tool-type=foo | python -mjson.tool
{
    "code": "UNKNOWN_TOOL_TYPE",
    "name": "foo",
    "success": false
}
```

Rule types represent types of validation rules that may be defined to validate
user input. For example, if a property value must be an integer between 1 and 10
then the `IntRange` rule type may be used. Similarly, if a property value must
contain data in a specific format, such as a phone number, then the `Regex` rule
type may be used. Here's an example listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/rule-types | python -mjson.tool
{
    "rule_types": [
        {
            "description": "Has a range of integers allowed",
            "hid": 3,
            "id": "rte04fb2c6-d5fd-47e4-ae89-a67390ccb67e",
            "name": "IntRange",
            "rule_description_format": "Value must be between: {Number} and {Number}.",
            "subtype": "Integer",
            "value_types": [
                "Number"
            ]
        },
        {
            "description": "Has a range of values allowed (non-integer)",
            "hid": 6,
            "id": "rt58cd8b75-5598-4490-a9c9-a6d7a8cd09dd",
            "name": "DoubleRange",
            "rule_description_format": "Value must be between: {Number} and {Number}.",
            "subtype": "Double",
            "value_types": [
                "Number"
            ]
        },
    ],
    "success": true
}
```

If you look closely at the example property type and rule type listings then
you'll notice that each property type has a single value type assocaited with it
and each rule type has one or more value types associated with it. The purpose
of value types is specifically to link property types and rule types. Tito uses
the value type to determine which types of rules can be applied to a property
that is being defined by the user. Here's an example value type listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/value-types | python -mjson.tool
{
    "value_types": [
        {
            "description": "Arbitrary text",
            "hid": 1,
            "id": "0115898A-F81A-4598-B1A8-06E538F1D774",
            "name": "String"
        },
        {
            "description": "True or false value",
            "hid": 2,
            "id": "E8E05E6C-5002-48C0-9167-C9733F0A9716",
            "name": "Boolean"
        },
        ...
    ],
    "success": true
}
```

Data sources are the known possible sources for data objects. In most cases,
data objects will come from a plain file. The only other options that are
currently available are redirected standard output and redirected standard
error output. Both of these options apply only to data objects that are
associated with an output. Here's an example:

```
$ curl -s http://by-tor:8888/get-workflow-elements/data-sources | python -mjson.tool
{
    "data_sources": [
        {
            "hid": 1,
            "id": "8D6B8247-F1E7-49DB-9FFE-13EAD7C1AED6",
            "label": "File",
            "name": "file"
        },
        ...
    ],
    "success": true
}
```

Tool types are known types of deployed components in the Discovery
Environment. Generally, there's a different tool type for each execution
environment that is supported by the Discovery Environment. Here's an
example:

```
$ curl -s http://by-tor:8888/get-workflow-elements/tool-types | python -mjson.tool
{
    "success": true,
    "tool_types": [
        {
            "description": "Run at the University of Arizona",
            "id": 1,
            "label": "UA",
            "name": "executable"
        },
        ...
    ]
}
```

As a final option, it is possible to get all types of workflow elements at
once using an element type of `all`. Here's an example listing:

```
$ curl -s http://by-tor:8888/get-workflow-elements/all | python -mjson.tool
{
    "components": [
        {
            "attribution": "Insane Membranes, Inc.",
            "description": "You'll find out!",
            "hid": 320,
            "id": "c718a4715484949a1bf0892e28324f64f",
            "location": "/usr/local2/bin",
            "name": "foo.pl",
            "type": "executable",
            "version": "0.0.1"
        },
        ...
    ],
    "data_sources": [
        {
            "hid": 1,
            "id": "8D6B8247-F1E7-49DB-9FFE-13EAD7C1AED6",
            "label": "File",
            "name": "file"
        },
        ...
    ],
    "formats": [
        {
            "hid": 1,
            "id": "E806880B-383D-4AD6-A4AB-8CDD88810A33",
            "label": "Unspecified Data Format",
            "name": "Unspecified"
        },
        ...
    ],
    "info_types": [
        {
            "hid": 3,
            "id": "0900E992-3BBD-4F4B-8D2D-ED289CA4E4F1",
            "label": "Unspecified",
            "name": "File"
        },
        ...
    ],
    "property_types": [
        {
            "description": "A text box (no caption or number check)",
            "hid": 12,
            "id": "ptffeca61a-f1b9-43ba-b6ff-fa77bb34f396",
            "name": "Text",
            "value_type": "String"
        },
        ...
    ],
    "rule_types": [
        {
            "description": "Has a range of integers allowed",
            "hid": 3,
            "id": "rte04fb2c6-d5fd-47e4-ae89-a67390ccb67e",
            "name": "IntRange",
            "rule_description_format": "Value must be between: {Number} and {Number}.",
            "subtype": "Integer",
            "value_types": [
                "Number"
            ]
        },
        ...
    ],
    "success": true,
    "tool_types": [
        {
            "description": "Run at the University of Arizona",
            "id": 1,
            "label": "UA",
            "name": "executable"
        },
        ...
    ],
    "value_types": [
        {
            "description": "Arbitrary text",
            "hid": 1,
            "id": "0115898A-F81A-4598-B1A8-06E538F1D774",
            "name": "String"
        },
        ...
    ]
}
```

## Search Deployed Components

Unsecured Endpoint: GET /search-deployed-components/{search-term}

The `/search-deployed-components/{search-term}` endpoint is used by Tito to
search for a deployed component with a name or description that contains the
given search-term.

The response format is the same as the /get-workflow-elements/components
endpoint:

```
$ curl -s http://by-tor:8888/search-deployed-components/example | python -mjson.tool
{
    "components": [
        {
            "name": "foo-example.pl",
            "description": "You'll find out!",
            ...
        },
        {
            "name": "foo-bar.pl",
            "description": "Another Example Script",
            ...
        },
        ...
    ]
}
```

## Listing Analysis Identifiers

Unsecured Endpoint: GET /get-all-analysis-ids

The export script needs to have a way to obtain the identifiers of all of the
analyses in the Discovery Environment, deleted or not. This service provides
that information. Here's an example listing:

```
$ curl -s http://by-tor:8888/get-all-analysis-ids | python -mjson.tool
{
    "analysis_ids": [
        "19F78CC1-7E14-481B-9D80-85EBCCBFFCAF",
        "C5FF73E8-157F-47F0-978C-D4FAA12C2D58",
        ...
    ]
}
```

## Deleting Categories

Unsecured Endpoint: POST /delete-categories

Analysis categories can be deleted using the `/delete-categories` entpoint.
This service accepts a list of analysis category identifiers and deletes all
corresponding analysis categories.  The request body is in the following
format:

```json
{
    "category_ids": [
        "category-id-1",
        "category-id-2",
        ...
        "category-id-n"
    ]
}
```

The response contains a list of category ids for which the deletion failed in
the following format:

```json
{
    "failures": [
        "category-id-1",
        "category-id-2",
        ...
        "category-id-n"
    ]
}
```

Here's an example:

```
$ curl -sd '
{
    "category_ids": [
        "D901F356-D33E-4AE9-8F92-0A07CE9AD70E"
    ]
}
' http://by-tor:8888/delete-categories | python -mjson.tool
{
    "failures": []
}
```

## Valiating Analyses for Pipelines

Unsecured Endpoint: GET /validate-analysis-for-pipelines/{analysis-id}

Multistep analyses and empty analyses can't currently be included in pipelines,
so the UI needs a way to determine whether or not an analysis can be included in
a pipeline. This service provides that information. The response body contains a
flag indicating whether or not the analysis can be included in a pipeline along
with the reason. If the analysis can be included in a pipeline then the reason
string will be empty. The response format is:

```json
{
    "is_valid": "flag",
    "reason", "reason"
}
```

Here are some examples:

```
$ curl -s http://by-tor:8888/validate-analysis-for-pipelines/9A39F7FA-4025-40E2-A720-489FA93C6A93 | python -mjson.tool
{
    "is_valid": true,
    "reason": ""
}
```

```
$ curl -s http://by-tor:8888/validate-analysis-for-pipelines/BDB011B6-1F6B-443E-B94E-400930619978 | python -mjson.tool
{
    "is_valid": false,
    "reason": "analysis, BDB011B6-1F6B-443E-B94E-400930619978, has too many steps for a pipeline"
}
```

## Listing Data Objects in an Analysis

Unsecured Endpoint: GET /analysis-data-objects/{analysis-id}

When a pipeline is being created, the UI needs to know what types of files are
consumed by and what types of files are produced by each analysis in the
pipeline. This service provides that information. The response body contains the
analysis identifier, the analysis name, a list of inputs (types of files
consumed by the service) and a list of outputs (types of files produced by the
service). The response format is:

```json
{
    "id": "analysis-id",
    "inputs": [
        {
            "data_object": {
                "cmdSwitch": "command-line-switch",
                "description": "description",
                "file_info_type": "info-type-name",
                "format": "data-format-name",
                "id": "data-object-id",
                "multiplicity": "multiplicity-name",
                "name": "data-object-name",
                "required": "required-data-object-flag",
                "retain": "retain-file-flag",
            },
            "description": "property-description",
            "id": "property-id",
            "isVisible": "visibility-flag",
            "label": "property-label",
            "name": "property-name",
            "type": "Input",
            "value": "default-property-value"
        },
        ...
    ]
    "name": analysis-name,
    "outputs": [
        {
            "data_object": {
                "cmdSwitch": "command-line-switch",
                "description": "description",
                "file_info_type": "info-type-name",
                "format": "data-format-name",
                "id": "data-object-id",
                "multiplicity": "multiplicity-name",
                "name": "data-object-name",
                "required": "required-data-object-flag",
                "retain": "retain-file-flag",
            },
            "description": "property-description",
            "id": "property-id",
            "isVisible": "visibility-flag",
            "label": "property-label",
            "name": "property-name",
            "type": "Output",
            "value": "default-property-value"
        },
        ...
    ]
}
```

Here's an example:

```
$ curl -s http://by-tor:8888/analysis-data-objects/19F78CC1-7E14-481B-9D80-85EBCCBFFCAF | python -mjson.tool
{
    "id": "19F78CC1-7E14-481B-9D80-85EBCCBFFCAF",
    "inputs": [
        {
            "data_object": {
                "cmdSwitch": "",
                "description": "",
                "file_info_type": "File",
                "format": "Unspecified",
                "id": "A6210636-E3EC-4CD3-97B4-CAD15CAC0913",
                "multiplicity": "One",
                "name": "Input File",
                "order": 1,
                "required": true,
                "retain": false
            },
            "description": "",
            "id": "A6210636-E3EC-4CD3-97B4-CAD15CAC0913",
            "isVisible": true,
            "label": "Input File",
            "name": "",
            "type": "Input",
            "value": ""
        }
    ],
    "name": "Jills Extract First Line",
    "outputs": [
        {
            "data_object": {
                "cmdSwitch": "",
                "description": "",
                "file_info_type": "File",
                "format": "Unspecified",
                "id": "FE5ACC01-0B31-4611-B81E-26E532B459E3",
                "multiplicity": "One",
                "name": "head_output.txt",
                "order": 3,
                "required": true,
                "retain": true
            },
            "description": "",
            "id": "FE5ACC01-0B31-4611-B81E-26E532B459E3",
            "isVisible": false,
            "label": "head_output.txt",
            "name": "",
            "type": "Output",
            "value": ""
        }
    ]
}
```

## Categorizing Analyses

Unsecured Endpoint: POST /categorize_analyses

When services are exported and re-imported, the analysis categorization
information also needs to be exported and re-imported. This service allows the
categorization information to be imported. Strictly speaking, this service can
also be used to move analyses to new categories, but this service hasn't been
used for that purpose since Belphegor and Conrad were created. This service is
documented in detail in the Analysis Categorization Services section of the
[tool integration services wiki page](https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services).

The request body for this service is in this format:

```json
{
    "categories": [
        {
            "category_path": {
                "path": [
                    "root-category-name",
                    "first-subcategory-name",
                    ...,
                    "nth-subcategory-name"
                ],
                "username": "username"
            }
            "analysis": {
                "name": "analysis-name",
                "id": "analysis-id"
            }
        },
        ...
    ]
}
```

The response body format is identical to the request body format except that
only failed categorizations are listed and each categorization contains the
reason for the categorization failure. Here's the format:

```json
{
    "failed_categorizations": [
        {
            "reason": reason-for-failure,
            "category_path": {
                "path": [
                    root-category-name,
                    first-subcategory-name,
                    ...,
                    nth-subcategory-name
                ],
                "username": username
            }
            "analysis": {
                "name": analysis-name,
                "id": analysis-id
            }
        },
        ...
    ]
}
```

Here's an example:

```
$ curl -sd '
{
    "categories": [
        {
            "analysis": {
                "id": "Foo",
                "name": "Foo"
            },
            "category_path": {
                "username": "nobody@iplantcollaborative.org",
                "path": [
                    "Public Apps",
                    "Foo"
                ]
            }
        }
    ]
}
' http://by-tor:8888/categorize-analyses | python -mjson.tool
{
    "failed_categorizations": [
        {
            "categorization": {
                "analysis": {
                    "id": "Foo",
                    "name": "Foo"
                },
                "category_path": {
                    "path": [
                        "Public Apps",
                        "Foo"
                    ],
                    "username": "nobody@iplantcollaborative.org"
                }
            },
            "reason": "analysis Foo not found"
        }
    ]
}
```

## Listing Analysis Categorizations

Unsecured Endpoint: GET /get-analysis-categories/{category-set}

This is the counterpart to the /categorize-analyses endpoint; it loads
categorizations from the database and produces output in the format required by
the /categorize-analyes endpoint. The response body is in this format:

```json
{
    "categories": [
        {
            "category_path": {
                "path": [
                    "root-category-name",
                    "first-subcategory-name",
                    ...,
                    "nth-subcategory-name"
                ],
                "username": "username"
            }
            "analysis": {
                "name": "analysis-name",
                "id": "analysis-id"
            }
        },
        ...
    ]
}
```

This service can export the categorizations for two different sets of analyses
as described in the following table:

<table>
    <tr><th>Category Set</th><th>Description</th></tr>
    <tr><td>all</td><td>All analysis categorizations</td></tr>
    <tr><td>public</td><td>Only public analysis categorizations</td></tr>
</table>

Note that when only public analysis categorizations are exported, private
categorizations for public analyses are not included in the service output. This
means that if an analysis happens to be both in the user's private workspace and
in a public workspace then only the categorization in the public workspace will
be included in the output from this service. Here's an example:

```
$ curl -s http://by-tor:8888/get-analysis-categories/public | python -mjson.tool
{
    "categories": [
        {
            "analysis": {
                "id": "839E7AFA-031E-4DB8-82A6-AEBD56E9E0B9",
                "name": "hariolf-test-12"
            },
            "category_path": {
                "path": [
                    "Public Apps",
                    "Beta"
                ],
                "username": "<public>"
            }
        },
        ...
    ]
}
```

## Determining if an Analysis Can be Exported

Unsecured Endpoint: POST /can-export-analysis

Some analyses can't be exported to Tito because they contain no steps, contain
multiple steps or contain types of properties that have been deprecated and are
no longer supported in Tito. The UI uses this service to determine whether or
not an analysis can be exported to Tito before attempting to do so. The request
body for this service is in this format:

```json
{
    "analysis_id": "analysis-id"
}
```

If the analysis can be exported then the response body will be in this format:

```json
{
    "can-export": true
}
```

If the analysis can't be exported then the response body will be in this
format:

```json
{
    "can-export": false,
    "cause": "reason"
}
```

Here are some examples:

```
$ curl -sd '{"analysis_id": "BDB011B6-1F6B-443E-B94E-400930619978"}' http://by-tor:8888/can-export-analysis | python -mjson.tool
{
    "can-export": false,
    "cause": "Multi-step applications cannot be copied or modified at this time."
}
```

```
$ curl -sd '{"analysis_id": "19F78CC1-7E14-481B-9D80-85EBCCBFFCAF"}' http://by-tor:8888/can-export-analysis | python -mjson.tool
{
    "can-export": true
}
```

## Adding Analyses to Analysis Groups

Unsecured Endpoint: POST /add-analyses-to-group

Users in the Discovery Environment can add analyses to an analysis groups in
some cases. The most common use case for this feature is when the user wants to
add an existing analysis to his or her favorites. The request body for this
service is in this format:

```json
{
    "analysis_id": "analysis-id",
    "groups": [
        "group-id-1",
        "group-id-2",
        ...,
        "group-id-n"
    ]
}
```

If the service succeeds then the response body is an empty JSON object. Here's
an example:

```
$ curl -sd '
{
    "analysis_id": "9BCCE2D3-8372-4BA5-A0CE-96E513B2693C",
    "groups": [
        "028fce65-2504-4497-a20c-45e3cf8583b8"
    ]
}
' http://by-tor:8888/add-analysis-to-group | python -mjson.tool
{}
```

## Getting Analyses in the JSON Format Required by the DE

Unsecured Endpoint: GET /get-analysis/{analysis-id}

The purpose of this endpoint is to provide a way to determine what the JSON for
an analysis will look like when it is obtained by the DE. The DE itself uses a
secured endpoint that performs the same task, but there was no reason to require
a user to be authenticated in order to obtain this information. We left this
endpoint in place despite the fact that it's not used by the DE because it's
convenient for debugging.

The response body for this service is in the following format:

```json
{
    "groups": [
        {
            "id": "property-group-id",
            "label": "property-group-label",
            "name": "property-group-name",
            "properties": [
                {
                    "description": "property-description",
                    "id": "unique-property-id",
                    "isVisible": "visibility-flag",
                    "label": "property-label",
                    "name": "property-name",
                    "type": "property-type-name",
                    "validator": {
                        "id": "validator-id",
                        "label": "validator-label",
                        "name": "validator-name",
                        "required": "required-flag",
                        "rules": [
                            {
                                "rule-type": [
                                    "rule-arg-1",
                                    "rule-arg-2",
                                    ...,
                                    "rule-arg-n"
                                ],
                            },
                            ...
                        ]
                    },
                    "value": "default-property-value"
                },
                ...
            ],
            "type": "property-group-type"
        },
        ...
    ]
    "id": "analysis-id",
    "label": "analysis-label",
    "name": "analysis-name",
    "type": "analysis-type"
}
```

Here's an example:

```
$ curl -s http://by-tor:8888/get-analysis/9BCCE2D3-8372-4BA5-A0CE-96E513B2693C | python -mjson.tool
{
    "groups": [
        {
            "id": "idPanelData1",
            "label": "Select FASTQ file",
            "name": "FASTX Trimmer - Select data:",
            "properties": [
                {
                    "description": "",
                    "id": "step_1_ta2eed78a0e924e6ba4fec03d929d905b_DE79E631-A10A-9C36-8764-506E3B2D59BD",
                    "isVisible": true,
                    "label": "Select FASTQ file:",
                    "name": "-i ",
                    "type": "FileInput",
                    "validator": {
                        "label": "",
                        "name": "",
                        "required": true
                    }
                }
            ],
            "type": "step"
        },
        ...
    ],
    "id": "9BCCE2D3-8372-4BA5-A0CE-96E513B2693C",
    "label": "FASTX Workflow",
    "name": "FASTX Workflow",
    "type": ""
}
```

## Getting Analysis Details

Unsecured Endpoint: GET /analysis-details/{analysis-id}

This service is used by the DE to obtain high-level details about a single
analysis. The response body is in the following format:

```json
{
    "component": "component-name",
    "component_id": "component-id",
    "description": "analysis-description",
    "edited_date": "edited-date-milliseconds",
    "id": "analysis-id",
    "label": "analysis-label",
    "name": "analysis-name",
    "published_date": "published-date-milliseconds",
    "references": [
        "reference-1",
        "reference-2",
        ...,
        "reference-n"
    ],
    "tito": "analysis-id",
    "type": "component-type"
}
```

This service will fail if the analysis isn't found or is a pipeline (that is, it
contains multiple steps). Here are some examples:

```
$ curl -s http://by-tor:8888/analysis-details/t0eba98231a404e3a927245001b21aa25 | python -mjson.tool
{
    "component": "cat",
    "component_id": "c72c314d1eace461290b9b568d9feb86a",
    "description": "Test Description for CORE-3750",
    "edited_date": "1354666971032",
    "id": "t0eba98231a404e3a927245001b21aa25",
    "label": "",
    "name": "Test CORE-3750",
    "published_date": "1354666971032",
    "references": [
        "test another ref",
        "https://pods.iplantcollaborative.org/jira/browse/CORE-3750"
    ],
    "tito": "t0eba98231a404e3a927245001b21aa25",
    "type": "executable"
}
```

```
$ curl -s http://by-tor:8888/analysis-details/foo | python -mjson.tool
{
    "reason": "app, foo, not found",
    "success": false
}
```

```
$ curl -s http://by-tor:8888/analysis-details/009CECFD-0DF7-4B3D-98EF-82105C84835F | python -mjson.tool
{
    "reason": "pipeline, 009CECFD-0DF7-4B3D-98EF-82105C84835F, can't be displayed by this service",
    "success": false
}
```

## Listing Analysis Groups

Unsecured Endpoint: GET /get-only-analysis-groups/{workspace-token}

This service is used by the DE and (indirectly) by Tito to obtain the list of
analysis groups that are visible to the user. This list includes analysis groups
that are in the user's workspace along with any analysis groups that are in a
workspace that is marked as public in the database. The `workspace-token`
argument can either be the workspace ID or the user's fully qualified username.
(The DE sends the workspace ID; Tito sends the username.) The response is in the
following format:

```json
{
    "groups": [
        {
            "description": "analysis-group-description",
            "groups": [
               ...
            ],
            "id": "analysis-group-id",
            "is_public": "public-flag",
            "name": "analysis-group-name",
            "template_count": "template-count"
        }
    ]
}
```

Note that this data structure is recursive; each analysis group may contain zero
or more other analysis groups.

Here's an example using a workspace ID:

```
$ curl -s http://by-tor:8888/get-only-analysis-groups/4 | python -mjson.tool
{
    "groups": [
        {
            "description": "",
            "groups": [
                {
                    "description": "",
                    "id": "b9a1a3b8-fef6-4576-bbfe-9ad17eb4c2ab",
                    "is_public": false,
                    "name": "Apps Under Development",
                    "template_count": 0
                },
                {
                    "description": "",
                    "id": "2948ed96-9564-489f-ad73-e099b171a9a5",
                    "is_public": false,
                    "name": "Favorite Apps",
                    "template_count": 0
                }
            ],
            "id": "57a39832-3577-4ee3-8ff4-3fc9d1cf9e34",
            "is_public": false,
            "name": "Workspace",
            "template_count": 0
        },
        ...
    ]
}
```

Here's an example using a username:

```
$ curl -s http://by-tor:8888/get-only-analysis-groups/nobody@iplantcollaborative.org | python -mjson.tool
{
    "groups": [
        {
            "description": "",
            "groups": [
                {
                    "description": "",
                    "id": "b9a1a3b8-fef6-4576-bbfe-9ad17eb4c2ab",
                    "is_public": false,
                    "name": "Apps Under Development",
                    "template_count": 0
                },
                {
                    "description": "",
                    "id": "2948ed96-9564-489f-ad73-e099b171a9a5",
                    "is_public": false,
                    "name": "Favorite Apps",
                    "template_count": 0
                }
            ],
            "id": "57a39832-3577-4ee3-8ff4-3fc9d1cf9e34",
            "is_public": false,
            "name": "Workspace",
            "template_count": 0
        },
        ...
    ]
}
```

## Listing Individual Analyses

Unsecured Endpoint: GET /list-analysis/{analysis-id}

This service lists information about a single analysis if that analysis exists.
Here are some examples:

```
$ curl -s http://by-tor:8888/list-analysis/00102BE0-A7D7-4CC8-89F0-B1DB84B79018 | python -mjson.tool
{
    "analyses": [
        {
            "deleted": false,
            "description": "",
            "disabled": false,
            "edited_date": "",
            "id": "00102BE0-A7D7-4CC8-89F0-B1DB84B79018",
            "integration_date": "",
            "integrator_email": "mherde@iplantcollaborative.org",
            "integrator_name": "mherde",
            "is_favorite": false,
            "is_public": false,
            "name": "Copy of FASTX Barcode Splitter (Single End)",
            "pipeline_eligibility": {
                "is_valid": true,
                "reason": ""
            },
            "rating": {
                "average": 0
            },
            "wiki_url": ""
        }
    ]
}
```

```
$ curl -s http://by-tor:8888/list-analysis/I-DO-NOT-EXIST | python -mjson.tool
{
    "analyses": []
}
```

## Exporting a Template

Unsecured Endpoint: GET /export-template/{template-id}

This service exports a template in a format similar to the format required by
Tito. This service is not used by the DE and has been superceded by the secured
`/edit-template` and `/copy-template` endpoints. The response body for this
service is fairly large, so it will not be documented in this file. For all of
the gory details, see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Exporting an Analysis

Unsecured Endpoint: GET /export-workflow/{analysis-id}

This service exports an analysis in the format used to import multi-step
analyses into the DE. Note that this format will work for both single- and
multi-step analyses. This service is used by the export script to export
analyses from the DE. The response body for this service is fairly large, so it
will not be documented in this file. For all of the gory details, see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Exporting Selected Deployed Components

Unsecured Endpoint: POST /export-deployed-components

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

Unsecured Endpoint: POST /permanently-delete-workflow

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

Unsecured Endpoint: POST /delete-workflow

This service works in exactly the same way as the `/permanently-delete-workflow`
service except that, instead of permanently deleting the analysis, it merely
marks the analysis as deleted. This prevents the analysis from being displayed
in the DE, but retains its definition so that it can be restored later if
necessary. For more information about this service, please see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Previewing Templates

Unsecured Endpoint: POST /preview-template

Tito uses this service (indirectly) to allow users to preview the UI for a
template that is being edited. The request body for this service is in the
format required by the `/import-template` service. The response body for this
service is the in the format produced by the `/get-analysis` service. For more
information about this service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Previewing Analyses

Unsecured Endpoint: POST /preview-workflow

The purpose of this service is to preview the JSON that would be fed to the UI
for an analysis. The request body for this service is in the format required by
the `/import-workflow` service. The response body for this service is in the
format produced by the `/get-analysis` service. For more information about this
service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Updating an Existing Template

Unsecured Endpoint: POST /update-template

This service either imports a new template or updates an existing template in
the database. For more information about this service, please see the (Tool
Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Updating an Analysis

Unsecured Endpoint: POST /update-workflow

This service either imports a new analysis or updates an existing analysis in
the database (as long as the analysis has not been submitted for public use).
The difference between this service and the `/update-template` service is that
this service can support multi-step analyses. For more information about this
service, please see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Forcing an Analysis to be Updated

Unsecured Endpoint: POST /force-update-workflow

The `/update-workflow` service only allows private analyses to be updated.
Analyses that have been submitted for public use must be updated using this
service. The analysis import script uses this service to import analyses that
have previously been exported. For more information about this service, please
see the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing a Template

Unsecured Endpoint: POST /import-template

This service imports a new template into the DE; it will not overwrite an
existing template. To overwrite an existing template, please use the
`/update-template` service. For more information about this service, please see
the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing an Analysis

Unsecured Endpoint: POST /import-workflow

This service imports a new analysis into the DE; it will not overwrite an
existing analysis. To overwrite an existing analysis, please use the
`/update-workflow` service. For more information about this service, please see
the (Tool Integration Services wiki
page)[https://pods.iplantcollaborative.org/wiki/display/coresw/Tool+Integration+Services].

## Importing Tools

Unsecured Endpoint: POST /import-tools

This service imports deployed components into the DE. In metadactyl, this
service is identical to the `/import-workflow` service.

## Updating Top-Level Analysis Information

Unsecured Endpoint: POST /update-analysis

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

## Getting Analysis JSON in DE Format

Secured Endpoint: GET /secured/template/{analysis-id}

This service is the secured version of the `/get-analyis` endpoint. The response
body for this service is in the following format:

```json
{
    "groups": [
        {
            "id": "property-group-id",
            "label": "property-group-label",
            "name": "property-group-name",
            "properties": [
                {
                    "description": "property-description",
                    "id": "unique-property-id",
                    "isVisible": "visibility-flag",
                    "label": "property-label",
                    "name": "property-name",
                    "type": "property-type-name",
                    "validator": {
                        "id": "validator-id",
                        "label": "validator-label",
                        "name": "validator-name",
                        "required": "required-flag",
                        "rules": [
                            {
                                "rule-type": [
                                    "rule-arg-1",
                                    "rule-arg-2",
                                    ...,
                                    "rule-arg-n"
                                ],
                            },
                            ...
                        ]
                    },
                    "value": "default-property-value"
                },
                ...
            ],
            "type": "property-group-type"
        },
        ...
    ]
    "id": "analysis-id",
    "label": "analysis-label",
    "name": "analysis-name",
    "type": "analysis-type"
}
```

Here's an example:

```
curl -s "http://by-tor:8888/secured/template/9BCCE2D3-8372-4BA5-A0CE-96E513B2693C?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "groups": [
        {
            "id": "idPanelData1",
            "label": "Select FASTQ file",
            "name": "FASTX Trimmer - Select data:",
            "properties": [
                {
                    "description": "",
                    "id": "step_1_ta2eed78a0e924e6ba4fec03d929d905b_DE79E631-A10A-9C36-8764-506E3B2D59BD",
                    "isVisible": true,
                    "label": "Select FASTQ file:",
                    "name": "-i ",
                    "type": "FileInput",
                    "validator": {
                        "label": "",
                        "name": "",
                        "required": true
                    }
                }
            ],
            "type": "step"
        },
        ...
    ],
    "id": "9BCCE2D3-8372-4BA5-A0CE-96E513B2693C",
    "label": "FASTX Workflow",
    "name": "FASTX Workflow",
    "type": ""
}
```

## Rating Analyses

Secured Endpoint: POST /secured/rate-analysis

Users have the ability to rate an analysis for its usefulness, and this service
provides the means to store the analysis rating. This service accepts an
analysis identifier a rating level between one and five, inclusive, and a
comment identifier that refers to a comment in iPlant's Confluence wiki. The
rating is stored in the database and associated with the authenticated user. The
request body for this service is in the following format:

```json
{
    "analysis_id": "analysis-id",
    "rating": "selected-rating",
    "comment_id": "comment-identifier"
}
```

The response body for this service contains only the average rating for the
analysis, and is in this format:

```json
{
    "avg": "average-rating",
}
```

Here's an example:

```
$ curl -sd '
{
    "analysis_id": "72AA400D-6945-463E-A18D-09513C2381D7",
    "rating": 4,
    "comment_id": 27
}
' "http://by-tor:8888/secured/rate-analysis?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "avg": 4
}
```

## Deleting Analysis Ratings

Secured Endpoint: POST /secured/delete-rating

The DE uses this service to remove a rating that a user has previously made.
This service accepts an analysis identifier in a JSON request body and deletes
the authenticated user's rating for the corresponding analysis. The request body
for this service is in the following format:

```json
{
    "analysis_id": "analysis-id",
}
```

The response body for this service contains only the new average rating for the
analysis and is in the following format:

```json
{
    "avg": "average-rating",
}
```

Here's an example:

```
$ curl -sd '
{
    "analysis_id": "a65fa62bcebc0418cbb947485a63b30cd"
}
' "http://by-tor:8888/secured/delete-rating?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "avg": 0
}
```

## Searching for Analyses

Secured Endpoint: GET /secured/search-analyses

This service allows users to search for analyses based on a part of the analysis
name or description. The response body contains a "templates" array that is in
the same format as the "templates" array in the /secured/get-analyses-in-group
endpoint response (see the next section):

```json
{
    "templates": [
        {
            "id": "analysis-id",
            "description": "analysis-description",
            "name": "analysis-name",
            "group_id": "analysis-group-id",
            "group_name": "analysis-group-name",
            ...
        },
        ...
    ]
}
```

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/search-analyses?user=snow-dog&email=sd@example.org&search=ranger" | python -mjson.tool
{
    "templates": [
        {
            "id": "9D221848-1D12-4A31-8E93-FA069EEDC151",
            "name": "Ranger",
            "description": "Some Description",
            "group_id": "99F2E2FE-9931-4154-ADDB-28386027B19F",
            "group_name": "Some Group Name",
            ...
        }
    ]
}
```

## Listing Analyses in an Analysis Group

Secured Endpoint: GET /secured/get-analyses-in-group/{group-id}

This service lists all of the analyses within an analysis group or any of its
descendents. The DE uses this service to obtain the list of analyses when a user
clicks on a group in the _Apps_ window.

This endpoint accepts optional URL query parameters to limit and sort Apps,
which will allow pagination of results.

<table "border=1">
    <tr><th>Parameter</th><th>Description</th></tr>
    <tr>
        <td>limit=X</td>
        <td>
            Limits the response to X number of results in the "templates" array.
            See
            http://www.postgresql.org/docs/8.4/interactive/queries-limit.html
        </td>
    </tr>
    <tr>
        <td>offset=X</td>
        <td>
            Skips the first X number of results in the "templates" array. See
            http://www.postgresql.org/docs/8.4/interactive/queries-limit.html
        </td>
    </tr>
    <tr>
        <td>sortField=X</td>
        <td>
            Sorts the results in the "templates" array by the field X, before
            limits and offsets are applied. This field can be any one of the
            simple fields of the "templates" objects, or `average_rating` or
            `user_rating` for ratings sorting. See
            http://www.postgresql.org/docs/8.4/interactive/queries-order.html
        </td>
    </tr>
    <tr>
        <td>sortDir=[ASC|DESC]</td>
        <td>
            Only used when sortField is present. Sorts the results in either
            ascending (`ASC`) or descending (`DESC`) order, before limits and
            offsets are applied. Defaults to `ASC`.
            See
            http://www.postgresql.org/docs/8.4/interactive/queries-order.html
        </td>
    </tr>
</table>

The response body for this service is in the following format:

```json
{
    "description": "analysis-group-description",
    "id": "analysis-group-id",
    "is_public": "public-group-flag",
    "name": "analysis-group-name",
    "template_count": "number-of-analyses-in-group-and-descendents",
    "templates": [
        {
            "can_run": "analysis-can-run-flag",
            "deleted": "analysis-deleted-flag",
            "description": "analysis-description",
            "disabled": "analysis-disabled-flag",
            "id": "analysis-id",
            "integrator_email": "integrator-email-address",
            "integrator_name": "integrator-name",
            "is_favorite": "favorite-analysis-flag",
            "is_public": "public-analysis-flag",
            "name": "analysis-name",
            "pipeline_eligibility": {
                "is_valid": "valid-for-pipelines-flag",
                "reason": "reason-for-exclusion-from-pipelines-if-applicable",
            },
            "rating": {
                "average": "average-rating",
                "comment-id": "comment-id",
                "user": "user-rating"
            },
            "wiki_url": "documentation-link"
        },
        ...
    ]
}
```

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/get-analyses-in-group/6A1B9EBD-4950-4F3F-9CAB-DD12A1046D9A?user=snow-dog&email=sd@example.org&limit=1&sortField=name&sortDir=DESC" | python -mjson.tool
{
    "can_run": true,
    "description": "",
    "id": "C3DED4E2-EC99-4A54-B0D8-196112D1BB7B",
    "is_public": true,
    "name": "Some Group",
    "template_count": 100,
    "templates": [
        {
            "deleted": false,
            "description": "Some app description.",
            "disabled": false,
            "id": "81C0CCEE-439C-4516-805F-3E260E336EE4",
            "integrator_email": "nobody@iplantcollaborative.org",
            "integrator_name": "Nobody",
            "is_favorite": false,
            "is_public": true,
            "name": "Z-AppName",
            "pipeline_eligibility": {
                "is_valid": true,
                "reason": ""
            },
            "rating": {
                "average": 4,
                "comment_id": 27,
                "user": 4
            },
            "wiki_url": "https://pods.iplantcollaborative.org/wiki/some/doc/link"
        }
    ]
}
```

The `can_run` flag is calculated by comparing the number of steps in the app to
the number of steps that have deployed component associated with them. If the
numbers are different then this flag is set to `false`. The idea is that every
step in the analysis has to have, at the very least, a deployed component
associated with it in order to run successfully.

## Listing Deployed Components in an Analysis

Secured Endpoint: GET /secured/get-components-in-analysis/{analysis-id}

This service lists information for all of the deployed components that are
associated with an analysis. This information used to be included in the results
of the analysis listing service. The response body is in the following format:

```json
{
    "deployed_components": [
        {
            "attribution": "attribution-1",
            "description": "description-1",
            "id": "id-1",
            "location": "location-1",
            "name": "name-1",
            "type": "type-1",
            "version": "version-1"
        },
        ...
    ]
}

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/get-components-in-analysis/0BA04303-F0CB-4A34-BACE-7090F869B332?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "deployed_components": [
        {
            "attribution": "",
            "description": "",
            "id": "c73ef66158ef94f1bb90689ff813629f5",
            "location": "/usr/local2/muscle3.8.31",
            "name": "muscle",
            "type": "executable",
            "version": ""
        },
        {
            "attribution": "",
            "description": "",
            "id": "c2d79e93d83044a659b907764275248ef",
            "location": "/usr/local2/phyml-20110304",
            "name": "phyml",
            "type": "executable",
            "version": ""
        }
    ]
}
```

## Updating the Favorite Analyses List

Secured Endpoint: POST /secured/update-favorites

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

## Making an Analysis Available for Editing in Tito

Secured Endpoint: GET /secured/edit-template/{analysis-id}

Tito uses this service to obtain the analysis description JSON so that the
analysis can be edited. The Analysis must have been integrated by the requesting
user, and it must not already be public. Currently, Analyses with more than 1
step can not be edited.

The response body contains the analysis description in the format that is
required by Tito. Here's an example:

```
$ curl -s "http://by-tor:8888/secured/edit-template/F29C156C-E286-4BBD-9033-0075C09E0D70?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "objects": [
        {
            "component": "cat",
            "component_id": "c72c314d1eace461290b9b568d9feb86a",
            "description": "",
            "edited_date": "",
            "groups": {
                "description": "",
                "groups": [
                    {
                        "description": "",
                        "id": "524AD6B2-7093-A9E6-1F56-919C09E286F9",
                        "isVisible": true,
                        "label": "Advanced Arguments",
                        "name": "",
                        "properties": [
                            {
                                "description": "",
                                "id": "37ADF623-36AD-31A1-3455-4F95F2108774",
                                "isVisible": true,
                                "label": "Advanced Options",
                                "name": "",
                                "omit_if_blank": false,
                                "order": -1,
                                "type": "Info",
                                "value": ""
                            },
                            {
                                "description": "This is an example of tool tip text that might be helpful ",
                                "id": "A391A212-8261-3662-A812-68E5309D3A5A",
                                "isVisible": true,
                                "label": "Number blank lines",
                                "name": "-b",
                                "omit_if_blank": false,
                                "order": -1,
                                "type": "Flag",
                                "value": "false"
                            }
                        ],
                        "type": ""
                    },
                    {
                        "description": "",
                        "id": "4CC29EF5-E950-5177-B54A-C61C33637BD4",
                        "isVisible": true,
                        "label": "This is a group mechanism",
                        "name": "",
                        "properties": [
                            {
                                "description": "",
                                "id": "23ABF631-8109-D3FA-0714-2378059BBBA1",
                                "isVisible": true,
                                "label": "Another argument",
                                "name": "-e",
                                "omit_if_blank": false,
                                "order": -1,
                                "type": "Flag",
                                "value": "false"
                            }
                        ],
                        "type": ""
                    }
                ],
                "id": "--root-PropertyGroupContainer--",
                "isVisible": true,
                "label": "",
                "name": ""
            },
            "id": "F29C156C-E286-4BBD-9033-0075C09E0D70",
            "label": "Sample Cat",
            "name": "Sample Cat",
            "published_date": "",
            "references": [],
            "tito": "F29C156C-E286-4BBD-9033-0075C09E0D70",
            "type": ""
        }
    ]
}
```

## Making a Copy of an Analysis Available for Editing in Tito

Secured Endpoint: GET /secured/copy-template/{analysis-id}

This service can be used to make a copy of an analysis in the user's workspace.
The response body consists of a JSON object containing the ID of the new
analysis. Here's an example:

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/copy-template/C720C42D-531A-164B-38CC-D2D6A337C5A5?user=snow-dog&email=sd@example.org" | python -m json.tool
{
    "analysis_id": "13FF6D0C-F6F7-4ACE-A6C7-635A17826383"
}
```

## Submitting an Analysis for Public Use

Secured Endpoint: POST /secured/make-analysis-public

This service can be used to submit a private analysis for public use. The user
supplies basic information about the analysis and a suggested location for it.
The service records the information and suggested location then places the
analysis in the Beta category. A Tito administrator can subsequently move the
analysis to the suggested location at a later time if it proves to be useful.
The request body is in the following format:

```json
{
    "analysis_id": "analysis-id",
    "email": "integrator-email-address",
    "integrator": "integrator-name",
    "references": [
        "reference-link-1",
        "reference-link-2",
        ...,
        "reference-link-n"
    ],
    "groups": [
        "suggested-group-1",
        "suggested-group-2",
        ...,
        "suggested-group-n"
    ],
    "desc": "analysis-description",
    "wiki_url": "documentation-link"
}
```

The response body is just an empty JSON object if the service call succeeds.

Making an analysis public entails recording the additional inforamtion provided
to the service, removing the analysis from all of its current analysis groups,
adding the analysis to the _Beta_ group.

Here's an example:

```
$ curl -sd '
{
    "analysis_id": "F771A215-4809-4683-87C0-A899C0732AF3",
    "email": "nobody@iplantcollaborative.org",
    "integrator": "Nobody",
    "references": [
        "http://foo.bar.baz.org"
    ],
    "groups": [
        "0A687324-099B-4EEF-A82C-C1A60B970487"
    ],
    "desc": "The foo is in the bar.",
    "wiki_url": "https://wiki.iplantcollaborative.org/docs/Foo+Foo"
}
' "http://by-tor:8888/secured/make-analysis-public?user=snow-dog&email=sd@example.org"
{}
```

## Getting an App Description

Unsecured Endpoint: GET /get-app-descrioption/{analysis-id}

This service is used by Donkey to get app descriptions for job status update
notifications. There is no request body and the response body contains only the
analysis description, with no special formatting.  Here's an example:

```
$ curl http://by-tor:8888/get-app-description/FA65A1AF-8B9D-4151-9073-2A5D1874F8C0 && echo
Lorem ipsum dolor sit amet
```

## Requesting Tool Installation

Secured Endpoint: PUT /secured/tool-request

This service submits a request for a tool to be installed so that it can be used
from within the Discovery Environment. The installation request and all status
updates related to the tool request will be tracked in the Discovery Environment
database. One possible request body format is:

```json
{
    "phone": "user-phone-number",
    "name": "tool-name",
    "description": "tool-description",
    "src_url": "link-to-tool-source",
    "documentation_url": "link-to-tool-documentation",
    "version": "tool-version",
    "attribution": "tool-attribution",
    "multithreaded": "multithreaded-flag",
    "test_data_file": "test-data-path",
    "cmd_line": "command-line-description",
    "additional_info": "optional-additional-info",
    "additional_data_file": "optional-additional-file",
    "architecture": "architecture flag"
}
```

All tool installation requests will look similar to this one, but some fields
may be replaced with others, depending on the nature of the request. A complete
description of the request body is included below with related fields organized
into groups. In cases where a multiple fields are in a required field group,
any one of the fields from that group may be specified.

<table border='1'>
    <thead>
        <tr>
            <th>Field Group</th>
            <th>Required</th>
            <th>Field Name</th>
            <th>Field Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Phone</td>
            <td>No</td>
            <td>phone</td>
            <td>The phone number of the user submitting the request.</td>
        </tr>
        <tr>
            <td>Tool Name</td>
            <td>Yes</td>
            <td>name</td>
            <td>The name of the tool being installed (should be the file name).</td>
        </tr>
        <tr>
            <td>Tool Description</td>
            <td>Yes</td>
            <td>description</td>
            <td>A brief description of the tool.</td>
        </tr>
        <tr>
            <td rowspan="2">Source Location</td>
            <td rowspan="2">Yes</td>
            <td>src_url</td>
            <td>A link that can be used to obtain the tool.</td>
        </tr>
        <tr>
            <td>src_upload_file</td>
            <td>The path to a file that has been uploaded into iRODS.</td>
        </tr>
        <tr>
            <td>Documentation Location</td>
            <td>Yes</td>
            <td>documentation_url</td>
            <td>A link to the tool documentation.</td>
        </tr>
        <tr>
            <td>Tool Version</td>
            <td>Yes</td>
            <td>version</td>
            <td>The tool's version string.</td>
        </tr>
        <tr>
            <td>Tool Attribution</td>
            <td>No</td>
            <td>attribution</td>
            <td>The people or organizations that produced the tool.</td>
        </tr>
        <tr>
            <td>Multithreaded Indicator</td>
            <td>No</td>
            <td>multithreaded</td>
            <td>
                A flag indicating whether or not the tool is multithreaded. This
                can be <code>Yes</code> to indicate that the user requesting the
                tool knows that it is multithreaded, <code>No</code> to indicate
                that the user knows that the tool is not multithreaded, or
                anything else to indicate that the user does not know whether or
                not the tool is multithreaded.
            </td>
        </tr>
        <tr>
            <td>Test Data Location</td>
            <td>Yes</td>
            <td>test_data_file</td>
            <td>
                The path to a test data file that has been uploaded to iRODS.
            </td>
        </tr>
        <tr>
            <td>Tool Usage Instructions</td>
            <td>Yes</td>
            <td>cmd_line</td>
            <td>Instructions for using the tool.</td>
        </tr>
        <tr>
            <td>Additional Tool Information</td>
            <td>No</td>
            <td>additional_info</td>
            <td>
                Any additional information that may be helpful during tool
                installation or validation.
            </td>
        </tr>
        <tr>
            <td>Additional Data File</td>
            <td>No</td>
            <td>additional_data_file</td>
            <td>
                Any additional data file that may be helpful during tool
                installation or validation.
            </td>
        </tr>
        <tr>
            <td>Tool Architecture</td>
            <td>Yes</td>
            <td>architecture</td>
            <td>
                One of the architecture names known to the DE. Currently, the
                valid values are `32-bit Generic` for a 32-bit executable that
                will run in the DE, `64-bit Generic` for a 64-bit executable
                that will run in the DE, `Others` for tools run in a virtual
                machine or interpreter, and `Don't know` if the user requesting
                the tool doesn't know what the architecture is.
            </td>
        </tr>
    </tbody>
</table>

The response body is a complete listing of the new tool request as returned by
the GET /tool-request service. Please see the description of that service for
more details.

Here's an example:

```
$ curl -sX PUT -d '
{
    "phone": "520-555-1212",
    "name": "jaguar",
    "description": "a really big cat",
    "src_url": "http://www.example.org/path/to/source.tar.gz",
    "documentation_url": "http://www.example.org/path/to/docs.html",
    "version": "1.0.0",
    "attribution": "An exemplary organization.",
    "multithreaded": "Yes",
    "test_data_file": "/path/to/test_file",
    "cmd_line": "jaguar some-file",
    "additional_info": "some additional info",
    "additional_data_file": "/path/to/additional_file",
    "architecture": "64-bit Generic"
}
' "http://by-tor:8888/secured/tool-request?user=nobody&email=nobody@iplantcollaborative.org" | python -mjson.tool
{
    "additional_data_file": "/path/to/additional_file",
    "additional_info": "some additional info",
    "architecture": "64-bit Generic",
    "attribution": "An exemplary organization.",
    "cmd_line": "jaguar some-file",
    "description": "a really big cat",
    "documentation_url": "http://www.example.org/path/to/docs.html",
    "history": [
        {
            "comments": "",
            "status": "Submitted",
            "status_date": "1364257498649",
            "updated_by": "nobody@iplantcollaborative.org"
        }
    ],
    "multithreaded": true,
    "name": "jaguar",
    "phone": "520-555-1212",
    "source_url": "http://www.example.org/path/to/source.tar.gz",
    "submitted_by": "nobody@iplantcollaborative.org",
    "success": true,
    "test_data_path": "/path/to/test_file",
    "uuid": "7C5ACB09-8675-4F04-B323-78431B801226",
    "version": "1.0.0"
}
```

## Listing Tool Requests

Scured Endpoint: GET /tool-requests

This endpoint lists high level details about tool requests that have been
submitted by a user. The number of results returned and the order of the results
can be controlled by query-string parameters:

<table>
    <thead>
        <tr>
            <th>Parameter Name(s)</th>
            <th>Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>sortfield</td>
            <td rowspan="2">
                The field to use when sorting the tool installation requests.
                This can be any field that appears in each tool request in the
                response body.
            </td>
        </tr>
        <tr>
            <td>sortField</td>
        </tr>
        <tr>
            <td>sortdir</td>
            <td rowspan="2">
                The sort order to use in the response list. This can be either
                `asc` for ascending or `desc` for descending. The values of this
                field are case-insensitive.
            </td>
        </tr>
        <tr>
            <td>sortDir</td>
        </tr>
        <tr>
            <td>limit</td>
            <td>The maximum number of results to return.</td>
        </tr>
        <tr>
            <td>offset</td>
            <td>The index of the first result to return.</td>
        </tr>
    </tbody>
</table>

The response body is in the following format:

```json
{
    "success": true,
    "tool_requests": [
        {
            "date_submitted": "timestamp",
            "date_updated": "timestamp",
            "name": "tool-name",
            "status": "tool-request-status",
            "updated_by": "username",
            "uuid": "tool-request-id",
            "version": "tool-version"
        },
        ...
    ]
}
```

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/tool-requests?user=nobody&limit=1" | python -mjson.tool
{
    "success": true,
    "tool_requests": [
        {
            "date_submitted": "1363047831085",
            "date_updated": "1363047831085",
            "name": "jaguar",
            "status": "Submitted",
            "updated_by": "nobody@iplantcollaborative.org",
            "uuid": "D38A108F-0626-481B-A4C9-356462916642",
            "version": "1.0.0"
        }
    ]
}
```

## Updating the Status of a Tool Request

Unsecured Endpoint: POST /tool-request

This endpoint is used by Discovery Environment administrators to update the
status of a tool request. The request body is in the following format:

```json
{
    "uuid": "tool-request-uuid",
    "status": "new-status-code",
    "username": "de-administrator-username",
    "comments": "administrator-comments"
}
```

The fields are all fairly self-explanatory except that the transitions that the
status code can make are limited. The valid status codes and status code
transitions are listed in the following table:

<table>
    <thead>
        <tr>
            <th>Status Code</th>
            <th>Description</th>
            <th>Reachable From</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan="2">Submitted</td>
            <td rowspan="2">
                This is the initial status code for all tool requests.
            </td>
            <td>Submitted</td>
        </tr>
        <tr>
            <td>Pending</td>
        </tr>
        <tr>
            <td rowspan="5">Pending</td>
            <td rowspan="5">
                Indicates that the support team is awaiting more information
                from the user who submitted the request.
            </td>
            <td>Submitted</td>
        </tr>
        <tr>
            <td>Pending</td>
        </tr>
        <tr>
            <td>Evaluation</td>
        </tr>
        <tr>
            <td>Installation</td>
        </tr>
        <tr>
            <td>Validation</td>
        </tr>
        <tr>
            <td rowspan="3">Evaluation</td>
            <td rowspan="3">
                Indicates that the support team is evaluating the tool for
                installation.
            </td>
            <td>Submitted</td>
        </tr>
        <tr>
            <td>Evaluation</td>
        </tr>
        <tr>
            <td>Pending</td>
        </tr>
        <tr>
            <td rowspan="3">Installation</td>
            <td rowspan="3">
                Indicates that the support team is installing the tool.
            </td>
            <td>Evaluation</td>
        </tr>
        <tr>
            <td>Installation</td>
        </tr>
        <tr>
            <td>Pending</td>
        </tr>
        <tr>
            <td rowspan="2">Validation</td>
            <td rowspan="2">
                Indicates that the support team is verifying that the tool was
                installed correctly.
            </td>
            <td>Installation</td>
        </tr>
        <tr>
            <td>Validation</td>
        </tr>
        <tr>
            <td>Completion</td>
            <td>Indicates that the tool was installed successfully.</td>
            <td>Validation</td>
        </tr>
        <tr>
            <td rowspan="4">Failed</td>
            <td rowspan="4">Indicates that the tool could not be installed.</td>
            <td>Submitted</td>
        </tr>
        <tr>
            <td>Evaluation</td>
        </tr>
        <tr>
            <td>Installation</td>
        </tr>
        <tr>
            <td>Validation</td>
        </tr>
    </tbody>
</table>

The respose body is in the same format as the GET /tool-request service. Please
see the documentation for that service for more information.

Here's an example:

```
$ curl -sd '
{
    "uuid": "7C5ACB09-8675-4F04-B323-78431B801226",
    "status": "Evaluation",
    "username": "someadmin",
    "comments": "About to do the evaluation."
}
' http://by-tor:8888/tool-request | python -mjson.tool
{
    "additional_data_file": "/path/to/additional_file",
    "additional_info": "some additional info",
    "architecture": "64-bit Generic",
    "attribution": "An exemplary organization.",
    "cmd_line": "jaguar some-file",
    "description": "a really big cat",
    "documentation_url": "http://www.example.org/path/to/docs.html",
    "history": [
        {
            "comments": "",
            "status": "Submitted",
            "status_date": "1364257498649",
            "updated_by": "nobody@iplantcollaborative.org"
        },
        {
            "comments": "About to do the evaluation.",
            "status": "Evaluation",
            "status_date": "1364328278490",
            "updated_by": "someadmin@iplantcollaborative.org"
        }
    ],
    "multithreaded": true,
    "name": "jaguar",
    "phone": "520-555-1212",
    "source_url": "http://www.example.org/path/to/source.tar.gz",
    "submitted_by": "nobody@iplantcollaborative.org",
    "success": true,
    "test_data_path": "/path/to/test_file",
    "uuid": "7C5ACB09-8675-4F04-B323-78431B801226",
    "version": "1.0.0"
}
```

## Obtaining Tool Request Details

Unsecured Endpoint: GET /tool-request/{uuid}

This service obtains detailed information about a tool request. This is the
service that the DE support team uses to obtain the request details. The
response body is in the following format:

```json
{
    "additional_data_file": "some-irods-path",
    "additional_info": "some-additional-info",
    "architecture": "tool-architecture-name",
    "attribution": "tool-attribution",
    "cmd_line": "command-line-description",
    "description": "tool-description",
    "documentation_url": "link-to-tool-documentation",
    "history": [
        {
            "comments": "status-change-comments",
            "status": "status-code",
            "status_date": "milliseconds-since-epoch",
            "updated_by": "username"
        },
        ...
    ],
    "multithreaded": "multithreaded-flag",
    "name": "tool-name",
    "phone": "user-phone-number",
    "source_url": "link-or-path-to-tool-source",
    "submitted_by": "username",
    "success": true,
    "test_data_path": "path-to-test-data",
    "uuid": "tool-request-uuid",
    "version": "tool-version"
}
```

Here's an example:

```
$ curl -s http://by-tor:8888/tool-request/7C5ACB09-8675-4F04-B323-78431B801226 | python -mjson.tool
{
    "additional_data_file": "/path/to/additional_file",
    "additional_info": "some additional info",
    "architecture": "64-bit Generic",
    "attribution": "An exemplary organization.",
    "cmd_line": "jaguar some-file",
    "description": "a really big cat",
    "documentation_url": "http://www.example.org/path/to/docs.html",
    "history": [
        {
            "comments": "",
            "status": "Submitted",
            "status_date": "1364257498649",
            "updated_by": "nobody@iplantcollaborative.org"
        },
        {
            "comments": "About to do the evaluation.",
            "status": "Evaluation",
            "status_date": "1364328278490",
            "updated_by": "someadmin@iplantcollaborative.org"
        }
    ],
    "multithreaded": true,
    "name": "jaguar",
    "phone": "520-555-1212",
    "source_url": "http://www.example.org/path/to/source.tar.gz",
    "submitted_by": "nobody@iplantcollaborative.org",
    "success": true,
    "test_data_path": "/path/to/test_file",
    "uuid": "7C5ACB09-8675-4F04-B323-78431B801226",
    "version": "1.0.0"
}
```
