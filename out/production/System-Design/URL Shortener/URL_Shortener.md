

### Functional Requirements:

1. long url --> short url [https://bit.ly/3YXDfQO]
2. Given short url Redirect to long url
3. Support Alias i.e user provided Short URL
4. Support expiration

### Non Functional Requirements:

1. Consistency or Availability - (Eventually Consistent with Availability for this application)
2. Scalability - (100mil DAU, 1bil URLs)
3. Low Latency - redirection should be fast(200ms)
4. System need guarantee uniqueness of short codes in urls.

10^7 DAU * 5 Read/Write = 50^7 tx/day = 50^7/24*60*60= 5788 tx/second

1 thread in server = 200ms for one request = 5 requests in 1 second

50 threads in server = 50 threads can execute 250 requests in 1 second by 1 server

Number of servers = 5788/250 = 23 servers rqd to serve 100mil DAU


### Core Entities:

- Original URLs
- Short URLs
- User

### API and Interface:

**Create Short URLs form Long URLs**

**`REQUEST`**: 
`POST /urls 
{
    originalURL: "",
    alias: "",
    expirationTime:""
}`

**`RESPONSE`** - short URL

**Redirect to Original URL**

**`REQUEST`**: `GET {shortUrl}`

**`RESPONSE`**:
HTTP/1.1 302 Found 
Location: _Long Url_



### Data Flow:

- **URL**
  shortUrl,
  longUrl,
  currentTime,
  expirationTime,
  userId,
  alias

- **USER**
  userId,
  user_details 


### High Level Design:


1. User requests for a Short URl for a Long URl using the above mentioned API.
2. URL Shortener service will check in DB if the short code already exists, otherwise generate a new short URL and respond back.
3. If alias is provided, use it to generate short URL unless it's already been used
4. User requests to redirect for a short URL.
5. URL Shortener service will fetch the Long URL for the short URL and set response status as 302 and Location as Long URL.
   (other redirect status 301 will cache the Location hence avoiding direct hit to server which may not be useful if you need to run Analytics on the system)
6. If the expirationTime for the record has expired, respond back with error message and ask to generate a new short URL


* checking if short URL already exists for every Create request is an overkill especially when we're dealing with a million users daily.
  we can cache short URLs but still will be an extra lookup
* how to validate if alias name is not already taken? Try BloomFilter
* How to generate the Short URL efficiently? need a way to generate short URLs without any collisions and reducing the need for extra lookup.
* What are the different ways of generating unique alphanumeric 7 characters for a long URL with minimum collisions.

### Deep Dives: 

1. **`Uniqueness of short URLs`**
Let's try various techniques to produce unique URLs 
- Hashing the URL using MD5
- Convert the above Hash using Base 62 encoding
- Incremental Counter with Base62 Encoding
- 

