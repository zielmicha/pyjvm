
assert 1+2 == 3
assert 2*3 == 6
#assert 10/2 == 5
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