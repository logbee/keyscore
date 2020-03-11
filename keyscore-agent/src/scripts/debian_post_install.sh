#!/bin/bash

#TODO keyscore-agent instead of ks_agent

# 1. Make jdk accessible
/bin/chmod -R 755 /usr/share/keyscore/keyscore-agent/jdk8

# 2. Edit start script for the keyscore-agent
/bin/sed -i 's_CMD="java_CMD="/usr/share/keyscore/keyscore-agent/jdk8/bin/java_g' /usr/share/keyscore/keyscore-agent/bin/keyscore-agent
/bin/chmod -R 755 /usr/share/keyscore/keyscore-agent/bin/keyscore-agent

#3. Check if systemd is available
SYSTEMD="/usr/lib/systemd/"
if [ -d $SYSTEMD ]
  then
    # 3.1 Place the keyscore-agent service
    cp /usr/share/keyscore/keyscore-agent/scripts/ks_agent.service /etc/systemd/system/
    # 3.2 Enable service
    systemctl enable ks_agent
    # 3.3 Start service
    systemctl start ks_agent
fi