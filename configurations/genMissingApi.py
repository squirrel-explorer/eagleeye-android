#!/usr/bin/env python3

import hashlib
import os

# Defines
methodsFileDir = './hidden_api_database'
cfgFileName = 'hidden-api.cfg'
urlPrefix = 'https://raw.githubusercontent.com/squirrel-explorer/eagleeye-android/master/configurations/hidden_api_database/'

for (dirpath, dirnames, filenames) in os.walk(methodsFileDir) :
    break
methodsFileList = filter(lambda str:str.endswith('.xml'), filenames)

cfgContent = ''

for methodsFile in methodsFileList :
    cfgContent += urlPrefix + methodsFile + '\n'

    md5 = hashlib.md5()
    methodsFileHandler = open(methodsFileDir + '/' + methodsFile, 'rb')
    md5.update(methodsFileHandler.read())
    cfgContent += md5.hexdigest() + '\n'
    methodsFileHandler.close()

cfgContent = cfgContent[:-1]

print(cfgContent)

cfgFileHandler = open(cfgFileName, 'w')
cfgFileHandler.write(cfgContent)
cfgFileHandler.close()
