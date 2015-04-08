# JDBCmonitor
WebLogic provides rich JDBC monitoring and diagnostics capabilities (debug, WLDF), however does not provide lightweight facility to report executed SQL statements lasting longer than predefined time. Moreover available out of the box subsystems are little too heavy, in area of configuration and generate too much information. Practical use case is to be informed only about SQL executions which consumed too much time. Presented solution delivers such lightweight SQL monitoring functionality. 

Missing capability of WebLogic is filled with JDBC monitor utilizing interceptor interface exposed by WLS JDBC layer. The JDBC monitor traces all JDBC executions to extract SQL command, related variables, and final execution time. In case of execution longer than predefined threshold, complete information related to such execution is logged in an alert log. JDBC monitor is shipped with user interface presenting real time SQL information and latest alerts. Note that user interface hows not yet completed SQL executions, while alert log shows only completed ones. In case of stuck execution on execute() JDBC method web interface is the only way to find out such situation.

*Note that it's an early version of the software. I've made a lot of work to tune and ensure that software is free of problems, but it's only software. Use it with caution.*

## Alert log
All executions lasting more that defined threshold are reported to ODL log file. ODL log files are located in server's log directory. SQL alert file is suffixed by "-sql[1-5].log", where number at the end of file name indicates interceptor number. It's important as operator may configure different log files for different data sources. 

All interceptors share single debug log file. Apart of debug information you will find content from sql[12345]*.log files in the debug log.

## User interface
User interface provides two main functions: (1) view on currently active SQL statements, and (2) view on latest alerts. 

View on active SQL is available under: http://host:port/JDBCmonitor/currentSQL
View on latest alerts is available under: http://host:port/JDBCmonitor/latestAlerts

Provided menu makes it easy to quickly switch between both views. Next to standard options, console menu contains possibility to configure sensitivity, and debug level. Use detailed debugging with caution, as amount of logged information is quite big, dramatically slowing down server JDBC operations. Menu provides ability to execute exemplary by inention slow SQL statement ({call dbms_lock.sleep(?)}) on "jdbc/SOADataSource". This feature is used to verify proper work of JDBC monitor. Execution of dbs_lock may be parametrized by providing (a) sleep time, (b) datasource, (c) sleep in Java code between JDBC operations, and (d) information to finalize sql interaction w/o close. It's possible to specify own SQL command. 

More in documentation....

