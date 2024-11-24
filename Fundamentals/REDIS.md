### Run REDIS container 
docker run --name redis-container -p 6379:6379 -d redis

### Connect to Redis
docker exec -it redis-container redis-cli

### Test the container
set mykey "Hello Redis"
get mykey
