# Recommender

## Running locally

### To configure

You must set the properties below in the file `conf/gu-conf/DEV.properties`. The value for
`apis.userhistory.apiKey` is omitted, speak to @davidfurey or @maxspencer to get it:

    apis.predictionio.base=http://engine.mobile-aws.guardianapis.com:8000/queries.json
    apis.mobile-api.items.base=http://mobile-apps.guardianapis.com/items
    elasticsearch.hosts=ec2-52-49-247-246.eu-west-1.compute.amazonaws.com,ec2-52-19-52-49.eu-west-1.compute.amazonaws.com,ec2-52-49-209-215.eu-west-1.compute.amazonaws.com
    apis.userhistory.apiKey=
    apis.userhistory.base=https://user-history.guardianapis.com/v1
    
Next you must create a file `/etc/gu/install_vars` with the contents `INT_SERVICE_DOMAIN=DEV` to ensure the config is
loaded from the `DEV.properties` file.

### To run

    sbt run

### To check

    curl -v 127.0.0.1:9000/healthcheck
     
should report a 200 OK response.
