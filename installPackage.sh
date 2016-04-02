#!/bin/bash
dir=`dirname "$0"`
cd "$dir"
haxelib remove extension-amazon-sns
haxelib local extension-amazon-sns.zip
