from flask import Flask, request, make_response
from flask_api import status
import requests
from socket import *
import json

app = Flask(__name__)

# User Server for Fibonacci Query
# It will first query AS to get IP of FS, then Query FS to get the Fibonacci Number

@app.route('/')
def hello_world():
    return 'This is User Server!'

@app.route('/fibonacci', methods=["GET"])
def fib():

    # Query AS to find IP of FS
    hostname = request.args.get('hostname')
    fs_port = request.args.get('fs_port')
    as_ip = request.args.get('as_ip')
    as_port = request.args.get('as_port')
    X = request.args.get('number')

    if not all([hostname,fs_port,as_ip,as_port,X]):
        return make_response("missing parameter", status.HTTP_400_BAD_REQUEST)

    clientSocket = socket(AF_INET, SOCK_DGRAM)
    json_msg = {"TYPE": "A", "NAME": hostname}

    clientSocket.sendto(json.dumps(json_msg).encode(),(as_ip, int(as_port)))
    print("Msg Sent : " + json.dumps(json_msg))

    serverMessage, serverAddress = clientSocket.recvfrom(2048)
    print("Msg Received : " + serverMessage.decode())
    clientSocket.close()


    # Query FS to get the Fibonacci Number
    if serverMessage.decode() == "DNS Query Fail":
        return make_response("Fail to query DNS for hostname", status.HTTP_400_BAD_REQUEST)

    fs_ip = json.loads(serverMessage.decode())["VALUE"]
    fs_response = requests.get('http://' + fs_ip + ':' + fs_port + '/fibonacci' + '?number=' + X)
    if fs_response.status_code == 200:
        fib_ans = json.loads(fs_response.text)
        return make_response("Fibonacci number for the sequence number " + X  + " is " + str(fib_ans), status.HTTP_200_OK)

    return make_response("Fail to query fibonacci number", fs_response.status_code)


if __name__ == '__main__':
    app.run(host='0.0.0.0',
            port=8080,
            debug=True)