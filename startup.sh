#!/usr/bin/env bash
workspace='result'
filename='torrentFinder'
git pull
mvn install
mkdir ${workspace} -p
cp target/${filename}.jar ${workspace}
cd ${workspace}
pid=$( ps -ef | grep ${filename} | grep jar | awk '{print $2}' )
if [ -n "$pid" ]; then
    kill -9 ${pid}
fi
nohup ./${filename}.jar &>out &