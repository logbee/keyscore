#!/bin/bash

SYSTEMD="/usr/lib/systemd/"
if [ -d $SYSTEMD ]
  then
  # 1.1 Disable service
  systemctl disable ks_frontier
  # 1.2 Stop service
  systemctl stop ks_frontier
  # 1.3 Remove service
  rm /etc/systemd/system/ks_frontier.service
fi