# Copyright (C) 2011 by Michal Zielinski
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.


import ir
import StringIO

instr_types = ['assertfail', 'binop', 'binopip', 'call', 'compare', 'const',
	'copy', 'excmatch', 'foriter', 'function', 'getattr', 'getexc', 'getitem',
	'getiter', 'getlocalsdict', 'getslice', 'global', 'import', 'jumpif',
	'jumpifnot', 'listappend', 'makeclass', 'makefunction', 'makelist',
	'makemodule', 'maketuple', 'nop', 'popexc', 'print', 'raise3', 'reraise',
	'return', 'setattr', 'setglobal', 'setitem', 'setlocal', 'setupexc',
	'unaryop', 'unpacktuple', 'useonlyglobals',
	'getimportattr', 'delattr', 'delglobal', 'makedict']

instr_map = dict( (name, i) for i, name in enumerate(instr_types) )


class Serializer(object):
	def __init__(self):
		self.uid = 0
		self.out = StringIO.StringIO()

	def increment_uid(self):
		self.uid += 1
		return self.uid
	
	def	write(self, ch):
		self.out.write(ch)

	def pack_uint(self, num):
		assert isinstance(num, (int, long)), 'invalid value %s' % num
		assert num >= 0
		
		data = []
		
		while num:
			data.append(0b1111111 & num)
			num >>= 7
		
		for d in data[:-1]:
			self.out.write(chr(0b10000000 | d))
		
		self.out.write(chr(data[-1] if data else 0))
	
	def pack_int(self, num):
		if num == 0:
			self.write('\0')
			return
		
		unum = abs(num)
		sign = num // unum
		
		first = unum & 0b111111
		rest = unum >> 6
		sign_data = 0b10000000 if sign == -1 else 0
		if rest:
			self.out.write(chr(first|sign_data|0b1000000))
			self.pack_uint(rest)
		else:
			self.out.write(chr(first|sign_data))
	
	def serialize_value(self, value):
		if isinstance(value, int):
			self.write('I')
			self.pack_int(value)
		elif isinstance(value, float):
			s = repr(value)
			self.write('F')
			self.pack_uint(len(s))
			self.out.write(s)
		elif isinstance(value, str):
			self.write('S')
			self.pack_uint(len(value))
			self.write(value)
		elif isinstance(value, unicode):
			encoded = value.encode('utf8')
			self.write('U')
			self.pack_uint(len(encoded))
			self.write(encoded)
		elif value == None:
			self.write('N')
		elif value == True:
			self.write('B1')
		elif value == False:
			self.write('B0')
		elif isinstance(value, dict):
			assert all( isinstance(k, str) for k in value.keys() )
			
			self.write('D')
			self.pack_uint(len(value))
			
			for k,v in value.items():
				self.pack_uint(len(k))
				self.write(k)
				self.serialize(v)
		elif isinstance(value, list):
			self.write('L')
			self.pack_uint(len(value))
			for val in value:
				self.serialize(val)
		else:
			raise TypeError('Unserializable type %s' % type(value))
	
	def pack_uint_list(self, l):
		for v in l:
			self.pack_uint(v)
	
	def serialize_instr(self, ident, instr, ids):
		'''
		Columns: id, name, next1, next2, lineno, inreg, outreg, args
		'''
		assert not any( (n is instr) for n in instr.next )
		next = [ ids.get(n, ident) for n in instr.next ] 
		if len(next) == 0: next.append(ident)
		if len(next) == 1: next.append(ident)
		
		if not instr.outreg: instr.outreg = []
		if not instr.inreg: instr.inreg = []
		
		self.pack_uint(instr_map[instr.name])
		self.pack_uint(len(instr.args))
		self.pack_uint(len(instr.inreg))
		self.pack_uint(len(instr.outreg))
		self.pack_int(next[0] - ident)
		self.pack_int(next[1] - ident)
		
		for arg in instr.args:
			self.serialize(arg)
		self.pack_uint_list(instr.inreg)
		self.pack_uint_list(instr.outreg)
		self.pack_uint(instr.lineno if instr.lineno >= 0 else 0)
	
	def serialize_instrs(self, main):
		instrs = {}
		ident = 0
		to_be_processed = [main]
		
		while to_be_processed:
			instr = to_be_processed.pop()
			if not instr:
				continue
			if instr in instrs:
				continue
			instrs[instr] = ident
			ident += 1
			to_be_processed.extend(instr.next)
		
		result = []
		
		self.write('i')
		self.pack_uint(self.increment_uid())
		self.write('-')
		self.pack_uint(len(instrs))
		
		for instr, id_ in sorted(instrs.items(), key=lambda (i,id_): id_):
			self.serialize_instr(id_, instr, instrs)
		
	def serialize_function(self, func):
		self.write('f')
		self.pack_uint(func.argcount)
		self.pack_uint(len(func.loadargs))
		self.pack_uint_list(func.loadargs)
		self.pack_uint(func.varcount)
		self.serialize(func.body)
	
	def serialize(self, obj, filename=None):
		if filename:
			self.write('m')
			self.serialize(filename)
			self.serialize(obj)
		elif isinstance(obj, ir.Instr):
			self.serialize_instrs(obj)
		elif isinstance(obj, ir.Function):
			self.serialize_function(obj)
		elif isinstance(obj, ir.Label):
			self.write('l')
		else:
			self.serialize_value(obj)
	
	def serialize_archive_dict(self, archive):
		self.write('D')
		self.pack_uint(len(archive))
		
		for name, code, fn in archive:
			self.pack_uint(len(name))
			self.write(name)
			self.serialize(code, filename=fn)

def serialize(obj, filename=None):
	serializer = Serializer()
	serializer.serialize(obj, filename)
	return serializer.out.getvalue()

def _serialize_uint(u):
	serializer = Serializer()
	serializer.pack_uint(u)
	return serializer.out.getvalue()

def test(obj):
	import os
	data = serialize(obj)
	w = os.popen('cd ..; java pyjvm.Main', 'w')
	w.write(data)
	w.close()

if __name__ == '__main__':
	import ir, sys
	val = ir.execute(sys.stdin.read())
	fn = sys.argv[1] if sys.argv[1:] else ''
	sys.stdout.write(serialize(val, filename=fn))