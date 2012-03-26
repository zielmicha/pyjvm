import sys
import os

def map(func, seq):
    return [ func(item) for item in seq ]

def raw_input(prompt):
    print prompt,
    return sys.stdin.readline()