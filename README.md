### Concurent java chat application
When **client** sends message, its added to a **queue** and **MessageDispatcher** is notified that there is message to be delivered.
Everytime message dispatcher try to deliver message thread goes to sleep for 10s to prove the order is right 
