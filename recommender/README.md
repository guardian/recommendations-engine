# Recommender

## Running locally

### To configure

    cp conf/gu-conf/SAMPLE.properties conf/gu-conf/DEV.properties

Then open `DEV.properties` and fill in the values for the `elasticsearch.hosts` and
`apis.userhistory.apiKey` properties which are ommitted for security reasons, speak to
@davidfurey or @maxspencer to get them. Note: files in `conf/gu-conf` are ignored by
Git.

    echo "INT_SERVICE_DOMAIN=DEV" >> /etc/gu/install_vars

To ensure the config is loaded from the `DEV.properties` file.

### To run

    sbt run

### To check

    curl -v 127.0.0.1:9000/healthcheck
     
should report a 200 OK response.
