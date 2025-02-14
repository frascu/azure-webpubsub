# Publish and subscribe messages

## Prerequisites

- [Java Development Kit (JDK)](/java/azure/jdk/) version 8 or above
- [Apache Maven](https://maven.apache.org/download.cgi)
- Create an Azure Web PubSub resource
- [ngrok](https://ngrok.com/download) to expose localhost endpoint

## Use ngrok to make the local server publicly available
Run the script `ngrok http 8080`, then copy the URL above the red line.
![ngrok](../../../docs/images/ngrok-sample.png)

## Setup event handler settings
1. Navigate to `settings` in portal.
2. Make a new hub setting for `chat`.
3. Fill in the URL copied from the previous step to `URL template`.
4. Fill in `*` to `User Event Pattern` and select all the system events.
5. Click `Save` button to update the settings, wait until the settings are updated successfully.
![event handler settings](../../../docs/images/eventhandler-settings-sample.png)

## Start server

1. Copy **Connection String** from **Keys** tab of the created Azure Web PubSub service, run the below command with the `<connection-string>` replaced by your **Connection String**:

```console
mvn compile & mvn package & mvn exec:java -Dexec.mainClass="com.webpubsub.tutorial.App" -Dexec.cleanupDaemonThreads=false -Dexec.args="'<connection_string>'"
```

![connection string](../../../docs/images/portal_conn.png)

## Send Messages in chat room
1. Open a browser in and visit http://localhost:8080.
2. Input your user name, and click `OK` button to attend the chat.

3. You will get welcome message `[SYSTEM] <user-name> is joined`.
4. Input a message to send, press `Enter` key to publish. 
5. You will see the message in the chat room.
6. Repeat the above steps in a window, you can see messages broadcast to all the windows.
![chat room](../../../docs/images/simple-chat-room.png)
