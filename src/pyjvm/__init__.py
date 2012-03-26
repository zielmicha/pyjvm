import sys
import os

def add_dir_to_path(file):
    sys.path.append(os.path.dirname(file))

def assert_build_script(name):
    if name != '__build__':
        raise RuntimeError('Build script executed not in build context')