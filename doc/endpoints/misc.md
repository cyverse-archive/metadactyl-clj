# Miscellaneous Endpoints

## Verifying that metadactyl-clj is Running

Unsecured Endpoint: GET /

The root path in metadactyl-clj can be used to verify that metadactyl-clj is
actually running and is responding. Currently, the response to this URL contains
only a welcome message. Here's an example:

```
$ curl -s http://by-tor:8888/
Welcome to Metadactyl!
```

## Initializing a User's Workspace

Secured Endpoint: GET /secured/bootstrap

The DE calls this service as soon as the user logs in. If the user has never
logged in before then the service initializes the user's workspace and returns
the user's workspace ID. If the user has logged in before then the service
merely returns the user's workspace ID. The response body for this service is in
the following format:

```json
{
    "workspaceId": workspace-id
}
```

Here's an example:

```
$ curl -s "http://by-tor:8888/secured/bootstrap?user=snow-dog&email=sd@example.org" | python -mjson.tool
{
    "workspaceId": "4"
}
```
