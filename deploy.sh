#!/bin/bash

# 현재 실행 중인 .jar 프로세스를 찾음
CURRENT_PID=$(pgrep -f community.backend-0.0.1-SNAPSHOT.jar)
echo "$CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
    echo "No process running."
else
    echo "Kill process $CURRENT_PID"
    kill -9 "$CURRENT_PID"
    sleep 3
fi

# 지정한 디렉토리에서 특정 패턴을 가진 JAR 파일을 찾음
JAR_PATH="/var/www/html"
SELECTED_JAR=$(find "$JAR_PATH" -name "community.backend-0.0.1-SNAPSHOT.jar")

if [ -z "$SELECTED_JAR" ]; then
    echo "No JAR file found."
else
    echo "Selected JAR path: $SELECTED_JAR"
    chmod +x "$SELECTED_JAR"
    nohup java -jar "$SELECTED_JAR" > /dev/null 2>&1 &
    echo "JAR file deploy success"
fi