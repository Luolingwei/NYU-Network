from flask import Flask, request, make_response
from flask_api import status
from socket import *
import json

app = Flask(__name__)

# FS Server for Fibonacci Query and Self Registration

def calFib(seq):
    if seq == 1: return 0
    a, b = 0, 1
    for _ in range(seq-2):
        a, b = b, a+b
    return b


@app.route('/')
def hello_world():
    return 'This is Fibonacci Server!'


# Fibonacci Query
@app.route('/fibonacci', methods=["GET"])
def fib():
    number = request.args.get('number')
    if not number.isdigit():
        return make_response("X is not a integer", status.HTTP_400_BAD_REQUEST)
    return make_response(str(calFib(int(number))), status.HTTP_200_OK)


# Self Registration by calling AS
@app.route('/register', methods=["PUT"])
def register():

    data = json.loads(request.data)

    hostname = data['hostname']
    ip = data['ip']
    as_ip = data['as_ip']
    as_port = data['as_port']

    clientSocket = socket(AF_INET, SOCK_DGRAM)
    json_msg = {"TYPE": "A", "NAME": hostname, "VALUE": ip, "TTL": "10"}

    clientSocket.sendto(json.dumps(json_msg).encode(),(as_ip, int(as_port)))
    print("Msg Sent : " + json.dumps(json_msg))

    serverMessage, serverAddress = clientSocket.recvfrom(2048)
    print("Msg Received : " + serverMessage.decode())

    if serverMessage.decode() == "Success":
        clientSocket.close()
        return make_response("registration is successful", status.HTTP_201_CREATED)

    clientSocket.close()


if __name__ == '__main__':
    app.run(host='0.0.0.0',
            port=9090,
            debug=True)