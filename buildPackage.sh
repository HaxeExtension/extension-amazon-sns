#!/bin/bash
dir=`dirname "$0"`
cd "$dir"
#rm -rf project/obj
#lime rebuild . ios
#rm -rf project/obj
rm -f extension-amazon-sns.zip
zip -r extension-amazon-sns.zip extension haxelib.json include.xml dependencies
