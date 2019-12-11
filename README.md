# health-apis-conformance-unifier
Conformance application to present a unified view of dstu2, stu3, and r4 metadata and well-known smart-configuration, despite different resources being serviced by different applications.

## Local Developer Testing

This application interfaces with any metadata or wellknown endpoints specified on command line.  
The application also stores unified reports to Amazon S3.   
To facilitate development testing, Docker can be used to emulate the Amazon S3 interface.

The dockers provided to facilitate testing include:

* conformance-docker-awss3 - Docker to emulate the Amazon S3 storage facility.

During local development a developer should use the `localtest` maven profile which will build the application to use a test application properties to reference locally mocked S3 using docker.

For example:

1. Build application:
   ```
   mvn -Plocaltest clean install
   ```

2. Start the S3 docker:
    ```
    mvn -Plocaltest docker:start
    ```

3. Once the S3 docker has started, the `conformance-unifier` can run locally using spring-boot:run or by building and running a local docker
    ```
    # 1. Maven Spring Boot Run Examples:
    mvn -Plocaltest -pl conformance-unifier -am spring-boot:run -Dspring-boot.run.arguments="dstu2,metadata,https://api.va.gov/services/fhir/v0/dstu2/metadata"
    
    mvn -Plocaltest -pl conformance-unifier -am spring-boot:run -Dspring-boot.run.arguments="dstu2,smart-configuration,https://api.va.gov/services/fhir/v0/dstu2/.well-known/smart-configuration"
    
    mvn -Plocaltest -pl conformance-unifier -am spring-boot:run -Dspring-boot.run.arguments="r4,metadata,https://api.va.gov/services/fhir/v0/r4/metadata"
    
    mvn -Plocaltest -pl conformance-unifier -am spring-boot:run -Dspring-boot.run.arguments="r4,smart-configuration,https://api.va.gov/services/fhir/v0/r4/.well-known/smart-configuration"

    # 2. Local Docker Build and Run Examples:
    mvn -Plocaltest,docker -pl conformance-unifier io.fabric8:docker-maven-plugin:build

    docker run --network="host" vasdvp/health-apis-conformance-unifier dstu2 metadata https://api.va.gov/services/fhir/v0/dstu2/metadata

    docker run --network="host" vasdvp/health-apis-conformance-unifier dstu2 smart-configuration https://api.va.gov/services/fhir/v0/dstu2/.well-known/smart-configuration

    # To specify a specific profile add the switch `--spring.profiles.active=qa` to the docker command.
    docker run --network="host" vasdvp/health-apis-conformance-unifier --spring.profiles.active=qa r4 smart-configuration https://api.va.gov/services/fhir/v0/r4/.well-known/smart-configuration

    # To specify a specific profile and property to the docker command.
    docker run --network="host" vasdvp/health-apis-conformance-unifier --spring.profiles.active=qa --result.bucket=qaresultbucket r4 smart-configuration https://api.va.gov/services/fhir/v0/r4/.well-known/smart-configuration
    ```

4. You can use regular `aws` commands to see the resulting objects in the mock s3.  For example,
   ```
   # List bucket name:
   aws s3api list-buckets --query "Buckets[].Name" --endpoint-url http://localhost:9090

   # List objects in bucket:
   aws s3api list-objects --bucket testbucket --endpoint-url http://localhost:9090 --query 'Contents[].{Key: Key, Size: Size}'

   # Get most recent object replacing dashes:
   aws s3api --endpoint-url http://localhost:9090 list-objects-v2 --bucket "testbucket" --query 'reverse(sort_by(Contents, &LastModified))[:1].Key' --output=text | awk '{gsub(/%2D/,"-")}1'

   # Copy the specified object to stdout (example showing r4-capability):
   aws s3 --endpoint-url http://localhost:9090 cp s3://testbucket/r4-capability -
   ``` 

5. To Stop the docker:
    ```
    mvn -Plocaltest docker:stop
    ```
