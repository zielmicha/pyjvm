import os

from pyjvm.compiler import ir
from pyjvm.compiler import serialize

def build(path, include, dest):
    Builder(path, dest).build(include)


class Builder(object):
    def __init__(self, path, dest, build_archive=False):
        self.path = path
        self.dest = dest
        self.build_archive = build_archive
        self.archive = []
        self.find_cache = {}
        self.compiled = set()
    
    def build(self, names):
        self.queue = []
        self.queue += [ ('', name) for name in names]
        
        while self.queue:
            parent, name = self.queue.pop()
            self.build_module(parent, name)
        
        if self.build_archive:
            self.write_archive()
    
    def write_archive(self):
        serializer = serialize.Serializer()
        serializer.serialize_archive_dict(self.archive)
        with open(self.dest, 'wb') as f:
            f.write(serializer.out.getvalue())
    
    def build_module(self, parent, name):
        full_name, path = self.find_module(parent, name)
        
        if not full_name:
            return
        
        if full_name in self.compiled:
            return
        
        print 'module', full_name
        
        self.compiled.add(full_name)
        
        data = open(path, 'r').read()
        main_instr = ir.execute(data)
        
        self.scan_imports(full_name, main_instr)
        
        if self.build_archive:
            self.archive.append((full_name, main_instr, path))
        else:
            dest_path = os.path.join(self.dest, full_name + '.bc')
            with open(dest_path, 'wb') as f:
                f.write(serialize.serialize(main_instr, filename=path))
    
    def scan_imports(self, module_name, main_instr):
        imports = get_imports(main_instr)
        
        for imported_name in imports:
            self.queue.append((module_name, imported_name))
    
    def find_module(self, parent, name):
        if (parent, name) in self.find_cache:
            return self.find_cache[(parent, name)]
        
        if os.sep in parent or os.sep in name:
            raise OSError('Invalid module name name=%r parent=%r' % (name, parent))
        
        parent_parts = parent.split('.')
        del parent_parts[-1] # remove module name
        
        for i in xrange(0, len(parent_parts) + 1):
            check_parent = parent_parts[0:i]
            full_name = '.'.join(check_parent + [name])
            found = self.find_module_with_full_name(full_name)
            if found:
                self.find_cache[(parent, name)] = found
                return found
        
        return None, None
    
    def find_module_with_full_name(self, name):
        def check_path(path):
            return os.path.exists(path)
        
        path_fragment = name.split('.')
        
        for find_in in self.path:
            start_with = os.path.join(find_in, *path_fragment)
            if check_path(start_with + '.py'):
                return name, os.path.abspath(start_with + '.py')
            elif check_path(start_with + os.sep + '__init__.py'):
                return name, os.path.abspath(start_with + os.sep + '__init__.py')


def get_imports(main_instr):
    imports = set()
    
    def walk(instr):
        if instr.name == 'function':
            instr.args[0].body.walk_readonly(walk)
        elif instr.name == 'import':
            imports.add(instr.args[0])
        elif instr.name == 'getimportattr':
            imports.add(instr.args[1] + '.' + instr.args[0])
    
    main_instr.walk_readonly(walk)
    return imports