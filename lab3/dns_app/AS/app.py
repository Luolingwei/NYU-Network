from socket import *
import json

# AS Server for DNS Registration and DNS Query
# Established by Socket UDP
# DNS records are stored in DataBase/record.json

if __name__ == '__main__':

    serverPort = 53533
    serverSocket = socket(AF_INET, SOCK_DGRAM)
    serverSocket.bind(('', serverPort))
    print("AS server is ready to receive msg")

    while True:
        clientMessage, clientAddress = serverSocket.recvfrom(2048)
        json_msg = json.loads(clientMessage.decode().upper())
        print("Msg Received : " + clientMessage.decode())

        # DNS Registration
        if "VALUE" in json_msg:
            with open("../DataBase/record.json", "a") as f:
                json.dump(json_msg, f)
                f.write('\n')
            print("Registration Complete...")
            serverSocket.sendto("Success".encode(), clientAddress)
            print("Msg Sent : " + "Success")

        # DNS Query
        else:
            with open("../DataBase/record.json", 'r') as f:
                flag = False
                for line in f.readlines():
                    data = json.loads(line)
                    if json_msg['NAME'] == data['NAME']:
                        serverSocket.sendto(line.encode(), clientAddress)
                        flag = True
                        print("Msg Sent : " + json.dumps(data))
                        break
                if not flag:
                    serverSocket.sendto("DNS Query Fail".encode(), clientAddress)
                    print("Msg Sent : " + "DNS Query Fail")