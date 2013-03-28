# Table of Contents

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
