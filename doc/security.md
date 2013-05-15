# Security

None of the services in metadactyl are literally secured. Instead, services in
Donkey provide the security and forward requests to metadactyl, which should not
be accessible from outside the local network. In general, when a secured service
in Donkey forwards requests to metadactyl, the metadactyl services are marked as
secured so that the relationship between the Donkey and metadactyl services is
clear.

Generally speaking, the metadactyl services that correspond to secured services
in Donkey require user credentials, which are passed to the service in query
parameters. For example, the first service that the Discovery Environment hits
when a user logs in is the bootstrap service, which requires user credentials.
This service can be accessed using the URL,
`/bootstrap?user={username}&email={email}` where {username} refers to a user's
login name, and {email} is that user's email address.

Secured services can be distinguished from unsecured services by looking at the
path in the URL. The paths for all secured endpoints begin with `/secured`
whereas the paths for all other endpoints do not. In the documentation below,
services that are not secured will be labeled as unsecured endpoints and
services that are secured will be labeled as secured endpoints.

If the username is not provided in the query string of a request to a service
that requires it then an HTTP 401 (unauthorized) status will result, and there
will be no response body, even if the service normally has a response body.
