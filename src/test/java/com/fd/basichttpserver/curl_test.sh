#!/bin/bash
url='http://localhost:8081'
i=0
while [ $i -lt 100000 ];do
	curl $url
	i=`expr $i + 1`
done
