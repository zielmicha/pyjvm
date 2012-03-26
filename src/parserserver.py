import socket
import threading

import pyjvm.compiler.ir
import pyjvm.compiler.serialize

def main():
    sock = socket.socket()
    
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', 8123))
    sock.listen(1)
    
    while True:
        client, addr = sock.accept()
        threading.Thread(target=client_handler, args=[client]).start()
        del client

def client_handler(raw_sock):
    sock = raw_sock.makefile('r+')
    filename = sock.readline().strip()
    size = int(sock.readline().strip())
    print '[read]', filename
    data = sock.read(size)
    
    instr = pyjvm.compiler.ir.execute(data)
    sock.write(pyjvm.compiler.serialize.serialize(instr, filename=filename))
    sock.close()
    raw_sock.close()
    print '[sent]', filename

if __name__ == '__main__':
    main()