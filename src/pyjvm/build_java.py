import os
import subprocess

class JavacError(Exception): pass

def build(sources, dest):
    all_files = [  ]
    for source in sources:
        java_files = find_ext(source, '.java')
        for java_path in java_files:
            class_path = os.path.join(dest, os.path.relpath(java_path, source))
            assert class_path.endswith('.java'), class_path
            class_path = class_path[:-5] + '.class'
            if is_source_earlier(java_path, class_path):
                all_files.append(java_path)
    javac(all_files, sources, dest)
    

def is_source_earlier(src, dest):
    try:
        src_time = os.path.getmtime(src)
        dst_time = os.path.getmtime(dest)
    except OSError:
        return True
    return src_time > dst_time

def javac(files, sourcepath, classes):
    if not files:
        print 'not compiling Java files'
        return
    sources_path = os.path.join(classes, 'sources.list')
    with open(sources_path, 'w') as f:
        for file in files:
            print >>f, file
    print 'compiling %d Java files...' % len(files)
    srcpath = ':'.join( entry for entry in sourcepath )
    cmd = ('javac', '-Xlint:none', '-sourcepath', srcpath, '-d', classes, '@' + sources_path)
    print ' '.join(cmd)
    status = subprocess.call(cmd)
    if status != 0:
        raise JavacError

def find_ext(path, endswith):
    q = [path]
    result = []
    while q:
        path = q.pop()
        if os.path.isdir(path):
            q += [ os.path.join(path, name) for name in os.listdir(path) ]
        else:
            if path.endswith(endswith):
                result.append(path)
    return result