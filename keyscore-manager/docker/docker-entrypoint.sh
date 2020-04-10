#!/bin/sh

if [ ! -z "$KEYSCORE_BASE_URL" ]; then
    /bin/sed -e "s#\"base-url\".*#\"base-url\": \"$KEYSCORE_BASE_URL\"#g" -i /usr/share/nginx/html/conf/application.conf
fi


exec /usr/sbin/nginx -g 'daemon off;'
