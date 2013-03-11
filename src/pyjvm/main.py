import sys

import pyjvm.build

def main():
    sys.setrecursionlimit(2000)

    result = parse_args()
    project_path, cmd, args = result

    project = pyjvm.build.Project(project_path)

    if not cmd:
        return help(project)

    try:
        project.load()
    except Exception as e:
        pass

    if cmd == 'run':
        cmd_run(project, args)
    elif cmd == 'clean':
        cmd_clean(project, args)
    elif cmd == 'create':
        cmd_create(project, args)
    elif cmd == 'jar':
        cmd_jar(project, args)
    elif cmd in project.actions:
        project.actions[cmd](args)
    else:
        return help(project)

def parse_args():
    args = sys.argv[1:]

    if len(args) == 0:
        return '.', None, None

    project_path = '.'
    if args and args[0] == '-p':
        project_path = args[1]
        args = args[2:]

    if len(args) == 0:
        return project_path, None, None

    cmd = args[0]
    return project_path, cmd, args[1:]

def help(project=None):
    print 'Usage: pyjvm [-p <project-path>] <action> <args...>'
    print 'action:'
    print '\trun        runs project'
    print '\tclean      cleans project build output'
    print '\tcreate     creates new project'
    print '\tjar        build JAR for project'
    if project:
        print 'project specific actions:'
        try:
            project.load()
        except Exception as e:
            print '\tfailed to load project (%s)' % e
        else:
            for name in project.actions:
                doc = getattr(project.actions[name], '__doc__', '')
                print '\t%s %s' % (name.ljust(10), doc)
            if not project.actions:
                print '\tnone'
    print 'see also: pyjvm.zielm.com'

def cmd_run(proj, args):
    if args:
        modname, = args
    else:
        modname = None

    if modname:
        proj.include_modules.append(modname)
    proj.build()
    proj.execute(modname or proj.main_module)

def cmd_clean(proj, args):
    if args:
        sys.exit("pyjvm clean - not expecting any arguments")

    proj.clean()

def cmd_create(proj, args):
    if args:
        sys.exit("pyjvm create - not expecting any arguments")

    proj.create()

def cmd_jar(proj, args):
    if args:
        sys.exit("pyjvm jar - not expecting any arguments")

    proj.build()
    proj.create_jar()