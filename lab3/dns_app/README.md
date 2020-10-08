Simplified authoritative server for a network of applications

* AS Server (Port 53533) for DNS Registration and DNS Query
* FS Server (Port 9090) for Fibonacci Query and Self Registration
* User Server (Port 8080) for User's Fibonacci Query

To run all 3 servers, you can simply run them via python3 app.py. They work well locally.

Or you can run via docker

```shell

docker network create lab3

docker build . -t lingweiluo/lab3_us

docker run --network lab3 --name lab3_US -p 8080:8080 -it lingweiluo/lab3_us:latest

docker build . -t lingweiluo/lab3_fs

docker run --network lab3 --name lab3_FS -p 9090:9090 -it lingweiluo/lab3_fs:latest

docker build . -t lingweiluo/lab3_as

docker run --network lab3 --name AS -p 53533:53533/udp -it lingweiluo/lab3_as:latest

```


Example requests:  

Step 1: FS self registration  

[PUT] http://0.0.0.0:9090/register

```json
{
"hostname": "fibonacci.com",
"ip": "0.0.0.0",
"as_ip": "0.0.0.0",
"as_port": "53533"
}
```

Record in DataBase 
```json
{"TYPE": "A", "NAME": "FIBONACCI.COM", "VALUE": "0.0.0.0", "TTL": "10"}
```

Step 2: User's Fibonacci Query via US Server  

[GET] http://0.0.0.0:8080/fibonacci?hostname=fibonacci.com&fs_port=9090&number=8&as_ip=0.0.0.0&as_port=53533

Response
```json
[200] Fibonacci number for the sequence number 8 is 13
```