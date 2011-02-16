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


import utils
from collections import namedtuple
import selectvar
import sys

basic_instr = {
	# (push, pop): [names]
	(1, 0): 'const global nested function getexc getlocalsdict useonlyglobals import', # push 1
	(0, 1): 'return jumpifnot jumpif setlocal delattr setglobal reraise assertfail', # pop 1 
	(1, 1): 'getattr getimportattr getiter foriter makemodule unaryop copy', # push1 pop1
	(0, 0): 'nop jump setupexc popexc genexpcontinue delglobal', # nothing
	(0, 2): 'setattr delitem listappend print', # pop2
	(1, 2): 'makefunction makegenexp compare binop getitem makeclass binopip excmatch', # pop2 push 1
	(0, 3): 'raise3 setitem', # pop3
	(1, 3): 'getslice', # pop3 push 1
}
ex_instr = set(['unpacktuple', 'maketuple', 'makelist', 'makedict', 'call', 'call1', 'call2', 'call3'])
jump_ex = ('jumpifnot', 'jumpif', 'setupexc', 'foriter')

basic_instr_inv = {}
for pop_push, vals in basic_instr.items():
	for val in vals.split():
		basic_instr_inv[val] = pop_push

class IRError(Exception):
	def __init__(self, msg, instr):
		Exception.__init__(self, msg)
		self.instr = instr

class Label(object):
	def __repr__(self):
		return '<Label %s>' % hex(id(self))[2:].upper()

Function = namedtuple('Function', 'argcount body loadargs varcount')

