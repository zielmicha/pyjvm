#!/usr/bin/env python
'''
In: python source
Out: stack based IR
'''

import compiler
import sys
from compiler import ast
from compiler.consts import OP_DELETE, OP_ASSIGN
import collections

from ir import Label, Function

def nodename(node):
	return node.__class__.__name__.lower()

def dupvisit(val, newnames):
	frame = sys._getframe(1)
	for name in newnames.split(' '):
		frame.f_locals['visit' + name] = val

class CodeError(Exception):
	pass

class Visitor(object):
	def __init__(self):
		self.cmds = []
		self.loops = []
		self.finaln = [0]
		self.lineno = -1
		
		self.futureDivision = False
	
	def emit(self, *args):
		self.cmds.append((self.lineno,) + args)
	
	def visitModule(self, node):
		self.emit('useonlyglobals')
		self.visit(node.node)
		self.emit('makemodule', node.doc)
		self.emit('return')
	
	def visitStmt(self, node):
		for expr in node.nodes:
			self.visit(expr)
	
	def visitDiscard(self, node):
		self.visit(node.expr)
		self.emit('pop')
	
	def visitReturn(self, node):
		self.visit(node.value)
		self.emit('return')
	
	def visitSubscript(self, node, override_flag=None):
		self.visit(node.expr)
		sub, = node.subs
		if isinstance(sub, ast.Sliceobj):
			self.visit(sub.nodes[0])
			self.visit(sub.nodes[1])
			self.visit(sub.nodes[2])
			slice = True
		else:
			self.visit(sub)
			slice = False
		
		name = 'slicex' if slice else 'item'
		
		flag = override_flag or node.flags
		
		if flag == OP_ASSIGN:
			self.emit('set%s' % name)
		elif flag == OP_DELETE:
			self.emit('del%s' % name)
		else:
			self.emit('get%s' % name)
	
	def visitTryFinally(self, node):
		final = Label()
		end = Label()
		self.emit('setupexc', final)
		
		self.finaln[-1] += 1
		self.visit(node.body)
		self.finaln[-1] -= 1
		
		self.emit('jump', end)
		self.emit(final)
		self.emit('getexc') 
		self.visit(node.final)
		self.emit('reraise')
		self.emit(end)
		self.emit('popexc')
		self.visit(node.final)
	
	def visitTryExcept(self, node):
		exc = Label()
		end = Label()
		else_ = Label()
		self.emit('setupexc', exc)
		
		self.finaln[-1] += 1
		self.visit(node.body)
		self.finaln[-1] -= 1
		
		self.emit('jump', else_)
		self.emit(exc)
		self.emit('getexc') 
		
		for excclass, excname, handler in node.handlers:
			# stack: TOS=exc
			next = Label()
			if excclass:
				self.emit('dup')
				# stack: TOS=exc,TOS1=exc
				self.visit(excclass)
				self.emit('excmatch')
				# stack: TOS=bool,TOS1=exc
				self.emit('jumpifnot', next)
			
			self.emit('dup')
			if excname:
				self.emit('setname', excname)
			else:
				self.emit('pop')
			
			self.visit(handler)
			
			self.emit('pop')
			self.emit('jump', end)
			self.emit(next)
		
		self.emit('reraise')
			
		self.emit(else_)
		self.emit('popexc', 1)
		
		if node.else_:
			self.visit(node.else_)
		
		self.emit(end)
	
	def visitSlice(self, node):
		self.visit(node.expr)
		self.visit(node.lower or ast.Const(None))
		self.visit(node.upper or ast.Const(None))
		self.emit('getslice')
	
	def visitName(self, node):
		if node.name in ('True', 'False', 'None'):
			val = {'True': True, 'False': False, 'None': None}[node.name]
			self.visit(ast.Const(val))
		else:
			self.emit('name', node.name)
	
	def visitCallFunc(self, node):
		def hastrueattr(obj, name):
			return hasattr(obj, name) and getattr(obj, name)
		
		self.visit(node.node)
		args = [ arg for arg in node.args if not isinstance(arg, ast.Keyword) ]
		kwargs = [
			(arg.name, arg.expr) for arg in node.args if isinstance(arg, ast.Keyword)
		]
		kwargs_names = [ name for (name, expr) in kwargs ]
		kwargs_values = [ expr for (name, expr) in kwargs ]
		for kwarg_value in kwargs_values:
			self.visit(kwarg_value)
		for arg in args:
			self.visit(arg)
		if hastrueattr(node, 'star_args'):
			self.visit(node.star_args)
			if hastrueattr(node, 'dstar_args'):
				self.visit(node.dstar_args)
				self.emit('call3', len(node.args), kwargs_names)
			else:
				self.emit('call1', len(node.args), kwargs_names)
		elif hastrueattr(node, 'dstar_args'):
			self.visit(node.dstar_args)
			self.emit('call2', len(node.args), kwargs_names)
		else:
			self.emit('call', len(node.args), kwargs_names)
	
	def visitImport(self, node):
		for name, varname in node.names:
			if not varname:
				fragments = name.split('.', 1)
				if len(fragments) == 2:
					self.emit('import', fragments[0], fragments[1])
				else:
					self.emit('import', fragments[0])
				self.emit('setname', fragments[0])
			else:
				self.emit('import', name)
				self.emit('setname', varname)
	
	def visitAssName(self, node):
		self.emit('setname', node.name)
	
	def visitAssAttr(self, node):
		# setattr attrname (TOS object, TOS1 value)
		self.visit(node.expr)
		self.emit('setattr', node.attrname)
	
	def visitAssTuple(self, node):
		elems = node.nodes
		self.emit('unpacktuple', len(elems))
		self.emit('swapn', len(elems)) # set them in order
		for elem in elems:
			self.visit(elem)
	visitAssList = visitAssTuple
	
	def visitAssign(self, node):
		self.visit(node.expr)
		for ass in node.nodes:
			if node.nodes[-1] is not ass:
				self.emit('dup')
			self.visit(ass)
	
	def visitTuple(self, node):
		elems = node.nodes
		for elem in reversed(elems):
			self.visit(elem)
		self.emit('maketuple', len(elems))
	
	def visitDict(self, node):
		elems = node.items
		for key, val in reversed(elems):
			self.visit(key)
			self.visit(val)
		self.emit('makedict', len(elems))
	
	def visitList(self, node):
		elems = node.nodes
		for elem in reversed(elems):
			self.visit(elem)
		self.emit('makelist', len(elems))
	
	def visitGlobal(self, node):
		for name in node.names:
			self.emit('makeglobal', name)
	
	def visitFrom(self, node):
		if node.modname == '__future__':
			self.applyFutures( name for name, varname in node.names)
		self.emit('import', node.modname)
		for name, varname in node.names:
			if not varname:
				varname = name
			self.emit('dup')
			self.emit('getattr', name)
			self.emit('setname', varname)
		self.emit('pop')
	
	def applyFutures(self, names):
		for name in names:
			if name == 'division':
				self.futureDivision = True
			else:
				raise CodeError('unknow future %s' % name)
	
	def visitRaise(self, node):
		NoneConst = ast.Const(None)
		self.visit(node.expr1 or NoneConst)
		self.visit(node.expr2 or NoneConst)
		self.visit(node.expr3 or NoneConst)
		self.emit('raise3')
	
	def visitWhile(self, node):
		assert not node.else_, 'while loops with else are not supported'
		
		start = Label()
		end = Label()
		self.loops.append((start, end))
		self.finaln.append(0)
		
		self.emit(start)
		self.visit(node.test)
		self.emit('jumpifnot', end)
		self.visit(node.body)
		self.emit('jump', start)
		self.emit(end)
		
		self.loops.pop()
		assert self.finaln.pop() == 0
	
	
	def visitFor(self, node):
		assert not node.else_, 'for loops with else are not supported'
		
		start = Label()
		end = Label()
		pop_and_end = Label()
		self.loops.append((start, end))
		self.finaln.append(0)
		
		self.visit(node.list)
		self.emit('getiter')
		
		self.emit(start)
		self.emit('dup')
		self.emit('foriter', pop_and_end)
		self.visit(node.assign)
		self.visit(node.body)
		self.emit('jump', start)
		
		self.emit(pop_and_end)
		self.emit('pop') # pop (maybe non-existing) value
		self.emit(end)
		self.emit('pop') # pop iterator
	
	def visitAssert(self, node):
		end = Label()
		self.visit(node.test)
		self.emit('jumpif', end)
		self.visit(node.fail or ast.Const(None))
		self.emit('assertfail')
		self.emit(end)
	
	def visitSingleIf(self, test, iftrue, iffalse):
		end = Label()
		else_ = Label()
		self.visit(test)
		self.emit('jumpifnot', else_)
		self.visit(iftrue)
		self.emit('jump', end)
		self.emit(else_)
		if iffalse:
			self.visit(iffalse)
		self.emit(end)
	
	def visitIf(self, node):
		if len(node.tests) == 1:
			self.visitSingleIf(node.tests[0][0], node.tests[0][1], node.else_)
		else:
			test = node.tests[0]
			else_ = ast.If(
				tests=node.tests[1:],
				else_=node.else_
			)
			self.visitSingleIf(test[0], test[1], else_)
	
	def visitIfExp(self, node):
		end = Label()
		else_ = Label()
		
		self.visit(node.test)
		self.emit('jumpifnot', else_)
		self.visit(node.then)
		self.emit('jump', end)
		self.emit(else_)
		self.visit(node.else_)
		self.emit(end)
	
	def runLoopFinals(self):
		self.finaln[-1]
	
	def visitBreak(self, node):
		if not self.loops:
			raise CodeError('break outside the loop')
		if self.finaln[-1]:
			self.runLoopFinals('popexc', self.finaln[-1])
		start, end = self.loops[-1]
		self.emit('jump', end)
	
	def visitContinue(self, node):
		if not self.loops:
			raise CodeError('continue outside the loop')
		self.runLoopFinals()
		start, end = self.loops[-1]
		self.emit('jump', start)
	
	def visitBinop(self, node):
		name = nodename(node)
		if name == 'div' and self.futureDivision:
			name = 'truediv'
		
		self.visit(node.left)
		self.visit(node.right)
		self.emit('binop', name)
	dupvisit(visitBinop, 'Add Div FloorDiv Mul Sub Mod LeftShift RightShift')
	
	def visitConst(self, node):
		self.emit('const', node.value)
	
	def visitAugAssign(self, node):
		get = node.node
		
		self.visit(get)
		self.visit(node.expr)
		self.emit('binopip', node.op)
		
		if isinstance(get, ast.Name):
			self.emit('setname', get.name)
		elif isinstance(get, ast.Subscript):
			self.visitSubscript(get, override_flag=OP_ASSIGN)
	
	def visitBinop2(self, node):
		self.visit(node.nodes[0])
		for expr in node.nodes[1:]:
			self.visit(expr)
			self.emit('binop', nodename(node))
	dupvisit(visitBinop2, 'Bitor Bitand Bitxor')
	
	def visitUnaryop(self, node):
		self.visit(node.expr)
		self.emit('unaryop', nodename(node))
	dupvisit(visitUnaryop, 'Not UnarySub')

	def visitSingleAnd(self, right, isand):
		self.emit('dup')
		end = Label()
		before_end = Label()
		self.emit('jumpif' if not isand else 'jumpifnot', before_end)
		self.emit('pop')
		self.visit(right)
		self.emit('copy')
		self.emit('jump', end)
		self.emit(before_end)
		self.emit('copy')
		self.emit(end)
	
	def visitAnd(self, node):
		isand = nodename(node) == 'and'
		self.visit(node.nodes[0])
		for expr in node.nodes[1:]:
			self.visitSingleAnd(expr, isand)
	visitOr = visitAnd	
	
	def visitCompare(self, node):
		assert len(node.ops) == 1, 'Comparing with many operators not supported'
		(op, to), = node.ops
		self.visit(node.expr)
		self.visit(to)
		self.emit('compare', op)
	
	def visitGetattr(self, node):
		self.visit(node.expr)
		self.emit('getattr', node.attrname)
	
	@staticmethod
	def createFunction(argnames, code):
		visitor = Visitor()
		visitor.functionStart(argnames)
		visitor.visit(code)
		visitor.functionEnd()
		visitor.close()
		return Function(
			argcount=len(argnames),
			body=visitor.cmds,
			loadargs=None,
			varcount=None
		)
	
	def functionStart(self, argnames):	
		for name in reversed(argnames):
			self.emit('setname', name)
	
	def functionEnd(self):
		self.emit('const', None)
		self.emit('return')
	
	def classStart(self):
		pass
	
	def classEnd(self):
		self.emit('getlocalsdict')
		self.emit('return') # return locals dict
	
	@staticmethod
	def createClass(code):
		visitor = Visitor()
		visitor.classStart()
		visitor.visit(code)
		visitor.classEnd()
		visitor.close()
		return Function(0, visitor.cmds, None, None)
	
	def visitClass(self, node):
		self.emit('function', Visitor.createClass(node.code))
		self.emit('call', 0, [])
		for base in node.bases:
			self.visit(base)
		self.emit('maketuple', len(node.bases))
		self.emit('makeclass', dict(
				name=node.name,
				doc=node.doc
			))
		self.emit('setname', node.name)
	
	def visitLambda(self, node):
		assert not any(map((lambda a:a==tuple), map(type, node.argnames))), \
			"unpacking tuples in function arguments not supported (lineno %d)" % self.lineno
		
		code = ast.Return(node.code)
		self.emit('function', Visitor.createFunction(node.argnames, code))
		for default in node.defaults:
			self.visit(default)
		
		self.emit('makefunction', dict(
				defaults=len(node.defaults),
				varargs=bool(node.varargs),
				kwargs=bool(node.kwargs),
				argnames=node.argnames
			))
		
	
	def visitFunction(self, node):
		assert not any(map((lambda a:a==tuple), map(type, node.argnames))), \
			"unpacking tuples in function arguments not supported (lineno %d)" % self.lineno
		
		def getDecorators(dec):
			if not dec:
				return []
			else:
				return dec.nodes
		
		self.emit('function', Visitor.createFunction(node.argnames, node.code))
		for default in node.defaults:
			self.visit(default)
		self.emit('makefunction', dict(
				name=node.name,
				doc=node.doc,
				argnames=list(node.argnames),
				defaults=len(node.defaults),
				varargs=bool(node.varargs),
				kwargs=bool(node.kwargs)
			))
		for decorator in reversed(getDecorators(node.decorators)):
			self.visit(decorator)
			self.emit('swap2')
			self.emit('call', 1)
		self.emit('setname', node.name)
	
	def visitPass(self, node):
		pass
	
	def visitGenExpr(self, node):
		self.visit(node.code)
	
	def visitGenExprInner(self, node):
		body = node.expr
		quals, = node.quals
		assign = quals.assign
		list = quals.iter
		ifs = quals.ifs
		
		self.emit('function', Visitor.createGenexpFunc(body, assign, ifs))
		self.visit(list)
		self.emit('makegenexp')

	def genexpBody(self, body, assign, ifs):
		self.visit(assign)
		for if_ in ifs:
			self.visit(if_.test)
			after = Label()
			self.emit('jumpif', after)
			self.emit('genexpcontinue')
			self.emit(after)
		self.visit(body)
		self.emit('return')
	
	@staticmethod
	def createGenexpFunc(body, assign, ifs):
		visitor = Visitor()
		visitor.genexpBody(body, assign, ifs)
		visitor.close()
		return Function(1, visitor.cmds, None, None)
	
	# === ListComp ===
	
	def visitListComp(self, node):
		body = node.expr
		quals, = node.quals
		assign = quals.assign
		list = quals.list
		ifs = quals.ifs
		
		loop_start = Label()
		loop_done = Label()
		
		self.emit('makelist', 0)
		self.visit(list)
		self.emit('getiter')
		self.emit(loop_start)
		self.emit('dup')
		self.emit('foriter', loop_done) # always pushes value on stack
		self.visit(assign)
		# stack: TOS=iterator, TOS1=output-list
		
		# ifs:
		for if_ in ifs:
			self.visit(if_.test)
			self.emit('jumpifnot', loop_start)
		
		self.emit('swap2')
		# stack: TOS1=output-list, TOS2=iterator
		self.visit(body)
		
		# notice: these swaps don't affect efficiency, since they are removed by ir.py
		# stack: TOS=value, TOS1=output-list, TOS2=iterator
		self.emit('swap2')
		# stack: TOS=output-list, TOS1=value, TOS2=iterator
		self.emit('dup')
		# stack: TOS=output-list, TOS1=output-list, TOS2=value, TOS3=iterator
		self.emit('swapn', 3)
		# stack: TOS=value, TOS1=output-list, TOS2=output-list, TOS3=iterator
		self.emit('listappend')
		# stack: TOS=output-list, TOS1=iterator
		self.emit('swap2')
		# stack: TOS=iterator, TOS1=output-list
		
		self.emit('jump', loop_start)
		self.emit(loop_done)
		self.emit('pop') # pop value from foriter
		self.emit('pop') # pop iterator
	
	def visitPrintnl(self, node):
		self.visit(node.dest or ast.Const(None))
		for expr in reversed(node.nodes):
			self.visit(expr)
		self.emit('maketuple', len(node.nodes))
		self.emit('print', True)
	
	def visitPrint(self, node):
		self.visit(node.dest or ast.Const(None))
		for expr in reversed(node.nodes):
			self.visit(expr)
		self.emit('maketuple', len(node.nodes))
		self.emit('print', False)
	
	def dump(self):
		import pprint
		for cmd in self.cmds:
			print str(cmd[0]) + ':\t',
			for part in cmd[1:]:
				print part,
			print

	def visit(self, node):
		try:
			method = getattr(self, 'visit' + node.__class__.__name__)
		except AttributeError:
			if not node:
				raise RuntimeError('visit(None)')
			print 'Not implemented:'
			dumpNode(node)
			nodes = node.getChildNodes()
			for sub in nodes:
				self.visit(sub)
		else:
			old = self.lineno
			self.lineno = node.lineno
			method(node)
			if old != -1:
				self.lineno = old
	
	def close(self):
		pass

def execute(code):
	v = Visitor()
	parsed = compiler.parse(code)
	v.visit(parsed)
	v.close()
	return v

def executeOnFile(name):
	return execute(open(name).read())

def dumpNode(node):
    print node.__class__
    for attr in dir(node):
        if attr[0] != '_' and attr not in ('getChildren', 'getChildNodes', 'asList'):
            print "   ", "%-10.10s" % attr, getattr(node, attr)

if __name__ == '__main__':
	import sys
	execute(sys.stdin.read()).dump()