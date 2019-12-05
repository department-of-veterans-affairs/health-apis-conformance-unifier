# Adobe's Amazon S3 Mock Service

Using Adobe's S3Mock to simulate an Amazon S3 service.

## Getting Started

Describes the steps to execute this project.

### Building

There is no need to build the project.  Refer to the next section on Running the mock.

### Running

The adobe/s3mock docker will start.

```
mvn -Plocaltest docker:start
```

Verify the container has started.

```
docker ps

CONTAINER ID        IMAGE                              COMMAND                  CREATED             STATUS              PORTS                                            NAMES
09411ac1be23        adobe/s3mock                       "/bin/sh -c 'java -X…"   8 minutes ago       Up 8 minutes        127.0.0.1:9090-9091->9090-9091/tcp, 9191/tcp     galS3Mock
```

### Stopping the docker container from Maven

To stop the container

```
mvn -Plocaltest docker:stop
```

