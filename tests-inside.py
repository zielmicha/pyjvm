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


assert 1+2 == 3
assert 2*3 == 6
assert 10/2 == 5
assert 10//2 == 5
assert 5-2 == 3

def f1(a, b, c):
	assert a==1
	assert b==2
	assert c==3

f1(1,2,3)

a = 10
a -= 1
assert a == 9

a = 10
a += 1
assert a == 11

seq = [1,2,3]
seq2 = (1,2,3)

assert seq[0] == 1

def f2():
	return 4

assert f2() == 4

assert int(5) == 5

for i in seq:
	assert i == 1
	break

number = 0

for i in xrange(3):
	number += 1

assert number == 3

seq.append(9)
assert seq[3] == 9
assert seq.__len__() == 4
assert len(seq) == 4

try:
	non_existing
except: pass
else: assert False

assert True or False
assert True and True
assert not False

def f3():
	non_existing

try:
	f3()
except: pass
else: assert False

class F:
	f = 4

assert F.f == 4
F.g = 5
assert F.g == 5

instance = F()
assert instance.f == 4
assert instance.g == 5
alamakota = 0
class D:
	def __init__(self):
		self.i = 5
	
	def m1(self):
		return 12

d = D()
assert d.i == 5
assert d.m1() == 12

import test_import
assert test_import.value == 5

try:
	non_exising
except AttributeError: pass
else: assert False

try:
	non_exising
except TypeError: assert False
except AttributeError: pass

def f3(a, b=1):
	assert b == a

f3(1)
f3(2, 2)

try:
	f3()
except TypeError: pass
else: assert False

try:
	f3(2, 2, 2)
except TypeError: pass
else: assert False

def f4(a, b):
	assert a - 1 == b

#f4(b=1, a=2)

def f5(a, b, c, d=4, e=0):
	assert a == 1
	assert b == 2
	assert c == 3
	assert d == 4
	assert e == 5

f5(1, 2, c=3, e=5)

s = 'abc'
s += 'def'
assert s == 'abcdef'

assert ','.join(['1', '2', '3']) == '1,2,3'

from test_import import value

assert value == 5

assert (1 < 2 < 3)
assert not (3 < 2 < 3)
assert not (1 < 2 < 1)

assert 1<2
assert 2>1
assert 1<=2
assert 2>=2
assert 1<=1
assert 1>=1

f = 10
del f

try:
	f
except AttributeError: pass
else: assert False

class S: pass
s = S()
s.f = 5
del s.f

try:
	s.f
except AttributeError: pass
else: assert False

try:
	raise IOError()
except IOError: pass
else: assert False

try:
	raise IOError
except IOError: pass
else: assert False

try:
	raise IOError, 5
except IOError: pass
else: assert False

try:
	raise IOError(), 5
except TypeError: pass
else: assert False
