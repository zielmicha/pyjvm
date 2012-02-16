import sys

import pyjvm.build

def main():
    sys.setrecursionlimit(2000)
    
    result = parse_args()
    if not result:
        return help()
    project_path, cmd, args = result
    
    if cmd == 'run':
        cmd_run(project_path, args)
    elif cmd == 'clean':
        cmd_clean(project_path, args)
    else:
        return help()

def parse_args():
    args = sys.argv[1:]
    
    if len(args) == 0:
        return
    
    project_path = '.'
    if args and args[0] == '-p':
        project_path = args[1]
        args = args[2:]
    
    if len(args) == 0:
        return
    
    cmd = args[0]
    return project_path, cmd, args[1:]

def help():
    print 'Usage: pyjvm [-p <project-path>] <action> <args...>'
    print 'action:'
    print '\trun     runs project'
    print '\tclean   cleans project build output'
    print 'see also: pyjvm.zielm.com'

def cmd_run(path, args):
    if args:
        modname, = args
    else:
        modname = None
    
    proj = pyjvm.build.Project(path)
    proj.build()
    proj.execute(modname or proj.main_module)

def cmd_clean(path, args):
    if args:
        sys.exit("pyjvm clean - not expecting any arguments")
    
    proj = pyjvm.build.Project(path)
    proj.clean()