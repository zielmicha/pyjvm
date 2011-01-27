#!/bin/bash
if [ "$1" = profile ]; then
	echo profiling...
	python compiler/serialize.py | java -Xrunhprof:cpu=samples pyjvm.Main
elif [ "$1" = gcj ]; then
	echo GCJ
	python compiler/serialize.py | ./pyjvm.bin
elif [ "$1" = jar ]; then
	echo JAR
	python compiler/serialize.py | java -jar pyjvm.jar
elif [ "$1" = pro ]; then
	echo JAR, proguard
	python compiler/serialize.py | java -jar pyjvm-pro.jar
else
	python compiler/serialize.py | java pyjvm.Main
fi
