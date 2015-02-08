#!/bin/bash
pushd ..
mvn clean package

echo
echo "Important! Chrome must be shut down"
echo "ie. _not_ running in background (as is default) and no open windows"
echo "else --proxy-server command line option has no effect"
echo

hosts="a.com,b.com"
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

portTP=8999
prgTB="java -jar target/proxy-1.0-SNAPSHOT.jar -transparentHostProxy -chatty -port ${portTP}"
cmd2="${prgTB} -proxyToUri http://localhost:${port} -hosts ${hosts} -shutdownKey ${sdkey}"
echo "Running: ${cmd2}"
`exec x-terminal-emulator 1>&2 -e $cmd2`

sleep 2s # if Chrome is already running it may launch tab before proxies are initialized

# start two Chrome tabs that are proxied through transparent host proxy to file system proxy (using two
# different handlers)

google-chrome --incognito --proxy-server="localhost:${portTP}" http://a.com/sample.html &
google-chrome --incognito --proxy-server="localhost:${portTP}" http://b.com/context/sample.html &

# start one Chrome tab that is not proxied (using system proxy or direct connection to the internet)

google-chrome --incognito --proxy-server="localhost:${portTP}" http://googlr.com &

echo
echo "To shut down filesystem proxy server, close terminal where proxy is running"
echo "or send GET request to shutdown url: wget http://localhost:${port}${sdcontext}/${sdkey}"
echo
echo "To shut down transparent proxy server, close terminal where proxy is running"
echo "or send GET request to shutdown url: wget http://localhost:${portTP}/shutdown/${sdkey}"
echo

popd
