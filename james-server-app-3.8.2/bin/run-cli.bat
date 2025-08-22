@echo off
setlocal
set CLASSPATH=..\conf;..\lib\*
set JAVA_OPTS=-Dapp.name=james-cli -Dapp.repo=..\lib -Dapp.home=.. -Dbasedir=..

java %JAVA_OPTS% -cp %CLASSPATH% org.apache.james.cli.ServerCmd %*
