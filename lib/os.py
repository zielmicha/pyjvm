import reflect

RandomAccessFile = reflect.get_class('java.io.RandomAccessFile')

class File:
    def __init__(self, name, mode='r'):
        self.f = RandomAccessFile.new(name, mode)
    
    def read(self, size=1000000000): # power not yet implemented = 2**30
        result = []
        dest = bytearray(1024)
        while size:
            if size < 1024:
                buff = size
            else:
                buff = 1024
            n = self.f.read(dest, 0, buff)
            if n in (0, -1):
                break
            size -= n
            result.append(dest.to_string(0, n))
        return ''.join(result)
    
    def read_char(self):
        code = self.f.read()
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