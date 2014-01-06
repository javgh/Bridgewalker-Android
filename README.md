This is the Android client for Bridgewalker ( https://www.bridgewalkerapp.com/ ).

The code is only sparsely commented, I'm afraid. To give a very rough overview:
The Android app talks to the Bridgewalker server over an encrypted websocket
connection over which a custom JSON-based protocol is spoken. This part of the
code is implemented as an Android service (see BackendService.java) which is
started and accessed from the various frontend activities. The service uses the
library Jackson to convert the JSON messages from the server into Java objects
(see classes in .apidata.\*) and conversely serializes commands send to the
server to JSON via Jackson as well. Some plumbing exists between the service
and the activities to push updates to the GUI as soon as they arrive and
conversely send commands resulting from user actions on to the server using the
backend service.

Documentation of the Bridgewalker API can be found here:
https://github.com/javgh/bridgewalker/blob/master/doc/api.md .
