# Table of Contents

* [App Execution Endpoints](#app-execution-endpoints)
    * [Obtaining Property Values for a Previously Executed Job](#obtaining-property-values-for-a-previously-executed-job)
    * [Obtaining Information to Rerun a Job](#obtaining-information-to-rerun-a-job)
    * [Submitting a Job for Execution](#submitting-a-job-for-execution)
    * [Listing Jobs](#listing-jobs)
    * [Getting Status Information for Selected Jobs](#getting-status-information-for-selected-jobs)
    * [Deleting Jobs](#deleting-jobs)

# App Execution Endpoints

## Obtaining Property Values for a Previously Executed Job

Unsecured Endpoint: GET /get-property-values/{job-id}

This service obtains the property values that were passed to a job that has
already been executed so that the user can see which values were passed to the
job. The response body is in the following format:

```json
{
    "analysis_id": "analysis-id",
    "parameters": [
        {
            "full_param_id": "fully-qualified-parameter-id",
            "param_id": "parameter-id",
            "param_name": "parameter-name",
            "param_value": "parameter-value",
            "param_type": "parameter-type",
            "info_type": "info-type-name",
            "data_format": "data-format-name",
            "is_default_value": "default-value-flag",
            "is_visible": "visibility-flag"
        },
        ...
    ]
}
```

Note that the information type and data format only apply to input files. For
other types of parameters, these fields will be blank. The `is_default_value`
flag indicates whether or not the default value was used in the job submission.
The value of this flag is determined by comparing the actual property value
listed in the job submission to the default property value in the application
definition. If the default value in the application definition is not blank and
the actual value equals the default value then this flag will be set to `true`.
Otherwise, this flag will be set to `false`. The `is_visible` flag indicates
whether or not the property is visible in the user interface for the
application. This value is copied directly from the application definition.

Here's an example:

```
$ curl -s http://by-tor:8888/get-property-values/jebf8120d-0ccb-45d1-bae6-849620f31553 | python -mjson.tool
{
    "analysis_id": "t55e2377c60724ecbbcfa1a39c9ef1eec",
    "parameters": [
        {
            "data_format": "Unspecified",
            "info_type": "File",
            "is_default_value": false,
            "is_visible": true,
            "full_param_id": "step1_38950035-8F31-0A27-1BE1-8E55F5C30B54",
            "param_id": "38950035-8F31-0A27-1BE1-8E55F5C30B54",
            "param_name": "Select an SRA or SRAlite file:",
            "param_type": "Input",
            "param_value": "/iplant/home/nobody/SRR001355.lite.sra"
        },
        {
            "data_format": "",
            "info_type": "",
            "is_default_value": true,
            "is_visible": true,
            "full_param_id": "step1_B962E548-4023-E40C-48E5-6484AF55E5DD",
            "param_id": "B962E548-4023-E40C-48E5-6484AF55E5DD",
            "param_name": "Optional accession override",
            "param_type": "Text",
            "param_value": ""
        },
        {
            "data_format": "",
            "info_type": "",
            "is_default_value": true,
            "is_visible": true,
            "full_param_id": "step1_DCFC3CD9-FB31-E0F8-C4CB-78F66FF368D2",
            "param_id": "DCFC3CD9-FB31-E0F8-C4CB-78F66FF368D2",
            "param_name": "File contains paired-end data",
            "param_type": "Flag",
            "param_value": "true"
        },
        {
            "data_format": "",
            "info_type": "",
            "is_default_value": true,
            "is_visible": true,
            "full_param_id": "step1_0E21A202-EC8A-7BFD-913B-FA73FE86F58E",
            "param_id": "0E21A202-EC8A-7BFD-913B-FA73FE86F58E",
            "param_name": "Offset to use for quality scale conversion",
            "param_type": "Number",
            "param_value": "33"
        },
        {
            "data_format": "",
            "info_type": "",
            "is_default_value": true,
            "is_visible": true,
            "full_param_id": "step1_F9AD602D-38E3-8C90-9DD7-E1BB4971CD70",
            "param_id": "F9AD602D-38E3-8C90-9DD7-E1BB4971CD70",
            "param_name": "Emit only FASTA records without quality scores",
            "param_type": "Flag",
            "param_value": "false"
        },
        {
            "data_format": "",
            "info_type": "",
            "is_default_value": true,
            "is_visible": false,
            "full_param_id": "step1_6BAD8D7F-3EE2-A52A-93D1-1329D1565E4F",
            "param_id": "6BAD8D7F-3EE2-A52A-93D1-1329D1565E4F",
            "param_name": "Verbose",
            "param_type": "Flag",
            "param_value": "true"
        }
    ]
}
```

## Obtaining Information to Rerun a Job

Unsecured Endpoint: GET /analysis-rerun-info/{job-id}

It's occasionally nice to be able to rerun a job that was prevously executed,
possibly with some tweaked values. The UI uses this service to obtain analysis
information in the same format as the `/get-analysis/{analysis-id}` service with
the property values from a specific job plugged in. Here's an example:

```
$ curl -s http://by-tor:8888/analysis-rerun-info/j41bef770-f68c-40a2-8da4-2f53e22d4a9b | python -mjson.tool
{
    "groups": [
        {
            "id": "3C17C860-AF27-468F-A8F2-64894B31DA23",
            "label": "Input and Output",
            "name": "",
            "properties": [
                {
                    "description": "Select the files to concatenate.",
                    "id": "Puma_733743D0-42BB-471A-BC53-63E0DBD5F41D",
                    "isVisible": true,
                    "label": "Input Files",
                    "name": "",
                    "type": "MultiFileSelector",
                    "validator": {
                        "label": "",
                        "name": "",
                        "required": true
                    },
                    "value": [
                        "/iplant/home/snow-dog/AllButRootHaveDistanceToParent.newick",
                        "/iplant/home/snow-dog/allNodesNamed.newick"
                    ]
                },
                {
                    "description": "Specify the name of the output file.",
                    "id": "Puma_5C540330-9858-460F-B1D4-CD760B99D85F",
                    "isVisible": true,
                    "label": "Output File",
                    "name": "",
                    "type": "Output",
                    "validator": {
                        "label": "",
                        "name": "",
                        "required": true
                    },
                    "value": "puma.txt"
                },
                {
                    "description": "Specify the name of the error output file.",
                    "id": "Puma_902B0804-17B7-456F-94D2-D09DC2D2ADE2",
                    "isVisible": true,
                    "label": "Error Output File",
                    "name": "",
                    "type": "Output",
                    "validator": {
                        "label": "",
                        "name": "",
                        "required": true
                    },
                    "value": "puma.err"
                }
            ],
            "type": ""
        },
        {
            "id": "90B15DA1-9DFF-463C-AA4A-6EB0DE1DA022",
            "label": "Options",
            "name": "",
            "properties": [
                {
                    "description": "Indicate whether lines should be numbered in the output file.",
                    "id": "Puma_FE1F8E52-FECC-462C-B5F8-5E4A8EAC6FBC",
                    "isVisible": true,
                    "label": "Number Lines",
                    "name": "-n",
                    "type": "Flag",
                    "value": "true"
                }
            ],
            "type": ""
        }
    ],
    "id": "t55e2377c60724ecbbcfa1a39c9ef1eec",
    "label": "Puma",
    "name": "Puma",
    "success": true,
    "type": ""
}
```

## Submitting a Job for Execution

Secured Endpoint: PUT /secured/workspaces/{workspace-id}/newexperiment

The DE uses this service to submit jobs for execution on behalf of the user. The
request body is in the following format:

```json
{
    "config": {
        property-id-1: "property-value-1",
        property-id-2: "property-value-2",
        ...,
        property-id-n: "property-value-n"
    },
    "analysis_id": "analysis-id",
    "name": "job-name",
    "type": "job-type",
    "debug": "debug-flag",
    "workspace_id": "workspace-id",
    "notify": "email-notifications-enabled-flag",
    "output_dir": "output-directory-path",
    "create_output_subdir": "auto-create-subdir-flag",
    "description": "job-description"
}
```

The property identifiers deserve some special mention here because they're not
obtained directly from the database. If you examine the output from the
`/get-analysis/{analysis-id}` endpoint or the `/template/{analysis-id}` endpoint
then these property identifiers are the ones that show up in the service output.
If you're looking in the database (or in the output from the
`/export-workflow/{analysis-id}` endpoint) then you can obtain the property ID
used in this service by combining the step name, a literal underscore and the
actual property identifier.

This service produces a response body consisting of a single JSON object
containing the job status information. Here's an example:

```
$ curl -XPUT -sd '
{
 "config":   {
   "step_1_LastLines": "1",
   "step_1_6FF31B1C-3DAB-499C-8521-69227C52CE10": "/iplant/home/snow-dog/data_files/aquilegia-tree.txt"
 },
 "analysis_id": "aa54b4fd9b56545db978fff4398c5ce81",
 "name": "a1",
 "type": "Text Manipulation",
 "debug": false,
 "workspace_id": "4",
 "notify": true,
 "output_dir": "/iplant/home/snow-dog/sharewith",
 "create_output_subdir": true,
 "description": ""
}
' http://by-tor:8888/secured/workspaces/4/newexperiment?user=snow-dog | python -mjson.tool
{
   "analysis_details": "Extracts a specified number of lines from the beginning of file",
   "analysis_id": "aa54b4fd9b56545db978fff4398c5ce81",
   "analysis_name": "Extract First Lines From a File",
   "description": "",
   "enddate": "0",
   "id": "jf7600670-ed13-46cd-8810-dfddb075d819",
   "name": "a1",
   "resultfolderid": "/iplant/home/snow-dog/sharewith/a1-2012-10-10-18-44-47.548",
   "startdate": "1349919887549",
   "status": "Submitted",
   "success": true,
   "wiki_url": "https://pods.iplantcollaborative.org/wiki/display/DEapps/Extract%20First%20Lines%20From%20a%20File"
}
```

## Listing Jobs

Secured Endpoint: GET /secured/workspaces/{workspace-id}/executions/list

Information about the status of jobs that have previously been submitted for
execution can be obtained using this service. The DE uses this service to
populate the _Analyses_ window. The response body for this service is in the
following format:

```json
{
    "analyses": [
        {
            "analysis_details": "analysis-description",
            "analysis_id": "analysis-id",
            "analysis_name": "analysis-name",
            "description": "job-description",
            "enddate": "end-date-as-milliseconds-since-epoch",
            "id": "job-id",
            "name": "job-name",
            "resultfolderid": "path-to-result-folder",
            "startdate": "start-date-as-milliseconds-since-epoch",
            "status": "job-status-code",
            "wiki_url": "analysis-documentation-link"
        },
        ...
    ],
    "success": true
}
```

With no query string parameters aside from `user` and `email`, this service
returns information about all jobs ever run by the user that haven't been marked
as deleted in descending order by start time (that is, the `startdate` field in
the result). Several query-string parameters are available to alter the way this
service behaves:

<table border="1">
    <thead>
        <tr>
            <th>Name</th>
            <th>Description</th>
            <th>Default</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>limit</td>
            <td>
                The maximum number of results to return.  If this value is zero
                or negative then all results will be returned.
            </td>
            <td>0</td>
        </tr>
        <tr>
            <td>offset</td>
            <td>The index of the first result to return.</td>
            <td>0</td>
        </tr>
        <tr>
            <td>filter</td>
            <td>
                Allows results to be filtered based on the value of some
                result field.  The format of this parameter is
                `[{"field":"some_field", "value":"search-term"}, ...]`,
                where `field` is the mame of the field on which the filter is
                based and `value` is the search value that can be contained
                anywhere, case-insensitive, in the corresponding analysis field.
                For example, to obtain the list of all jobs that were
                executed using the application, `CACE`, the parameter value
                can be `[{"field":"analysis_name","value":"cace"}]`.
                Additional filters may be provided in the query array, and any
                analysis that matches any filter will be returned.
            </td>
            <td>No filtering</td>
        </tr>
        <tr>
            <td>sort-field</td>
            <td>The name of the field that results are sorted by.</td>
            <td>startdate</td>
        </tr>
        <tr>
            <td>sort-order</td>
            <td>
                `asc` or `ASC` for ascending and `desc` or `DESC` for descending.
            </td>
            <td>desc</td>
        </tr>
    </tbody>
</table>

Here's an example using no parameters:

```
$ curl -s "http://by-tor:8888/secured/workspaces/4/executions/list?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "analyses": [
        {
            "analysis_details": "Find significant changes in transcript expression, splicing, and promoter use across RNAseq alignment data files",
            "analysis_id": "516ED301-E250-40BC-B2BC-31DD7B64D3BA",
            "analysis_name": "CuffDiff",
            "description": "Selecting a non-default file for output. ",
            "enddate": "1329252482000",
            "id": "BD421AF3-2C6E-4A92-A215-D380CD6FECC8",
            "name": "CuffDiffTest1",
            "resultfolderid": "/iplant/home/snow-dog/analyses/CuffDiff/",
            "startdate": "1329252412998",
            "status": "Failed",
            "wiki_url": "https://pods.iplantcollaborative.org/wiki/some/doc/link/CuffDiff"
        },
        ...
    ]
}
```

Here's an example of a filtered search with a limit of one result:

```
$ curl -s "http://by-tor:8888/secured/workspaces/4/executions/list?user=snow-dog&email=sd@example.org&filter=analysis_name=CACE&limit=1" | python -mjson.tool
{
    "analyses": [
        {
            "analysis_details": "Maximum likelihood ancestral character estimation for continuous traits",
            "analysis_id": "4BA117B1-0BFB-F4B2-C5B0-AABE56CF8406",
            "analysis_name": "CACE",
            "description": "",
            "enddate": 1346444723000,
            "id": "j34b7dd71-1a72-45fb-9569-c68a71f0b58d",
            "name": "analysis1",
            "resultfolderid": "/iplant/home/snow-dog/analyses/analysis1-2012-08-31-13-25-03.493",
            "startdate": 1346444703493,
            "status": "Failed",
            "wiki_url": "https://pods.iplantcollaborative.org/wiki/display/DEapps/CACE"
        }
    ],
    "success": true
}
```

## Getting Status Information for Selected Jobs

Secured Endpoint: POST /secured/workspaces/{workspace-id}/executions/list

The UI needs to be able to retrieve status information for selected jobs when
updating the analyses window. This endpoint provides it with the means to do so.
This endpoint takes no query-string parameters but, instead, takes a list of job
IDs in a JSON request body in the following format:

```json
{
    "executions": [
        "job-id-1",
        "job-id-2",
        ...,
        "job-id-n"
    ]
}
```

The response body for this endpoint is in the same format as the GET request for
the same URL path:

```json
{
    "analyses": [
        {
            "analysis_details": "analysis-description",
            "analysis_id": "analysis-id",
            "analysis_name": "analysis-name",
            "description": "job-description",
            "enddate": "end-date-as-milliseconds-since-epoch",
            "id": "job-id",
            "name": "job-name",
            "resultfolderid": "path-to-result-folder",
            "startdate": "start-date-as-milliseconds-since-epoch",
            "status": "job-status-code",
            "wiki_url": "analysis-documentation-link"
        },
        ...
    ],
    "success": true
}
```

Here's an example:

```
$ curl -sd '
{
    "executions": [
        "j34b7dd71-1a72-45fb-9569-c68a71f0b58d",
        "j2d719268-4b12-440b-b086-89228c1ecbe6"
    ]
}
' "http://by-tor:8888/secured/workspaces/4/executions/list?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "analyses": [
        {
            "analysis_details": "Maximum likelihood ancestral character estimation for continuous traits",
            "analysis_id": "4BA117B1-0BFB-F4B2-C5B0-AABE56CF8406",
            "analysis_name": "CACE",
            "description": "",
            "enddate": 1346443377000,
            "id": "j2d719268-4b12-440b-b086-89228c1ecbe6",
            "name": "analysis1",
            "resultfolderid": "/iplant/home/snow-dog/analyses/analysis1-2012-08-31-13-02-37.512",
            "startdate": 1346443357512,
            "status": "Failed",
            "wiki_url": "https://pods.iplantcollaborative.org/wiki/display/DEapps/CACE"
        },
        {
            "analysis_details": "Maximum likelihood ancestral character estimation for continuous traits",
            "analysis_id": "4BA117B1-0BFB-F4B2-C5B0-AABE56CF8406",
            "analysis_name": "CACE",
            "description": "",
            "enddate": 1346444723000,
            "id": "j34b7dd71-1a72-45fb-9569-c68a71f0b58d",
            "name": "analysis1",
            "resultfolderid": "/iplant/home/snow-dog/analyses/analysis1-2012-08-31-13-25-03.493",
            "startdate": 1346444703493,
            "status": "Failed",
            "wiki_url": "https://pods.iplantcollaborative.org/wiki/display/DEapps/CACE"
        }
    ],
    "success": true
}
```

## Deleting Jobs

Secured Endpoint: PUT /secured/workspaces/{workspace-id}/executions/delete

After a job has completed, a user may not want to view the job status
information in the _Analyses_ window any longer. This service provides a way to
mark job status information as deleted so that it no longer shows up. The
request body for this service is in the following format:

```json
{
    "executions": [
        "job-id-1",
        "job-id-2",
        ...,
        "job-id-n"
    ]
}
```

The response body for this endpoint contains only a status flag if the service
succeeds.

It should be noted that this service does not fail if any of the job identifiers
refers to a non-existent or deleted job. If the identifier refers to a deleted
job then the update is essentially a no-op. If a job with the identifier can't
be found then a warning message is logged in metadactyl-clj's log file, but the
service does not indicate that a failure has occurred.

Here's an example:

```
$ curl -X PUT -sd '
{
    "executions": [
        "84DFCC0E-03B9-4DF4-8484-55BFBD6FE841",
        "FOO"
    ]
}
' "http://by-tor:8888/secured/workspaces/4/executions/delete?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "success": true
}
```
