#!/usr/bin/env python
import ir

instr_types = ['assertfail', 'binop', 'binopip', 'call', 'compare', 'const',
	'copy', 'excmatch', 'foriter', 'function', 'getattr', 'getexc', 'getitem',
	'getiter', 'getlocalsdict', 'getslice', 'global', 'import', 'jumpif',
	'jumpifnot', 'listappend', 'makeclass', 'makefunction', 'makelist',
	'makemodule', 'maketuple', 'nop', 'popexc', 'print', 'raise3', 'reraise',
	'return', 'setattr', 'setglobal', 'setitem', 'setlocal', 'setupexc',
	'unaryop', 'unpacktuple', 'useonlyglobals']

xor_with = id(5) ^ id([])

def pack_uint(num):
	assert isinstance(num, (int, long)), 'invalid value %s' % num
	assert num >= 0
	
	data = []
	while num:
		data.append(0b1111111 & num)
		num >>= 7
	return ''.join(chr(0b10000000 | d) for d in data[:-1]) + \
		chr(data[-1] if data else 0)

def pack_int(num):
	if num == 0:
		return '\0'
	
	unum = abs(num)
	sign = num // unum
	
	first = unum & 0b111111
	rest = unum >> 6
	sign_data = 0b10000000 if sign == -1 else 0
	if rest:
		return chr(first|sign_data|0b1000000) + pack_uint(rest)
	else:
		return chr(first|sign_data)

def serialize_value(value):
	if isinstance(value, int):
		return 'I' + pack_int(value)
	if isinstance(value, float):
		s = repr(value)
		return 'F' + pack_uint(len(s)) + s
	if isinstance(value, str):
		return 'S' + pack_uint(len(value)) + value
	if isinstance(value, unicode):
		encoded = value.encode('utf8')
		return 'U' + pack_uint(len(encoded)) + encoded
	if value == None:
		return 'N'
	if value == True:
		return 'B1'
	if value == False:
		return 'B0'
	if isinstance(value, dict):
		assert all( isinstance(k, str) for k in value.keys() )
		items = ''.join(
			pack_uint(len(k)) + k + serialize(v)
			for k,v in value.items()
		)
		return 'D' + pack_uint(len(value)) + items
	if isinstance(value, list):
		return 'L' + pack_uint(len(value)) + ''.join(map(serialize, value))
	raise TypeError('Unserializable type %s' % type(value))

def pack_uint_list(l):
	return ''.join(map(pack_uint, l))

def serialize_instr(ident, instr, ids):
	'''
	Columns: id, name, next1, next2, lineno, inreg, outreg, args
	'''	
	assert not any( (n is instr) for n in instr.next )
	next = [ ids.get(n, ident) for n in instr.next ] 
	if len(next) == 0: next.append(ident)
	if len(next) == 1: next.append(ident)
	
	if not instr.outreg: instr.outreg = []
	if not instr.inreg: instr.inreg = []
	
	return ''.join([
		pack_uint(instr_types.index(instr.name)), pack_uint(len(instr.args)), 
		pack_uint(len(instr.inreg)), pack_uint(len(instr.outreg)), 
		pack_int(next[0] - ident), pack_int(next[1] - ident), 
		''.join(map(serialize, instr.args)), pack_uint_list(instr.inreg), 
		pack_uint_list(instr.outreg), pack_uint(instr.lineno if instr.lineno >= 0 else 0)
	])

def serialize_instrs(main):
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
	
	for instr, id_ in sorted(instrs.items(), key=lambda (i,id_): id_):
		result.append(serialize_instr(id_, instr, instrs))
	
	result_s = ''.join( item for item in result )
	
	return 'i' + pack_int(id(main)) + '-' + pack_uint(len(instrs)) + result_s

def serialize_function(func):
	return 'f' + pack_uint(func.argcount) + pack_uint(len(func.loadargs)) + \
		pack_uint(func.varcount) + serialize(func.body)

def serialize(obj):
	if isinstance(obj, ir.Instr):
		return serialize_instrs(obj)
	elif isinstance(obj, ir.Function):
		return serialize_function(obj)
	elif isinstance(obj, ir.Label):
		return 'l'
	else:
		return serialize_value(obj)

if __name__ == '__main__':
	import ir, sys
	val = ir.execute(sys.stdin.read())
	print serialize(val)