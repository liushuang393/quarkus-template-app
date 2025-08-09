#!/bin/bash

# 设置UTF-8编码环境变量
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export LC_CTYPE=en_US.UTF-8

# Java编码参数
export JAVA_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Tokyo -Dsun.jnu.encoding=UTF-8 -Duser.language=ja -Duser.country=JP"
export MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# 设置终端编码
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
    # Windows Git Bash
    chcp.com 65001 > /dev/null 2>&1
fi

echo "Starting Quarkus with UTF-8 encoding..."
echo "JAVA_OPTS: $JAVA_OPTS"
./mvnw quarkus:dev -Pdev -Ddebug=5005
