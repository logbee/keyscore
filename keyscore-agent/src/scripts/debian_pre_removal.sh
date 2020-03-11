#!/bin/bash

#TODO keyscore-agent instead of ks_agent

#1 Check if systemd is available
SYSTEMD="/usr/lib/systemd/"
if [ -d $SYSTEMD ]
  then
    # 1.1 Disable service
    systemctl disable ks_agent
    # 1.2 Stop service
    systemctl stop ks_agent
    #1.3 Remove service
    rm /etc/systemd/system/ks_agent.service
fi

#2 Remove extensions
rm -rf /usr/share/keyscore/keyscore-agent/bin/extensions