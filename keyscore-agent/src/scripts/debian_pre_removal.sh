#!/bin/bash

# 1.1 Disable service
systemctl disable ks_agent
# 1.2 Stop service
systemctl stop ks_agent
#1.3 Remove service
rm /etc/systemd/system/ks_agent.service