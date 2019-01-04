# SlowPdfs
Demonstrate showing a progress bar with WebSocket
# What it does
This shows a slow-running task on the server, which is generating a large number of PDFs and putting them in a
zip file. The PDF generation is artificially slowed. The server uses STOMP over WebSocket to update the client
on progress.
# Features
This project uses modern APIs, including StompJs 5.0 and the Fetch API, instead of XMLHttpRequest.
# To do
There should be ability to run multiple jobs, watch their progress, and also cancel them if needed.
