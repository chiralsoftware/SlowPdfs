/*
 * Use WebSocket to do dynamic updates to show progress of a slow task.
 * This is updated to use the modern StompJS 5. Previous versions of StompJS are
 * not maintained.
 * See: https://github.com/stomp-js/stompjs
 * This version also uses the modern fetech API instead of XMLHttpRequest
 *   */
$(document).ready(function () {
    console.log("Activating button");
    $('#startButton').click(startTask)
    connect();
});

function startTask() {
    $(this).prop('disabled', true);
    
    var form = new FormData();
    form.append("number", $('#number').val());
    
    request = new Request('start', {
        method: 'POST' ,
        body: form
    });
    fetch(request).
            then((resp) => resp.json()).
            then(complete);
}

function complete(data) {
    console.log("the task completed, here is result: " + data.message);
}

function connect() {
//    var socket = 'ws://localhost:8080/SlowPdfs/mystatus';

    stompClient = new StompJs.Client({
        brokerURL: WEBSOCKET_URL,
        reconnectDelay: 1000,
        heartbeatIncoming: 1000,
        heartbeatOutgoing: 1000
    });
    
    stompClient.onConnect = function (frame) {
        stompClient.subscribe('/topic/status', function (messageOutput) {
            console.log("New Message: " + messageOutput);
            var messageObject = $.parseJSON(messageOutput.body);
            $('#fileProgress').val(messageObject.progress);
            $('#message').text(messageObject.message);
            if(messageObject.status === "complete") {
                console.log("The status is complete, download link should be: download/" + messageObject.filename + ".zip");
                $("#results").append("<tr><td>File: " + messageObject.filename + "</td>" + 
                        "<td><a href=\"./download/" + messageObject.filename + ".zip\">" + 
                        messageObject.filename + "</a></td></tr>\n"
                        );
                $('#startButton').prop('disabled', false);
                $('#fileProgress').val(0);
            }
        });

    };
    stompClient.onStompError = function(frame) {
      console.log('Broker reported error: ' + frame.headers['message']);
      console.log('Additional details: ' + frame.body);
    }
    stompClient.activate();
    console.log("Stomp should be connected");
}
