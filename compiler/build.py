import ir
import os
import serialize

class Builder(object):
	def __init__(self, path):
		self.path = path
		self.find_cache = {}
		self.imported = set()
		self.destdir = '.'
	
	def build(self, script):
		self.compile(script, '__main__', '__main__', allow_caching=False)
	
	def compile(self, path, module_name, output=None, allow_caching=True):
		
		if output == None:
			output = module_name
		
		output_path = os.path.join(self.destdir, output + '.bc')
		
		print 'M', module_name
		
		if allow_caching:
			try:
				output_modified = os.path.getmtime(output_path)
				input_modified = os.path.getmtime(path)
				
				if input_modified <= output_modified:
					return
			except (OSError, IOError):
				pass
		
		data = open(path, 'r').read()
		compiled = ir.execute(data)
		
		self.scan_imports(module_name, compiled)
		
		serialized = serialize.serialize(compiled, filename=path)
		
		output = open(output_path, 'w')
		output.write(serialized)
		output.close()
	
	def get_imports(self, main_instr):
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
	
	def scan_imports(self, module_name, main_instr):
		imports = self.get_imports(main_instr)
		
		for imported_name in imports:
			try:
				self.compile_module(module_name, imported_name)
			except ImportError:
				pass
	
	def compile_module(self, parent, name):
		full_name, path = self.find_module(parent, name)
		if full_name in self.imported:
			return

		self.imported.add(full_name)
		
		self.compile(path, full_name)
	
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
		
		raise ImportError('Module %s not found (parent=%s)' % (name, parent))
	
	def find_module_with_full_name(self, name):
		def check_path(path):
			return os.path.exists(path)
		
		path_fragment = name.split('.')
		
		for find_in in self.path:
			start_with = os.path.join(find_in, *path_fragment)
			if check_path(start_with + '.py'):
				return name, start_with + '.py'
			elif check_path(start_with + os.sep + '__init__.py'):
				return name, start_with + os.sep + '__init__.py'

def main():
	import optparse

	parser = optparse.OptionParser()
	
	parser.add_option('-i', '--mainfile', dest='mainfile', type=str,
						help='script file')
	parser.add_option('-d', '--destdir', dest='destdir', type=str,
						help='destination directory', metavar='DESTDIR')
	parser.add_option('--path', metavar='PATH', dest='path', default='.',
					  help='path')
	
	options, args = parser.parse_args()
	
	if len(args) != 0:
		parser.error("incorrect number of arguments")
	
	builder = Builder([options.path])
	builder.destdir = options.destdir
	builder.build(options.mainfile)

if __name__ == '__main__':
	main()