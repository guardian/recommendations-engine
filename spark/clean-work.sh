#!/bin/bash

# crontab: */5 * * * * /root/clean-work.sh

find /root/spark/work/ -maxdepth 1 -mindepth 1 -type d -cmin +120 -exec rm -rf {} \;