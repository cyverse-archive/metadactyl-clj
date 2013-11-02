# Table of Contents

* [App Metadata Information Services](#app-metadata-information-services)
    * [Getting Analyses in the JSON Format Required by the DE](#getting-analyses-in-the-json-format-required-by-the-de)
    * [Getting Analysis Details](#getting-analysis-details)
    * [Getting an App Description](#getting-an-app-description)

# App Metadata Information Services

## Getting Analyses in the JSON Format Required by the DE

*Unsecured Endpoint:* GET /get-analysis/{analysis-id}

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

*Secured Endpoint:* GET /secured/template/{analysis-id}

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

## Getting Analysis Details

*Unsecured Endpoint:* GET /analysis-details/{analysis-id}

This service is used by the DE to obtain high-level details about a single
analysis. The response body is in the following format:

```json
{
    "components": [
        {
            "id": "component-id",
            "name": "component-name",
            "description": "component-description",
            "location": "component-location",
            "type": "executable",
            "version": "component-version",
            "attribution": "component-attribution"
        }
    ],
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
    "groups": [
        {
            "name": "Beta",
            "id": "g5401bd146c144470aedd57b47ea1b979"
        }
    ],
    "tito": "analysis-id",
    "type": "component-type"
}
```

This service will fail if the analysis isn't found or is a pipeline (that is, it
contains multiple steps). Here are some examples:

```
$ curl -s "http://by-tor:8888/analysis-details/0309394C-37C9-4A64-A806-C12674D2D4F8" | python -mjson.tool
{
    "components": [
        {
            "attribution": "Rice P, Longden I, Bleasby A. EMBOSS: the European Molecular Biology Open Software Suite. Trends Genet. 2000 Jun;16(6):276-7",
            "description": "Needleman-Wunsch global alignment",
            "id": "c3610c827b37d4c4ba5f18cc1edeb72e5",
            "location": "/usr/local3/bin/emboss/bin",
            "name": "needle",
            "type": "executable",
            "version": "6.4.0"
        }
    ],
    "description": "needle reads two input sequences and writes their optimal global sequence alignment to file.",
    "edited_date": "1334734950952",
    "groups": [
        {
            "id": "4B3A8586-9A55-441C-8ED2-D730D60BF5F1",
            "name": "EMBOSS"
        }
    ],
    "id": "0309394C-37C9-4A64-A806-C12674D2D4F8",
    "label": "",
    "name": "EMBOSS Needle",
    "published_date": "1334734995546",
    "references": [],
    "suggested_groups": [],
    "tito": "0309394C-37C9-4A64-A806-C12674D2D4F8"
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

## Getting an App Description

Unsecured Endpoint: GET /get-app-description/{analysis-id}

This service is used by Donkey to get app descriptions for job status update
notifications. There is no request body and the response body contains only the
analysis description, with no special formatting.  Here's an example:

```
$ curl http://by-tor:8888/get-app-description/FA65A1AF-8B9D-4151-9073-2A5D1874F8C0 && echo
Lorem ipsum dolor sit amet
```
