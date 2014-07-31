@echo OFF

rmdir /Q /S build
mkdir "build\class\scwu\js"
mkdir "build\class\scwu\switchboard"

copy /Y /B "ext\manifest.mf" "build\manifest.mf"
copy /Y /B "ext\json.jar" "build\json.jar"
copy /Y /B "ext\java_websocket.jar" "build\java_websocket.jar"
copy /Y /B "ext\commons-lang3-3.1.jar" "build\commons-lang3-3.1.jar"

javac -d "build\class" -cp ".;ext\commons-lang3-3.1.jar;ext\java_websocket.jar;ext\json.jar" "src\scwu\switchboard\*.java"
copy /Y /B "src\scwu\js\init.js" "build\class\scwu\js\init.js"
copy /Y /B "src\scwu\js\json.js" "build\class\scwu\js\json.js"

jar cvmf "build\manifest.mf" "build\Switchboard.jar" -C "build\class" .

java -jar "proguard.jar" @proguard-switchboard.conf
