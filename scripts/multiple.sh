#!/bin/bash
pushd ..
mvn clean package

hosts="domain.com,domain.com,domain.com"
port=9999
prg="java -jar target/proxy-1.0-SNAPSHOT.jar -filesystemProxy -chatty -port ${port}"
root="-root src/main/test/resource/root/multiple"
vh="-virtualHosts ${hosts}"
subdir="-subdirectories location1,location2,location3"
names="-logicalNames Interceptor-A,Interceptor-B,Interceptor-C"
sdcontext="/shutdown"
sdkey="1337"
sd="-shutdownContext ${sdcontext} -shutdownKey ${sdkey}"
cmd1="${prg} ${root} ${vh} ${subdir} ${names} ${sd}"

echo "Running: ${cmd1}"
`exec x-terminal-emulator 1>&2 -e $cmd1`

sleep 2s # time for proxy to startup before accessing it

# retrieve resources - each served from the same domain by different resource handlers

curl --proxy "localhost:${port}" http://domain.com/a.html
curl --proxy "localhost:${port}" http://domain.com/b.html
curl --proxy "localhost:${port}" http://domain.com/c.html

echo
echo "To shut down filesystem proxy server, close terminal where proxy is running"
echo "or send GET request to shutdown url: wget http://localhost:${port}${sdcontext}/${sdkey}"
echo

popd
