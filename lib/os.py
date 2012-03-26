import reflect
import sys

RandomAccessFile = reflect.get_class('java.io.RandomAccessFile')
System = reflect.get_class('java.lang.System')

class JavaFile:
    def __init__(self, input=None, output=None):
        self.in_f = input
        self.out_f = output
    
    def read(self, size=1000000000): # power not yet implemented = 2**30
        result = []
        dest = bytearray(1024)
        while size:
            if size < 1024:
                buff = size
            else:
                buff = 1024
            n = self.in_f.read(dest, 0, buff)
            if n in (0, -1):
                break
            size -= n
            result.append(dest.to_string(0, n))
        return ''.join(result)
    
    def read_char(self):
        code = self.in_f.read()
        return chr(code) if code != -1 else ''
    
    def readline(self):
        line = []
        while True:
            ch = self.read_char()
            if not ch:
                break
            line.append(ch)
            if ch == '\n':
                break
        return ''.join(line)
    
    def readlines(self):
        l = []
        while True:
            line = self.readline()
            if not line:
                return l
            l.append(line)
    
    def write(self, data):
        self.out_f.write(data)
    
    def flush(self):
        self.out_f.flush()

class File(JavaFile):
    def __init__(self, name, mode='r'):
        f = RandomAccessFile.new(name, mode)
        JavaFile.__init__(self, f, f)


sys.stdin = JavaFile(getattr(System, 'in'))
sys.stdout = JavaFile(None, System.out)
sys.stderr = JavaFile(None, System.err)
