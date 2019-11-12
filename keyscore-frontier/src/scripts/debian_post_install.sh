#!/bin/bash

# 1. Make jdk accessible
/bin/chmod -R 755 /usr/share/keyscore/keyscore-frontier/jdk8

# 2. Edit start script for the keyscore-frontier
/bin/sed -i 's_CMD="java_CMD="/usr/share/keyscore/keyscore-frontier/jdk8/bin/java_g' /usr/share/keyscore/keyscore-frontier/keyscore-frontier

# 3.1 Place the keyscore-frontier service
cp /usr/share/keyscore/keyscore-frontier/ks_frontier.service /etc/systemd/system/
# 3.2 Enable service
systemctl enable ks_frontier
# 3.3 Start service
systemctl start ks_frontier