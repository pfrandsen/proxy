#!/bin/bash
pushd ..
mvn clean package

echo
echo "Important! Chrome must be shut down"
echo "ie. _not_ running in background (as is default) and no open windows"
echo "else --proxy-server command line option has no effect"
echo

port=9999
prg="java -jar target/proxy-1.0-SNAPSHOT.jar -filesystemProxy -chatty -port ${port}"
root="-filesystemProxy -root src/main/test/resource/root"
vh="-virtualHosts a.com,b.com"
subdir="-subdirectories subdir-a,subdir-b"
context="-contextPaths /,/context"
names="-logicalNames Interceptor-A,Interceptor-B"
sdcontext="/shutdown"
sdkey="1337"
sd="-shutdownContext ${sdcontext} -shutdownKey ${sdkey}"
cmd1="${prg} ${root} ${vh} ${subdir} ${context} ${names} ${sd}"

echo "Running: ${cmd1}"
`exec x-terminal-emulator 1>&2 -e $cmd1`
sleep 5s
google-chrome --incognito --proxy-server="localhost:${port}" http://a.com/sample.html &
google-chrome --incognito --proxy-server="localhost:${port}" http://b.com/context/sample.html &

echo
echo "To shut down proxy server, close terminal where proxy is running"
echo "or send GET request to http://localhost${sdcontext}/${sdkey}"
echo

popd
