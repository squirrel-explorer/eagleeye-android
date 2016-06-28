#!/usr/local/bin/python3

import hashlib
import os

# Defines
methodsFileDir = './missing_api_database'
cfgFileName = 'missing-api.cfg'

for (dirpath, dirnames, filenames) in os.walk(methodsFileDir) :
    break
methodsFileList = filter(lambda str:str.endswith('.xml'), filenames)

cfgContent = ''

for methodsFile in methodsFileList :
    cfgContent += methodsFile + '\n'

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
