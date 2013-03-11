import os
import yaml
import shutil
import subprocess
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
        self.actions = {}

    def build(self):
        self.load()

        mkdir_quiet(join(self.path, 'build'))
        mkdir_quiet(join(self.path, 'build', 'classes'))
        mkdir_quiet(join(self.path, 'build', 'bytecode'))

        build_java.build(self.java_path, join(self.path, 'build', 'classes'))
        build_py.build(self.python_path, self.include_modules, join(self.path, 'build', 'bytecode'))

    def create_jar(self):
        if os.path.exists(join(self.path, 'build', 'jar')):
            shutil.rmtree(join(self.path, 'build', 'jar'))

        shutil.copytree(join(self.path, 'build', 'classes'), join(self.path, 'build', 'jar'))
        shutil.copytree(join(self.path, 'build', 'bytecode'), join(self.path, 'build', 'jar', 'bc'))
        os.remove(join(self.path, 'build', 'jar', 'sources.list'))

        with open(join(self.path, 'build', 'jar', 'bc', 'mf'), 'w') as f:
            f.write(self.main_module + '\n')

        mkdir_quiet(join(self.path, 'build', 'jar', 'META-INF'))

        with open(join(self.path, 'build', 'jar', 'META-INF', 'MANIFEST.MF'), 'w') as f:
            f.write('Manifest-Version: 1.0\n')
            f.write('Main-Class: pyjvm.JarMain\n')
            f.write('\n')

        subprocess.check_call(('jar', 'cfm',
                               join(self.path, 'build', 'build.jar'),
                               join(self.path, 'build', 'jar', 'META-INF', 'MANIFEST.MF'),
                               '-C', join(self.path, 'build', 'jar'),
                                '.'))

    def action(self, name):
        def decorator(func):
            self.actions[name] = func
            return func
        return decorator

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

        script = conf.get_build_script()
        if script:
            self.execute_build_script(script)

    def execute_build_script(self, path):
        execfile(path, {'__file__': path, '__name__': '__build__', 'project': self})

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

    def create(self):
        if os.path.exists(join(self.path, 'pyjvm.yaml')):
            print 'project aleardy exists'
            return

        mkdir_quiet(self.path)

        with open(join(self.path, 'pyjvm.yaml'), 'w') as f:
            f.write('depend: [$pyjvm]\npath: [.]\ninclude: [os, main]\nmain: main')

        if not os.path.exists(join(self.path, 'main.py')):
            with open(join(self.path, 'main.py'), 'w') as f:
                f.write('print \'Hello world!\'\n')


class ProjectConf(object):
    def __init__(self, path):
        self.path = path
        self.config = yaml.load(open(join(path, 'pyjvm.yaml')))

    def get_deps(self):
        l = []
        for name in self.config.get('depend', []):
            if name == '$pyjvm':
                l.append(pyjvm_project_path)
            elif name.startswith('$pyjvm/'):
                l.append(join(pyjvm_project_path, name.split('/', 1)[1]))
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

    def get_build_script(self):
        if 'build-script' in self.config:
            return os.path.abspath(join(self.path, self.config['build-script']))

def mkdir_quiet(path):
    try:
        os.mkdir(path)
    except OSError:
        pass

