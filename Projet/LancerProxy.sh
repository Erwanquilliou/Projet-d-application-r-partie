#!/bin/bash
cd "$(dirname "$0")/src" || exit 1
echo "lancer ServiceWaze"
javac -cp ".;../json-20131018.jar" API/ProxyServer.java
java  -cp ".;../json-20131018.jar" API.ProxyServer localhost localhost
cd - > /dev/null