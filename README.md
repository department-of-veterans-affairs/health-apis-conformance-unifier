# health-apis-conformance-unifier
Conformance application to present a unified view of dstu2, stu3, and r4 metadata and well-known smart-configuration, despite different resources being serviced by different applications.

## Local Developer Testing

This application interfaces with any metadata or wellknown endpoints specified on command line.  
The application also stores unified reports to Amazon S3.   
To facilitate development testing, Docker can be used to emulate the Amazon S3 interface.

The dockers provided to facilitate testing include:

* conformance-docker-awss3 - Docker to emulate the Amazon S3 storage facility.

A developer can use the test docker in a local development environment by running using the `localtest` maven profile.

1. Start the dockers:
    ```
    mvn -Plocaltest docker:start
    ```

2. Once the S3 docker has started, the `conformance-unifier` can run locally:
    ```
    cd conformance-unifier
    
    mvn spring-boot:run -Dspring-boot.run.arguments="dstu2,metadata,https://api.va.gov/services/fhir/v0/dstu2/metadata"
    
    mvn spring-boot:run -Dspring-boot.run.arguments="dstu2,smart-configuration,https://api.va.gov/services/fhir/v0/dstu2/.well-known/smart-configuration"
    
    mvn spring-boot:run -Dspring-boot.run.arguments="r4,metadata,https://api.va.gov/services/fhir/v0/r4/metadata"
    
    mvn spring-boot:run -Dspring-boot.run.arguments="r4,smart-configuration,https://api.va.gov/services/fhir/v0/r4/.well-known/smart-configuration"
    ```

3. You can use regular `aws` commands to see the resulting objects in the mock s3.  For example,
   ```
   # List bucket name:
   aws s3api list-buckets --query "Buckets[].Name" --endpoint-url http://localhost:9090

   # List objects in bucket:
   aws s3api list-objects --bucket testbucket --endpoint-url http://localhost:9090 --query 'Contents[].{Key: Key, Size: Size}'

   # Get most recent object replacing underscores:
   aws s3api --endpoint-url http://localhost:9090 list-objects-v2 --bucket "testbucket" --query 'reverse(sort_by(Contents, &LastModified))[:1].Key' --output=text | awk '{gsub(/%5F/,"_")}1'

   # Copy the specified object to stdout:
   aws s3 --endpoint-url http://localhost:9090 cp s3://testbucket/2019_12_09_11_05_22 -
   ``` 

4. To Stop the docker:
    ```
    cd ..
    mvn -Plocaltest docker:stop
    ```
