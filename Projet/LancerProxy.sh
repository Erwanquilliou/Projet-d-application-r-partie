#!/bin/bash
cd "$(dirname "$0")/src" || exit 1
echo "compiler Proxy"
javac -cp ".:../json-20131018.jar" API/ProxyServerHTTPS.java
echo "lancer Proxy"
java  -cp ".:../json-20131018.jar" API.ProxyServerHTTPS $1 $2
cd - > /dev/null