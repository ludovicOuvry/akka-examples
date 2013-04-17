Remote Hello World Demo
================

Two remote actor system are created: server and client. 
Server actor system creates actor which just prints received messages into ``System.out`` 
Client actor system makes lookup for remote actor and send simple text message to it 
Remote config is created from ``Map`` where keys are config keys and values are config values 
