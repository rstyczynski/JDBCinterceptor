# JDBCinterceptor
WebLogic provides rich JDBC monitoring and diagnostics capabilities (debug, WLDF), however does not provide lightweight facility to report executed SQL statements lasting longer than predefined time. Moreover available out of the box subsystems are little too heavy, in area of configuration and generate too much information. Practical use case is to be informed only about SQL executions which consumed too much time. Presented solution delivers such lightweight SQL monitoring functionality. 

