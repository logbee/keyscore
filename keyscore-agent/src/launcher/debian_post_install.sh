#!/bin/bash

/bin/chmod -R 755 /usr/share/keyscore/keyscore-agent/jdk8

/bin/sed -i 's_CMD="java_CMD="./jdk8/bin/java_g' /usr/share/keyscore/keyscore-agent/keyscore-agent