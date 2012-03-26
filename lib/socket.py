import reflect
import os

Socket = reflect.get_class('java.net.Socket')

class socket:
    def __init__(self):
        pass
    
    def connect(self, address):
        (addr, port) = address
        self._socket = Socket.new(addr, port)
    
    def makefile(self, mode='r+'):
        return os.JavaFile(self._socket.getInputStream(), self._socket.getOutputStream())

