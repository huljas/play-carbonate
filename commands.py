import sys
import os
import subprocess

MODULE = 'carbonate'

# Commands that are specific to your module

COMMANDS = ['carbonate', 'carbonate:help', 'carbonate:new']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "carbonate:new":
        java_cmd = app.java_cmd(args, None, "play.modules.carbonate.NewMigrationMain")
        try:
            subprocess.call(java_cmd, env=os.environ)
        except OSError:
            print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
            sys.exit(-1)
    else:
        print "~ Carbonate module is used for managing your database changes."
        print "~"
        print "~ Usage:"
        print "~ play carbonate:new - Creates new database migration file, prefilled with changes detected between your database and model."

# This will be executed before any command (new, run...)
def before(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")


# This will be executed after any command (new, run...)
def after(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "new":
        pass
