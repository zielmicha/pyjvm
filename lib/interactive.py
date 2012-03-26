import socket
import sys

def main():
    print 'PyJVM'
    ns = string_dict()
    while True:
        line = raw_input('>>>')
        if not line.strip():
            break
        data = compile(line)
        try:
            data.run(ns)
        except:
            ans = raw_input('Error. Continue? (y/n)').strip().lower()
            if ans != 'y':
                raise

def compile(data, filename=None):
    sock = socket.socket()
    sock.connect(('localhost', 8123))
    f = sock.makefile('r+')
    f.write((filename or '<string>') + '\n')
    f.write(str(len(data)) + '\n')
    f.write(data)
    f.flush()
    
    data = f.read()
    return sys.unserialize(data)