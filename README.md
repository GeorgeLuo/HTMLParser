# HTMLParser (Java 8)

Import into your IDE of choice and/or run Maven clean, Maven generate resources, then Maven generate install. The jar file will then be found under the /target directory (NOTE: this jar file should not be moved without the accompanied dependencies). The program takes one commandline argument, the name of the file which contains the 5 URLs:

```
java -jar HTMLParser-0.0.1-SNAPSHOT.jar Links.txt
```

The statistics gathered will be placed under the root directory with the name, 'runtime-statistics.txt'.

Main.java contains 3 parts: single threaded behavior, multithreaded, and finally asynchronous. HTTPClient was used to make the URL queries, while Apache Tika was used to parse the responses of styling/scripting. 

TODO: Refactor setup into methods.

TODO: Refactor parts 1, 2, and 3 into methods.

TODO: Combine/configure try/catch to use same finally block.
