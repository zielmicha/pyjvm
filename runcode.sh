#!/bin/bash
call="python compiler/serialize.py $1"
if [ "$1" = profile ]; then
	echo profiling...
	$call | java -Xrunhprof:cpu=samples pyjvm.Main
elif [ "$1" = gcj ]; then
	echo GCJ
	$call | ./pyjvm.bin
elif [ "$1" = jar ]; then
	echo JAR
	$call | java -jar pyjvm.jar
elif [ "$1" = pro ]; then
	echo JAR, proguard
	$call | java -jar pyjvm-pro.jar
else
	$call | java pyjvm.Main
fi
