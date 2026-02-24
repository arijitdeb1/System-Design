## **Idempotent api**

`how to avoid duplicate requests in api`

3 stages of failure between client server communication,

1. request couldn't reach server
2. request reached server but server couldn't process it due to some error.
3. request reached server, server processed it but response couldn't reach client.

`how to handle these failures?`

1. request couldn't reach server
    - client can retry the request
    - client can use idempotent api
    - client can use unique request id for each request and server can check if the request is duplicate or not.
    - client can use exponential backoff algorithm to retry the request.
    - client can use circuit breaker pattern to avoid sending request to server if server is down.

2. request reached server but server couldn't process it due to some error.
    - client can retry the request
    - client can use idempotent api
    - client can use unique request id for each request and server can check if the request is duplicate or not.
    - client can use exponential backoff algorithm to retry the request.
    - client can use circuit breaker pattern to avoid sending request to server if server is down.

3. request reached server, server processed it but response couldn't reach client.
    - client can retry the request
    - client can use idempotent api
    - client can use unique request id for each request and server can check if the request is duplicate or not.
    - client can use exponential backoff algorithm to retry the request.
    - client can use circuit breaker pattern to avoid sending request to server if server is down.

`Idempotent api`:
    - An idempotent HTTP method is an HTTP method that can be called many times without different outcomes. It would not matter if the method is called only once, or ten times over. The result should be the same.
    - GET, PUT, DELETE are idempotent methods.
    - POST and PATCH is not idempotent method because it creates a new resource every time it is called.
    - If a request is idempotent, then it can be retried without any side effects.
    - If a request is not idempotent, then it can't be retried as it may cause side effects.

`How to create idempotent api?`
    - Use unique request id for each request and server can check if the request is duplicate or not.
    - Use exponential backoff algorithm to retry the request.
    - Use circuit breaker pattern to avoid sending request to server if server is down.
    - Use idempotent api like GET, PUT, DELETE.

`Typical payment gateway example`:
    - User makes a payment request to payment gateway.
    - Payment gateway processes the request and sends the response back to user.
    - If the response couldn't reach user, user can retry the request.
    - If the response reached user but user couldn't process it, user can retry the request.
    - If the response reached user but user couldn't process it, user can retry the request.
    
`How to make above example idempotent?`
    - Use unique request id for each request and server can check if the request is duplicate or not.
    - if duplicate request is received, server can return the response of the previous request.
    - if not duplicate request is received, but server couldn't process it, user can retry the request.
    
 `How user will get the unique request id?`
        - user can generate the unique request id and send it in the request.
        - server can generate the unique request id and send it in the response.
    - Use exponential backoff algorithm to retry the request.
    - Use circuit breaker pattern to avoid sending request to server if server is down.
    - Use idempotent api like GET, PUT, DELETE.

`How stripe payment gateway handles duplicate requests?`
    - Stripe payment gateway uses unique request id for each request.
    - If duplicate request is received, stripe returns the response of the previous request.
    - If not duplicate request is received, but stripe couldn't process it, user can retry the request.
    - Stripe uses exponential backoff algorithm to retry the request.
    - Stripe uses circuit breaker pattern to avoid sending request to server if server is down.
    - Stripe uses idempotent api like GET, PUT, DELETE.

`What fields are considered by Stripe to generate unique request id?`
    - user id
    - payment id
    - amount
    - currency
    - payment method
    - payment gateway
    - timestamp
    - random number

`How stripe generates unique request id? Explain in detail with example.`
    - Stripe generates unique request id by combining the above fields.
    - user id - 123
    - payment id - 456
    - amount - 100
    - currency - USD
    - payment method - credit card
    - payment gateway - stripe
    - timestamp - 2022-01-01 10:00:00
    - random number - 123456
    - unique request id = 123-456-100-USD-credit card-stripe-2022-01-01 10:00:00-123456

`When the unique request id is generated?`
    - Unique request id is generated when the request is received by the server.
    - Unique request id is generated before processing the request.
    - Unique request id is generated before sending the response.

`How long to store the unique request id?`
    - Unique request id is stored until the response is sent back to the user.
    - Unique request id is stored until the response is received by the user.
    - Unique request id is stored until the response is processed by the user.

Idempotency keys are typically stored in a key-value storage system on the server side. Common choices include:

**Redis**: Known for its speed and efficiency in handling key-value pairs1.
**DynamoDB**: A scalable option often used in serverless architectures1.
These systems ensure that each key is unique and can be quickly retrieved to check if a request has already been processed, preventing duplicate operations.


They store the idempotency keys with an in-memory database on the server side.

And cache the server response after a request gets processed successfully.

So the in-memory database gets queried to check whether a request has been processed.

They process a request only if it's new and then store its idempotency key in the database.

Otherwise, the cached response gets returned. This means the request was processed earlier.

Also they roll back a transaction using the ACID database when a server error occurs.

Besides they remove the idempotency keys from the in-memory database after 24 hours.

It helps to reduce storage costs and gives enough time to retry failed requests.

Put another way, an idempotency key could be reused after that period.

So they use the exponential backoff algorithm.

That means the client adds an extra delay to retry after each failed request.

Besides a failed server could experience a thundering herd problem when many clients try to reconnect at once.

So they use jitter to add randomness to the client’s waiting time before a retry.



https://newsletter.systemdesign.one/p/idempotent-api
https://www.youtube.com/watch?v=XAccGbtl3Z8
https://www.youtube.com/watch?v=J2IcD9FZvZU
    
