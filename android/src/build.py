# build-script
import pyjvm
import pyjvm.build
import pyjvm.build_java
import pyjvm.build_py
from pyjvm.build_java import JavacError

import shutil
import os
import sys
import yaml
import subprocess

from os.path import join

pyjvm.assert_build_script(__name__)
pyjvm.add_dir_to_path(__file__)

@project.action('apk-debug')
def apk_debug(args):
    'build debug APK'
    if args:
        sys.exit('apk_debug not expecting any arguments')
    
    init()
    
    pyjvm.build.mkdir_quiet(join(project.path, 'build'))
    
    create_android_project()
    build_python()
    create_source()
    build_dex()
    build_apk()
    
def assert_exists(path):
    if not os.path.exists(path):
        sys.exit('File %s does not exists - update your Android SDK and install android-4 platform' % path) 

def init():
    global sdk_dir, config
    sdk_dir = os.path.abspath(find_sdk())
    assert_exists(join(sdk_dir, 'platforms', 'android-4', 'android.jar'))
    config = yaml.load(open(join(project.path, 'android.yaml')))

def build_python():
    pyjvm.build.mkdir_quiet(join(project.path, 'build', 'android', 'assets'))
    
    pyjvm.build_py.Builder(project.python_path, join(project.path, 'build', 'android', 'assets', 'code.dat'), build_archive=True).build(project.include_modules)

def build_dex():
    # javac -encoding ascii -target 1.5 -d bin/classes -bootclasspath $SDKDIR/android.jar src/some/project/*
    #pyjvm.build_java.
    
    input = join(project.path, 'build', 'android-classes')
    output = join(project.path, 'build', 'classes.dex')
    
    java_path = (project.java_path) + [join(project.path, 'build', 'android', 'src')]
    
    pyjvm.build.mkdir_quiet(input)
    pyjvm.build_java.build(java_path, input, use_javac=android_javac)
    
    cmd = (join(sdk_dir, 'platform-tools', 'dx'), '--dex', '--output=' + output, input)
    
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    
    if status != 0:
        raise JavacError

def android_javac(files, sourcepath, classes):
    if not files:
        print 'not compiling Java files'
        return
    sources_path = os.path.join(classes, 'sources.list')
    with open(sources_path, 'w') as f:
        for file in files:
            print >>f, file
    print 'compiling %d Java files...' % len(files)
    srcpath = ':'.join( entry for entry in sourcepath )
    cmd = ('javac', '-Xlint:none', '-sourcepath', srcpath, '-d', classes, '@' + sources_path,
           '-encoding', 'ascii', '-target', '1.5', '-bootclasspath', join(sdk_dir, 'platforms', 'android-4', 'android.jar'))
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    
    if status != 0:
        raise JavacError

def build_apk():
    build_apk_res()
    build_apk_builder()
    build_apk_with_builder()

def build_apk_res():
    andr_path = join(project.path, 'build', 'android')
    
    cmd = (join(sdk_dir, 'platform-tools', 'aapt'), 'p', '-f',
            '-M', join(andr_path, 'AndroidManifest.xml'),
            '-S', join(andr_path, 'res'),
            '-A', join(andr_path, 'assets'),
            '-I', join(sdk_dir, 'platforms', 'android-4', 'android.jar'),
            '-F', join(project.path, 'build', 'output.ap_'))
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    
    if status != 0:
        raise JavacError

def build_apk_builder():
    input = join(os.path.dirname(__file__), 'ApkBuilderCmd.java')
    output = join(project.path, 'build')
    sdk_jar = join(sdk_dir, 'tools', 'lib', 'sdklib.jar')
    
    cmd = ('javac', '-Xlint:none', '-cp', sdk_jar, '-d', output, input)
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    
    if status != 0:
        raise JavacError

def build_apk_with_builder():
    build_dir = join(project.path, 'build')
    invoke_apk_builder(join(build_dir, 'package.apk'),
                       join(build_dir, 'output.ap_'),
                       join(build_dir, 'classes.dex'),
                       join(os.path.expanduser('~'), '.android', 'debug.keystore'))

def invoke_apk_builder(output, resources, classes, keystore):
    sdk_jar = join(sdk_dir, 'tools', 'lib', 'sdklib.jar')
    classpath = sdk_jar + os.pathsep + join(project.path, 'build')
    
    cmd = ('java', '-cp', classpath, 'ApkBuilderCmd', output, resources, classes, keystore)
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    
    if status != 0:
        raise JavacError

def find_sdk():
    def is_sdk(path):
        return os.path.exists(join(path, 'tools', 'android'))
    
    if os.environ.get('ANDROID_SDK'):
        path = os.environ.get('ANDROID_SDK')
        if not is_sdk(path):
            sys.exit('ANDROID_SDK env varible points to a folder that is not an Android SDK (%r)' % path)
        return path
    
    path = os.environ['PATH'].split(os.pathsep)
    
    for folder in path:
        if is_sdk(folder):
            return folder
        elif is_sdk(join(folder, '..')):
            return join(folder, '..')
    
    sys.exit('Android SDK not found. Set env varible ANDROID_SDK to Android SDK directory.')

def create_android_project():
    path = join(project.path, 'build', 'android')
    if os.path.exists(path):
        shutil.rmtree(path)
    os.mkdir(path)
    
    with open(join(path, 'project.properties'), 'w') as f:
        f.write('target=android-4\n')
    
    with open(join(path, 'local.properties'), 'w') as f:
        f.write('sdk.dir=%s\n' % sdk_dir)
    
    with open(join(path, 'AndroidManifest.xml'), 'w') as f:
        write_manifest(f)
    
    os.makedirs(join(path, 'res', 'values'))
    
    with open(join(path, 'res', 'values', 'strings.xml'), 'w') as f:
        f.write('''<?xml version="1.0" encoding="utf-8"?>
    <resources>
        <string name="app_name">%s</string>
    </resources>''' % config['name'])
    

def write_manifest(f):
    activity_name = config['activity_name']
    vercode = config.get('vercode', '900')
    version = config.get('version', '0.9.0')
    package = config['package']
    
    f.write('''<?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      package="%(package)s"
      android:versionCode="%(vercode)s"
      android:versionName="%(version)s">
        <application android:label="@string/app_name">
            <activity android:name="%(activity_name)s"
                      android:label="@string/app_name"
                      android:process=":pyjvm">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
        </application>
    </manifest>''' % locals()) 


def create_source():
    path = join(project.path, 'build', 'android')
    
    package = config['package']
    actname = config['activity_name']
    pkg_dir = join(path, 'src', *package.split('/'))
    os.makedirs(pkg_dir)
    
    with open(join(pkg_dir, actname + '.java'), 'w') as f:
        f.write('''
package %(package)s;

import android.app.Activity;
import android.os.Bundle;

public class %(actname)s extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        try {
            pyjvm.Importer.loadArchive(getAssets().open("code.dat"));
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        pyjvm.Importer.importModule("_android_main");
    }
}
''' % locals())
    