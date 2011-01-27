#!/usr/bin/env python

def pop_many(list, n):
	result = []
	for i in xrange(n):
		result.append(list.pop())
	return result

class Struct(object):
	_defaults = None
	_ignore_in_repr = ''
	
	def __init__(self, **kwargs):
		if self._defaults:
			kwargs2 = self._defaults.copy()
			kwargs2.update(kwargs)
			kwargs = kwargs2
		for field in self._fields.split():
			if field not in kwargs:
				raise ValueError('Required argument (%s) not given' % field)
			setattr(self, field, kwargs[field])
			del kwargs[field]
		if kwargs:
			raise ValueError('Unexpected argument %s' % kwargs.keys()[0])
		self._init_struct()
	
	def _init_struct(self):
		pass
	
	def __repr__(self):
		attrs = ' '.join(
			'%s=%r' % (k, v)
			for k, v in self._attrs.items()
			if k not in self._ignore_in_repr.split()
		)
		return '<%s %s>' % (self.__class__.__name__, attrs) 
	
	@property
	def _attrs(self):
		return dict(
			(key, getattr(self, key))
			for key in self._fields.split()
		)
	
	def clone(self, **attrs):
		thisdict = self._attrs.copy()
		for k, v in attrs.items():
			assert k in self._fields
			thisdict[k] = v
		new = type(self)(**thisdict)
		return new

