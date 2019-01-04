# SlowPdfs
Demonstrate showing a progress bar with WebSocket
# What it does
This shows a slow-running task on the server, which is generating a large number of PDFs and putting them in a
zip file. The PDF generation is artificially slowed. The server uses STOMP over WebSocket to update the client
on progress. The project has no external dependencies and is the bare minimum to show this feature.
# Features
This project uses modern APIs, including StompJs 5.0 and the Fetch API, instead of XMLHttpRequest.
# WebSocket through a proxy
This project shows how to enable WebSockets behind a proxy, such as using Tomcat behind an Nginx server. In this case, the Java side must use setAllowedOrigins to allow the client to connect. This code uses an environment property to make deployment easy in both production (with a proxy) and in development (on localhost). See the WebSocketConfig.java file.
# To do
There should be ability to run multiple jobs, watch their progress, and also cancel them if needed.
# Try it
A test site is available: https://chiralsoftware.com/SlowPdfs/
