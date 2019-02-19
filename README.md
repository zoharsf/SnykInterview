# SnykInterview

## Implementation of a service that, given a name and version of an npm package, returns its dependent packages. The service exposes an HTTP api and responds to HTTP POST requests with the name and version of any npm packages which the requested package depends upon.

### Goal
* Create a service that given a name and version of an npm package, responds with the name and version of any npm packages which the requested package depends upon. 
* The user will send an HTTP POST request to the service with the name and version of an npm package.
* The service will respond to the request with the name and version of any npm packages which the requested package depends upon.

### Running
* Deploy jar file to target machine.
* Run in command line (replace <PATH_TO_JAR> with the full path to the location of the jar file from the previous bullet):
```
java -jar <PATH_TO_JAR>
```

### Interacting With The Service
#### Dependency Request
Send the following POST request to the service:
```
http://<host>:8080/dependencies
```
Or:
```
POST /dependencies HTTP/1.1
Host: <host>:8080
Content-Type: application/json
Cache-Control: no-cache

<payload>
```

#### Dependency Tree Request
Send the following POST request to the service:
```
http://<host>:8080/dependencyTree
```
Or:
```
POST /dependencyTree HTTP/1.1
Host: <host>:8080
Content-Type: application/json
Cache-Control: no-cache

<payload>
```

### Payload
Both the Dependency Request and the Dependency Tree Request require a payload to be sent as part of the request. 
The format of the payload is a flat JSON which incudes the name and the version of the requested npm package. For example:
```
{
	"name":"depd",
	"version":"1.1.1"
}
```

### Responses
#### Dependency Request Response
The response is a flat JSON which incudes the names and the versions of the dependencies for the requested npm package. For example: 
```
[
    {
        "name": "async",
        "version": "2.0.1"
    },
    {
        "name": "lodash",
        "version": "4.8.0"
    }
]
```
#### Dependency Tree Request Response
The response is a hierarchical JSON which incudes the names and the versions of the dependencies for the requested npm package. For example:
```
{
    "npmPackage": {
        "name": "proxy-addr",
        "version": "2.0.4"
    },
    "packageDependencyTree": [
        {
            "npmPackage": {
                "name": "forwarded",
                "version": "0.1.2"
            },
            "packageDependencyTree": []
        },
        {
            "npmPackage": {
                "name": "ipaddr.js",
                "version": "1.8.0"
            },
            "packageDependencyTree": []
        }
    ]
}
```

## Under The Hood:
### Dependency Request:
When an HTTP POST request is received, the service will do the following:
* Check if the cache already includes the reuested package.
* If so, the dependencies will be retreived directly from the cache.
* If not, the dependencies will be retreived from the registry.
* Any dependency retreived from the registry will be added to a queue which its members will also be subject to retreival from the registry. 
* Any dependency retreived from the registry will be added to the cache.
* The response sent to the user will be a flat JSON which will include all dependencies and their versions.

### Dependency Tree Request:
When an HTTP POST request is received, the service will do the following:
* Check if the cache already includes the reuested package.
* If so, the dependencies will be retreived recursively from the cache.
* If not, the dependencies will be retreived from the registry.
* Any dependency retreived from the registry will be added to a queue which its members will also be subject to retreival from the registry. 
* Any dependency retreived from the registry will be added to the cache.
* After adding all of the dependencies to the cache, the dependencies will be retreived recursively from the cache in order to create a dependency tree.
* The response sent to the user will be a JSON which will include all dependencies and their versions ordered hierarchically.

### Notes:
The service has very rich logs.
The logs include:
* Incoming requests.
* The outcome of checking the cache for the requested package.
* The request sent to the registry.
* the request URL to the registry
* The dependencies extracted from the registry's response.
* The number and a list of dependencies found for the requested package.
* Any errors encountered
 
### Future improvements:
- [ ] Add security features to service.
- [ ] Dockerize service.
- [ ] Add tests to project.
- [ ] Write dependencies to DB to allow cache population on startup.
- [ ] Improve code efficiency.