class Instr(utils.Struct):
	_fields = 'lineno name args inreg outreg stack _stackafter next1 next2 varcount'
	_defaults = dict(
		inreg=None,
		outreg=None,
		stack=None,
		_stackafter=None,
		next1=None,
		next2=None,
		varcount=None
	)
	
	_ignore_in_repr = 'next1 next2 inreg outreg stack _stackafter varcount'
	
	def _init_struct(self):
		if not isinstance(self.name, str):
			raise TypeError('Unexpected value for name: %s' % self.name)
	
	def next(self):
		if self.name == 'jump':
			val = [self.next2]
		elif self.name in jump_ex:
			val = [self.next1, self.next2]
		elif self.name == 'return':
			val = []
		else:
			val = [self.next1]
		
		#if any([ n is None for n in val ]):
		#	raise IRError('Some targets of Instr are None (self=%r, next=%r)' % (self, val))
		
		return val
	
	def set_next(self, val):
		if self.name == 'jump':
			[self.next2] = val
		elif self.name in jump_ex:
			[self.next1, self.next2] = val
		elif self.name == 'return':
			[] = val
		else:
			[self.next1] = val

	next = property(next, set_next)

	def _sibiligs(self, sibiligs):
		sibiligs.add(self)
		
		for next in self.next:
			if next not in sibiligs and next:
				next._sibiligs(sibiligs)
	
	def sibiligs(self):
		sibiligs = set()
		self._sibiligs(sibiligs)
		return frozenset(sibiligs)
	
	def walk(self, func):
		to_be_processed = [self]
		processed = {}
		
		while to_be_processed:
			item = to_be_processed.pop()
			
			if item in processed:
				continue
			
			result = func(item)
			
			if result is item:
				raise RuntimeError('Internal: Function did not modify instruction.')
			
			processed[item] = result
			to_be_processed += result.next
		
		for item, result in processed.items():
			result.next = [ processed[code] if code else None for code in result.next ]
		
		return processed[self]

	def walk_readonly(self, func):
		to_be_processed = [self]
		processed = set()
		
		while to_be_processed:
			item = to_be_processed.pop()
			
			if item in processed:
				continue
			
			func(item)
			
			processed.add(item)
			to_be_processed += item.next
	
	def is_nop(self):
		if self.name in ('nop', 'jump', 'local'):
			return True
		else:
			return False

	def process(self, stack):
		if self.stack is not None and stack != self.stack:
			raise IRError('Instr %r (next=%r) can be reached with two stacks: %r and %r' %
				(self, self.next, self.stack, stack), self)
		if self.stack is None:
			self.stack = stack
			self._stackafter = self.calc_stack(stack)
		return self._stackafter
	
	def process_recursive(self, stack):
		# recursive version is simpler, but causes stack overflows
		to_be_processed = [(self, stack)]
		processed = []
		
		def add(item, istack):
			if item.stack == istack:
				return
			to_be_processed.append((item, istack))
		
		while to_be_processed:
			self, stack = to_be_processed.pop()
			newstack = self.process(stack)
			for next in self.next:
				if next:
					add(next, newstack)
			processed.append(self)
			
		for self in reversed(processed):
			self.elimanate_nops()

	def calc_stack(self, before):
		stack = list(before)
		if self.name in basic_instr_inv or self.name in ex_instr:
			if self.name in ex_instr:
				push, pop = getattr(self, 'cmd_getstackops_' + self.name)(*self.args)
			else:
				push, pop = basic_instr_inv[self.name]
			try:
				self.inreg = list(reversed(utils.pop_many(stack, pop)))
			except IndexError:
				raise IRError('Pop from empty stack at %s' % self, self)
			max_reg = max(stack + [self.varcount - 1])
			self.outreg = list(xrange(max_reg + 1, max_reg + 1 + push))
			stack.extend(self.outreg)
		else:
			getattr(self, 'cmd_run_' + self.name)(stack)
			self.name = 'nop' # not pretty
		return tuple(stack)
	
	def cmd_run_dup(self, stack):
		stack.append(stack[-1])
	
	def cmd_run_swap2(self, stack):
		stack[-1], stack[-2] = stack[-2], stack[-1]
	
	def cmd_run_swapn(self, stack):
		n, = self.args
		list = utils.pop_many(stack, n)
		stack.extend(list)
	
	def cmd_run_local(self, stack):
		vari, = self.args
		stack.append(vari)
	
	def cmd_run_pop(self, stack):
		stack.pop()

	def cmd_getstackops_unpacktuple(self, length):
		return length, 1
	
	def cmd_getstackops_maketuple(self, length):
		return 1, length
	
	def cmd_getstackops_makelist(self, length):
		return 1, length
	
	def cmd_getstackops_makedict(self, length):
		return 1, length * 2

	def cmd_getstackops_call(self, length, kwargs=[]):
		return 1, length + 1
	
	def cmd_getstackops_call1(self, length, kwargs=[]):
		return 1, length + 2
	cmd_getstackops_call2 = cmd_getstackops_call1
	
	def cmd_getstackops_call3(self, length, kwargs=[]):
		return 1, length + 2

	def elimanate_nops(self): 
		''' Eliminate nop, jump '''
		
		if self.next1 and self.next1.is_nop():
			self.next1, = self.next1.next
		if self.next2 and self.next2.is_nop():
			self.next2, = self.next2.next
	
	def eliminate_setlocal(self):
		isconditional = len(self.next) != 1
		ismaybejump = self.next and self.next[0] != self.next1
		issingleregister = self.outreg and len(self.outreg) == 1
		
		if isconditional or ismaybejump or not issingleregister:
			return
		
		if not self.next1: return
		
		if self.next1.name == 'setlocal':
			input_of_setlocal, = self.next1.inreg
			output_of_self, = self.outreg
			if input_of_setlocal != output_of_self:
				return
			locali, = self.next1.args
			self.outreg = [locali]
			self.next1, = self.next1.next
	
	def dump(self, dump_functions=True, _idents=None, _indent=0, mark=None):
		def myrepr(obj):
			if isinstance(obj, dict):
				return 'dict(%s)' % ', '.join( '%s=%r' % (k,v) for k, v in obj.items())
			else:
				return repr(obj)
		
		def dump(instr, _my_indent):
			if not instr:
				pr((_my_indent - _indent) * '\t' + '{end}')
			else:
				instr.dump(dump_functions, _idents, _my_indent, mark=mark)
		
		def pr(text, args=()):
			text = text.replace('[','\033[01;37m').replace(']', '\033[00m') # white
			text = text.replace('{','\033[01;36m').replace('}', '\033[00m') # green?
			text = text.replace('<','\033[01;31m').replace('>', '\033[00m') # red
			print >>sys.stderr, '\t' * _indent, text % args
		
		if _idents is None:
			_idents = {}
		
		if self in _idents:
			pr('<deref %d>', _idents[self])
		else:
			nextident = max(_idents.values() or [0]) + 1
			_idents[self] = nextident
			
			mark_s = '  \033[01;36m<<< error here\033[00m' if mark is self else ''
			
			if self.name == 'function':
				if dump_functions:
					func, = self.args
					pr('{function} [argcount=%d loadargs=%r] inreg=%s outreg=%s<id:%d>', (func.argcount, func.loadargs, self.inreg, self.outreg, nextident))
					dump(func.body, _indent + 1)
				else:
					pr('{function} [...] inreg=%s outreg=%s <id:%d>', (self.inreg, self.outreg, nextident))
			else:
				args = ', '.join([ myrepr(arg) for arg in self.args if not isinstance(arg, Label)])
				pr('{%s} [%s] inreg=%s outreg=%s %r %r <id:%d>%s', (
					self.name, args, self.inreg, self.outreg, self.stack, self._stackafter, nextident, mark_s))
			
			nexts = self.next
			if len(nexts) == 1:
				dump(nexts[0], _indent)
			if len(nexts) == 2:
				dump(nexts[1], _indent+1)
				pr('{else}')
				dump(nexts[0], _indent+1)
		

