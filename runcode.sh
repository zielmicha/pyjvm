#!/bin/bash
if [ $# = 0 ]; then
	input=$(mktemp ./tmp.XXXXXXXXXX)
	istmp=1
	cat > $input
else
	istmp=0
	input="$1"
fi

if [ "$2" = profile ]; then
	echo profiling...
	cmd="java -Xrunhprof:cpu=samples pyjvm.Main"
elif [ "$2" = gcj ]; then
	echo GCJ
	cmd="./pyjvm.bin"
elif [ "$2" = jar ]; then
	echo JAR
	cmd="java -jar pyjvm.jar"
elif [ "$2" = pro ]; then
	cmd="java -jar pyjvm-pro.jar"
else
	cmd="java pyjvm.Main"
fi
mkdir -p dest
python compiler/build.py -i "$input" -d dest
$cmd dest < dest/__main__.bc

if [ $istmp = 1 ]; then
	rm $input
fi

exit
 
