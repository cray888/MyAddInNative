#!/bin/sh
svn up .
set -e


productbuild  --component ~/BldArea/addindoc/example/tmp/1CEAdnWebNPAPISafOSX.bundle  /Library/Internet\ Plug-Ins/ ../../AddInSafOSX.pkg


