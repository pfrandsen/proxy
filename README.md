# proxy

This tool contains the following proxy tools:

* File system proxy - proxy virtual host(s) to local filesystem
* Transparent host proxy

### Build and run

Build jar (e.g., proxy-1.0-SNAPSHOT.jar) with the following command

    mvn clean package

Run with one of the following commands

    java -jar <jar-file> -filesystemProxy <options>
    java -jar <jar-file> -transparentHostProxy <options>

## File system proxy

Proxy for a list of local directories. Each directory is exposed via a virtual host.

A domain can be listed multiple times. If it is listed more than one time the proxy will try to serve the
resource from the first location in the list. If it is not found it will try the second location, then the third etc
See scripts/multiple.sh for an example.

### Command line options

    java -jar <jar-file> -filesystemProxy -help

## Transparent host proxy

### Command line options

    java -jar <jar-file> -transparentHostProxy -help

