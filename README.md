# GridFS example

For file uploading and downloading over http

Upload pic

```bash
➜  gridfs-example git:(master) ✗ curl -X POST -H "Content-Type: image/jpeg" --data-binary "@path/to/cat.jpg" http://localhost:8080/photo

Photo uploaded with id: 662e993bacca585fd977b32a%  
```

Show files in GridFS

```bash
➜  gridfs-example git:(master) ✗ mongofiles list                                                                                                           
2024-04-28T23:45:57.381+0500    connected to: mongodb://localhost/
photo.jpg       2036202
```

Download pic by id

```bash
➜  gridfs-example git:(master) ✗ curl -v -OJ http://localhost:8080/photo/662e993bacca585fd977b32a.jpeg                                                    
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying [::1]:8080...
* Connected to localhost (::1) port 8080
> GET /photo/662e993bacca585fd977b32a.jpeg HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.4.0
> Accept: */*
> 
< HTTP/1.1 200 OK
< Date: Sun, 28 Apr 2024 18:46:31 GMT
< Connection: keep-alive
< Content-Length: 2036202
< Content-Type: image/jpeg
< 
{ [102269 bytes data]
```
