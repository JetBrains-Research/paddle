#!/usr/bin/env python3

import sys
import importlib


class PluginsProvider:
    def __init__(self, plugins_dir):
        self.plugins_dir = plugins_dir
        self.plugins = dict()

    def get_plugin(self, name):
        plugin = self.plugins.get(name, None)
        if not plugin:
            sys.path.append(f"{self.plugins_dir}/{name}")
            mod = importlib.import_module("main")
            plugin = getattr(mod, "plugin")
            self.plugins[name] = plugin

        return plugin

    def invalidate_cache(self):
        self.plugins = dict()
