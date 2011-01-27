#!/usr/bin/env python
import ir

def selectvar(nestedvars, instr):
	code = instr.sibiligs()
	
	localnames = set(
		instr.args[0] for instr in code if instr.name == 'setname'
	)
	globalnames = set(
		instr.args[0] for instr in code if instr.name == 'global'
	)
	localnames -= globalnames
	localnames = list(localnames)
	
	useonlyglobals = any( ins.name == 'useonlyglobals' for ins in code )
	if useonlyglobals:
		localnames = []
	
	varcount = len(localnames)
	
	newnestedvars = dict(
		(k, (nesting+1, num))
		for k, (nesting, num) in nestedvars.items()
	)
	newnestedvars.update(dict(
		(k, (0, num))
		for num, k in enumerate(localnames)
	))
	
	def process(instr):
		#print '\033[01;37m', instr.name, instr.args, '\033[00m'
		
		name = instr.name
		if name in ('name', 'setname', 'delname'):
			prefix = dict(name='', setname='set', delname='del')[name]
			varname = instr.args[0]
			
			if varname in localnames:
				opt = 'local'
				arg = localnames.index(varname),
			elif varname in nestedvars:
				opt = 'nested'
				nesting, num = nestedvars[varname]
				arg = nesting, num
			else:
				opt = 'global'
				arg = varname,
			return instr.clone(name=prefix + opt, args=arg, varcount=varcount)
		
		if name == 'function':
			newfunc = ir.cmdlist_to_instr(instr.args[0].body,
				nested=newnestedvars, argcount=instr.args[0].argcount)
			return instr.clone(args=(newfunc,), varcount=varcount)
		
		if name == 'getlocalsdict':
			instr.args = (localnames, )
		
		return instr.clone(varcount=varcount)
	
	return instr.walk(process)

def execute(code):
	import ir
	instr = ir.execute(code)
	return instr

if __name__ == '__main__':
	import sys
	sys.setrecursionlimit(600)
	try:
		execute(sys.stdin.read()).dump()
	except:
		import traceback
		traceback.print_exc(limit=100)