def cmdlist_to_instr(cmds, nested=None, argcount=None):
	'''
	if argscount == None: transforms cmdlist to Instr object
	else: transforms cmdlist to Function object
	'''
	if nested is None:
		nested = {}
	
	labels = {}
	def create_instr(i, cmd):
		if isinstance(cmd[1], Label):
			labels[cmd[1]] = i
			return Instr(name='nop', lineno=-1, args=())
		else:
			return Instr(name=cmd[1], lineno=cmd[0], args=cmd[2:])
	
	instrs = [
		create_instr(i, cmd)
		for i, cmd in enumerate(cmds)
	]
	
	for i, instr in enumerate(instrs):
		args = instr.args
		if args and isinstance(args[0], Label):
			label = args[0]
			if label not in labels:
				raise KeyError('Internal: Invalid label (%r not in %r)' % (label, labels.keys()))
			instr.next2 = instrs[labels[label]]
		try:
			instr.next1 = instrs[i + 1]
		except IndexError:
			instr.next1 = None
	
	for instr in instrs:
		instr.elimanate_nops()
	
	main_instr = selectvar.selectvar(nested, instrs[0])
	
	if argcount is not None:
		load_args = []
		
		for argi in xrange(argcount):
			if main_instr.name == 'setlocal':
				load_args.append(main_instr.args[0])
				main_instr, = main_instr.next
		
		if not main_instr:
			main_instr = Instr(name='nop', lineno=-1, args=(), varcount=0)
	
	startstack = tuple(range(argcount or 0))
	try:
		main_instr.process_recursive(stack=startstack)
	except IRError as err:
		print 'IR error, start=line', main_instr.lineno
		main_instr.dump(mark=err.instr)
		raise
	
	for instr in main_instr.sibiligs():
		instr.eliminate_setlocal()
	
	while main_instr.is_nop() and len(main_instr.next) == 1 and main_instr.next[0]:
		main_instr, = main_instr.next
	
	if argcount is not None:
		while len(load_args) < argcount:
			load_args.append(main_instr.varcount + len(load_args))
	
	if argcount is not None:
		return Function(argcount=argcount, body=main_instr, varcount=main_instr.varcount, loadargs=load_args)
	else:
		return main_instr


def execute(code):
	import transformer
	v = transformer.execute(code)
	instr = cmdlist_to_instr(v.cmds)
	return instr

def executeOnFile(name):
	return execute(open(name).read())

if __name__ == '__main__':
	import sys
	print 'If you use ir.py as __main__, ir.Label != __main__.Label, you want that?'
	sys.exit(1)
	####
	#### execute(sys.stdin.read()).dump()