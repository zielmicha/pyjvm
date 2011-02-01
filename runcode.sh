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
amarok                           
automoc                          
k3b                              
kdebase-workspace-bin            
kdelibs5-dev                     
kdepim-runtime-libs4             
kdepimlibs-data                  
kdepimlibs5                      
ksysguard                        
libkrb5-dev                      
libkwineffects1                  
libpq-dev                        
libqt4-dev                       
libqt4-opengl-dev                
libqt4-phonon                    
libqt4-phonon-dev                
okular                           
plasma-dataengines-workspace     
plasma-widget-adjustableclock    
plasma-widget-bkodama            
plasma-widget-lancelot           
plasma-widget-logout             
plasma-widget-plasmaboard        
plasma-widget-stasks             
plasma-widget-windowlist         
plasma-widget-windowslist        
plasma-widgets-addons            
plasma-widgets-workspace

kde-window-manager               
kdebase-runtime-data-common      
kdepim-runtime-libs4             
kdepimlibs-data                  
kdepimlibs5                      
kmix                             
ksysguard                        
libqt4-phonon                    
okular                           
plasma-widget-daisy              
plasma-widget-fancytasks         
plasma-widget-ktorrent           
plasma-widget-lancelot           
plasma-widget-logout             
plasma-widget-network-manager    
plasma-widget-networkmanagement  
plasma-widget-networkmanagement-dbg
plasma-widget-plasmaboard          
plasma-widget-stasks               
plasma-widget-toggle-compositing   
plasma-widget-windowlist           
plasma-widget-windowslist          
plasma-widget-xbar

libdbusmenu-glib0                
libdbusmenu-gtk0                 
libept0                          
libgoffice-0-8                   
libmagickwand2                   
liboobs-1-4                      
python2.5                        
python2.5-minimal                
wine-bin 
