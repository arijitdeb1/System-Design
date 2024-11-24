## Functional Requirement -
- Book Ticket
- View Events
- Search Events

## Non Functional Requirement -
- in the context of `CAP theorem`, do we need to be consistent or available?
  Here, consistency is more important than availability.(no double booking)
- so consistency for booking tickets
- but high availability for viewing events and searching events


- `Read Write ratio`, is the system read heavy or write heavy?
- Here, it is read heavy, as the number of reads will be more than the number of writes.

- `Scalability` is important during surges from popular events.

- Low latency `Search` is important.

## Core Entities -
- Event
- Venue
- Performer
- Ticket

## API Design -
- GET /events/eventId - get event details, venue details, performer details and ticket details
- GET /search?query= - search events based on query

during booking ticket, we start with booking a seat and then proceed to payment.the window for booking a seat is 10 minutes. if the payment is not done within 10 minutes, the seat is released.

- POST /booking/reserve - reserve a seat for 10 minutes
  header : JWT session token
  body : {
   ticketId  
   }
- POST /booking/confirm - confirm the booking
  header : JWT session token
  body : {
   ticketId
   paymentId(stripe)
   }

## Database Design -
- Event Table [eventId, eventName, venueId, performerId, startTime, endTime, ticketId]
- Venue Table [venueId, venueName, venueLocation, seatMap]
- Performer Table [performerId, performerName, performerType]
- Ticket Table [ticketId, eventId, seatNumber, price, status,user, reservationTime]


considering the `CAP theorem`, we need to be consistent during booking tickets. so, we need to use `ACID` transactions.
Hence, we need to use `RDBMS` like `Postgres` for this use case.

a cron job will run every 10 minutes to release the seat if the payment is not done.
### problem statement -
there could be a time gap between 10 minutes completion and the cron job running. so, we need to handle this scenario.
- we need something bit more real time than cron job.
- we'll introduce a `Distributed Lock` like `Redis` to handle this scenario.
- so when a ticket is reserved, we'll set a key in redis with ticketId and value as the reservation time in the Distributed Lock.
- we'll set a TTL of 10 minutes for this key.
- once TTL expires, the key will be deleted.



## High Level Design -
![ScreenShot](/Designs/Ticketmaster/Ticketmaster_highlevel.PNG?raw=true)

  
## Deep Dive -
- For low latency search, we can use `Elasticsearch` for indexing and searching events.
- handle `Scalability` when there is a surge in popular events.Booking tickets might see huge surge in traffic.

### problem statement(1) -
how to keep the data in `Postgres` and `Elasticsearch` in sync?
- any time an event is created, updated or deleted, we need to update the data in `Elasticsearch` as well.
- another solution is to use `Kafka Connect` to stream data(CDC) from `Postgres` to `Elasticsearch`.

### problem statement(2) -
how to search popular events or events frequently searched?
- use AWS OpenSearch Service instead of Elasticsearch.
- OpenSearch caches the data on each node of ES cluster.
- another solution is to use `Redis` to cache the frequently searched events.
- another solution is to use `CDN` to cache the frequently searched events.

### problem statement(3) -
how to maintain the seatMap i.e. which seats are booked and which are available in real time?
- use `Long Polling` to get the real time updates.
- Long Polling is a http request that waits for a response like 30 seconds and if there is no update, it will send another request.
- it works only if the user is on the same page to get the real time updates.

- another solution is to use `WebSockets` to get the real time updates.
- WebSockets is a protocol that provides full-duplex communication channels over a single TCP connection.
- it works even if the user is on a different page to get the real time updates or in same page for longer duration.
- WebSockets is a better solution than Long Polling because it is more efficient and less resource intensive.

- another solution is to use `Server Sent Events(SSE)` to get the real time updates.
- SSE is a standard describing how servers can initiate data transmission towards browser clients once an initial client connection has been established.
- set up SSE between the client, API Gateway and server to get the real time updates.

### problem statement(4) -
how to keep booking tickets functional even if traffic is high inspite after scaling?
- use Virtual waiting queue/Priority Queue infront of booking tickets service
- service will pick the request from the queue based on priority and availability of the seat.
- response will be sent back to the user using the open SSE connection.
  
![ScreenShot](/Designs/Ticketmaster/Ticketmaster_deepdive.PNG?raw=true)






