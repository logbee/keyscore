#!/bin/sh

if [ ! -z "$BASE_URL" ]; then
    /bin/sed -e "s#\"base-url\".*#\"base-url\": \"$BASE_URL\"#g" -i /usr/share/nginx/html/application.conf
fi

exec /usr/sbin/nginx -g 'daemon off;'
