set SBT_OPTS=-Xms64M -Xmx1G -Xss1M
call java %SBT_OPTS% -jar %~dp0\sbt-launch.jar %*