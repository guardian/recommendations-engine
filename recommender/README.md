# Recommender

## Running locally

### To configure

You must set the properties below in the file `conf/gu-conf/DEV.properties`. The values for
`elasticsearch.hosts` and `apis.userhistory.apiKey` are omitted, speak to @davidfurey or
@maxspencer to get it:

    apis.predictionio.base=http://engine.mobile-aws.guardianapis.com:8000/queries.json
    apis.mobile-api.items.base=http://mobile-apps.guardianapis.com/items
    elasticsearch.hosts=
    apis.userhistory.apiKey=
    apis.userhistory.base=https://user-history.guardianapis.com/v1
    
Next you must create a file `/etc/gu/install_vars` with the contents `INT_SERVICE_DOMAIN=DEV`
to ensure the config is loaded from the `DEV.properties` file.

### To run

    sbt run

### To check

    curl -v 127.0.0.1:9000/healthcheck
     
should report a 200 OK response.
