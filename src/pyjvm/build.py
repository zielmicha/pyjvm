import os
import yaml
import shutil
import subprocess

from os.path import join

from pyjvm import build_java
from pyjvm import build_py

pyjvm_project_path = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))

class Project(object):
    def __init__(self, path='.'):
        self.path = os.path.abspath(path)
        self._loaded = False
        
        self.python_path = []
        self.java_path = []
        self.include_modules = []
    
    def build(self):
        self.load()
        
        mkdir_quiet(join(self.path, 'build'))
        mkdir_quiet(join(self.path, 'build', 'classes'))
        mkdir_quiet(join(self.path, 'build', 'bytecode'))
        
        build_java.build(self.java_path, join(self.path, 'build', 'classes'))
        build_py.build(self.python_path, self.include_modules, join(self.path, 'build', 'bytecode'))
    
    def load(self):
        if self._loaded:
            return
        self._loaded = True
        self.main_module = ProjectConf(self.path).get_main_module()
        
        self.load_dep()
        
        #print 'python path:', self.python_path
        #print 'java path:', self.java_path
        #print 'include:', self.include_modules
    
    def load_dep(self):
        q = [os.path.abspath(self.path)]
        loaded = set(self.path)
        
        while q: # BFS
            path = q.pop()
            if path not in loaded:
                loaded.add(path)
                conf = ProjectConf(path)
                self.load_conf(conf)
                for dep in conf.get_deps():
                    q.append(dep)
        
    def load_conf(self, conf):
        self.include_modules += conf.get_include_modules()
        self.python_path += conf.get_python_path()
        self.java_path += conf.get_java_path()
    
    def execute(self, module):
        last_cwd = os.getcwd()
        os.chdir(self.path)
        args = ('java', '-cp', join(self.path, 'build', 'classes'), 'pyjvm.Main', join(self.path, 'build', 'bytecode'), module)
        subprocess.call(args)
        os.chdir(last_cwd)
    
    def clean(self):
        def on_error(func, path, info):
            print 'failed to', func.__name__, path
        
        if not os.path.exists(join(self.path, 'build')):
            return
        
        shutil.rmtree(join(self.path, 'build'), onerror=on_error)
    

class ProjectConf(object):
    def __init__(self, path):
        self.path = path
        self.config = yaml.load(open(join(path, 'pyjvm.yaml')))
    
    def get_deps(self):
        l = []
        for name in self.config.get('depend', []):
            if name == '$pyjvm':
                l.append(pyjvm_project_path)
            else:
                l.append(os.path.abspath(join(self.path, name)))
        return l
    
    def get_include_modules(self):
        return self.config.get('include', [])
    
    def get_python_path(self):
        return [ os.path.abspath(join(self.path, name)) for name in self.config.get('path', []) ]
    
    def get_java_path(self):
        return [ os.path.abspath(join(self.path, name)) for name in self.config.get('java', []) ]
    
    def get_main_module(self):
        return self.config.get('main', 'main')

def mkdir_quiet(path):
    try:
        os.mkdir(path)
    except OSError:
        pass

