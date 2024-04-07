# Design a Payment System

Let's design a Payment System for Card payment. A Card payment system comprises of various actors/components as explained below -

**Customer** - _The owner of the Credit Card or the person who initiated the Credit Card transaction._

**Merchant** - _A business that sells goods and services to customers and accept Credit Card as payment._

**Acquiring Bank** - _A bank that processes Credit Card payment on behalf of the merchant. Amount debited from customer will be credited here until final settlement with seller._
 
**Issuer Bank** - _A bank that has issued the Credit Card to the customer or the bank to which the Credit Card is registered. The Issuing bank transfer money for purchases to the Acquiring bank._

**Card Network/Association/Scheme** - _This association set interchange rates and qualification guidelines. The Acquiring Bank validates the Credit Card and corresponding transaction by communicating to this association._

**Payment Service Provider(PSP)** - Third party components that facilitate business in processing payments safely and securely. example: Stripe, Paypal etc

**Pay-In Flow** - _A payment system receives money from customers on behalf of sellers._
**Pay-Out Flow** - _A payment system sends money to sellers around the world._

**Ledger** -  The ledger keeps a financial record of the payment transaction. For example, when a user pays the seller $1, we record it as debit $1 from a user and credit $1 to the seller. The ledger system is very important in post-payment analysis, such as calculating the total revenue of the e-commerce website or forecasting future revenue. 

**Wallet** -  The wallet keeps the account balance of the merchant. It may also record how much a given user has paid in total and how much seller is to receive for an order.

**Regulatory Compliance**  - Any payment system or application which is storing card details and integrating with banks to process the payment has to adhere to certain standards and regulations as below   
        - **Payment card Industry Data Security Standard(PCI DSS)** It is a security standard, if the seller sis compliant with this standard then payment page can be generated on the seller's page and it can store the Credit Card details else they need to redirect the request to payment gateways's payment page, which is compliant with this standard.
        - **Payment Services Directive(PSD2)** - To regulate the third-party financial providers. 
        - **Know Your Customer(KYC)** - To run verification account checks from time to time.
        - **Anti Money Laundering(AML)**  - To protect the account from criminal monetary activity.

**ISO-8583** - EFT switch message format for card payment processing.   


## Functional requirements
1. Allows multiple ways of payments.
2. Secure payment details.
3. Secure transactions.
4. Avoid duplicate payment.
5. For Payment processing we'll be using third party payment processor like Stripe, Paypal etc.
6. Perform Reconciliation to address any inconsistencies resulted due to any failures.



## Non-functional requirements
1. Overall transaction and corresponding response should be fast.
2. Handle failures and Time outs.
3. Highly Consistent and Available. No Eventual consistency i.e partition intolerant. Refer [CAP Theorem](#)
4. Highly scalable. 
 
## Design Considerations
1. We'll be building a Payment system by integrating with a Third party PSP. Overriding PSP and direct connectivity to Card Schemes will require the system to be compliant to certain regulations mentioned above and hence avoided.
2. The Payment system also doesn't have to store the Card details as it'll be stored and maintained by the PSP being compliant with all required regulations.
3. The Payment system comprises of multiple services to handle different kind of payments like card, internet, banking payments, UPI etc.
4. All communications to external PSPs will use SSL/TLS.
5. Any transaction details persisted by the Payment system should be encrypted.
6. Consistency and Availability will be prioritized over Partition Tolerance.
7. The Payment system and it's internal services will handle all transaction failures and perform reconciliation to rectify the inconsistencies. 
8. The Payment system will provide appropriate Payment Status to the End Customer and handle delays from PSPs efficiently.
9. The Payment system will use adequate retry strategies but avoid duplicate payments from end customers.
10. The Payment system will be equipped to handle large throughput or sudden burst of payment requests. 

## Architecture

![ScreenShot](/images/payment-system-architecture.PNG?raw=true) 



![ScreenShot](/images/payment-system-sequences.PNG?raw=true)


 
 1. Customer places an Order on the Merchant Website and hit Checkout for payment processing. 
 2. A payment event gets triggered.
 3. The **`Payment Ingestion`** service on behalf of Payment System accepts the event and generates a Transaction Id/Checkout Id for that payment event and store details in the [payment_event](##-data-modelling) table. A single payment event may contain several payment orders which gets split and registered to [payment_orders](##-data-modelling) with checkout_id as foreign key. Set payment status in database to NOT STARTED. This service also interact with third party external service for Risk Check to validate fraud transactions.
 4. The **`Payment Ingestion`** service will publish the checkout_id to kafka. 
 5. The **`Payment Executor`** service polls the checkout_id from kafka, 
 6. The **`Payment Executor`** service fetches all related details from database for that checkout_id. 
 7. The **`Payment Executor`** service calls Payment Service Provider(PSP) to register the payment with that checkout_id. Set payment status in database to IN PROGRESS.
 8. The PSP returns a token back to the payment service. A token is a UUID on the PSP side that uniquely identifies the payment registration.
 9. The  **`Payment Executor`** service register the token in database.
 10. The  **`Payment Executor`** service request for the PSP hosted payment form with the payment token and redirect URI after payment processing. 
 11. Customer will provide sensitive card details to the form and persisted by the PSP. The Payment Form is managed and provided by the Payment Gateway system or Third party PSP as it's compliant with PCI-DSS.
 12. PSP processes the payment for the details provided in the form and redirect the page to the redirected URL.
 13. Asynchronously, the PSP calls the payment service with the payment status via a webhook. The webhook is an URL on the payment system side that was registered with the PSP during the initial setup with the PSP. When the payment system receives payment events through the webhook, it extracts the payment status and updates the payment status field in database table. Set payment status in database to SUCCESS/FAILED. 
 
 As part of payment processing, the PSP perform below steps(not covered in the sequence flows)                
 1. PSP forwards the request to advanced verification services for Fraud and Risk prevention.
 2. Post verification, the Payment Gateway system or PSP forwards the payment details to Merchant Acquiring Bank for payment processing.
 3. The Acquiring bank captures the transaction details and routes the request to Card Network Schemes for basic validations.
 4. Once validated, the payment request is forwarded to the Card Issuing bank for approval.
 5. The Issuing bank validates the transaction and approves or declines the payment and the amount gets credited to Merchant Acquiring Bank instead of directly transferring to the seller. This process is called Pay-In.
 6. After payment is processed, the **`Payment Ingestion`** service updates the **`Wallet`** service to record how much money a given seller has and set wallet_updated field to true in database.
 7. After the wallet service has successfully updated the seller’s balance information, the **`Payment Ingestion`** service calls the **`Ledger`** service to update it and set ledger_updated field to true in database.
 8. During Pay-Out flow(at the end of day) the Merchant Acquiring Bank settles out all payment to seller.
 
 
## Architecture Deep-Dive and Details
  1. The Payment system contains following core components - 
     - Payment Ingestion Service - keeps payment-related data such as nonce, token, payment order, execution status, etc.
     - Payment Executor Service - integrates with PSP and executes the payment, track and update the execution status to system.
     - PSP Integration - Secure Payments and store payment status.
     - Wallet - Keeps Accounting balance of Merchant.
     - Ledger - Keeps all Accounting data, used for Analytics, Final Revenue, Auditing.
     
     
  
  2. Communications between internal services is **asynchronous** and use persistent queues like Kafka. This is - 
     - To avoid any tight coupling between services which may impact overall system Availability in case of failures in any of the dependent services.
     - To handle or balance out high throughput or request outbursts by sequentially processing the requests/messages from queues instead of catering all at once and overburdening the system.
     - Using persistent queues like Kafka ensures data retention and encryption.
     - System can be easily scaled as required to consume and process more messages from queues.
     
  3. Communications whose responses are mandatory to complete the transaction are **synchronous** in nature, example- invoking 'Risk-Check' service to validate the transaction. This also requires - 
     - Handle retries during network-failures, time-out or related issues.
     - Retries should be executed at exponential intervals(Exponential back off retry).
     - An adequate time-out will be defined in the service api settings based on the api performance and SLAs.
     - Retries for timed-out errors will have be idempotent and avoid duplicate payments from customers for same order.
     - Fallback mechanism will have to be defined to handle certain scenarios like "_if the transaction amount is less than 100 and Risk-Check apis are getting timed-out, Fallback handler can allowed the transaction to proceed_".
     - For errors that consistently failing even after multiple retries (eg: bad request/improper message format), corresponding request should be uploaded to Dead Letter Queue on Kafka to consumed and analyzed a later point of time. End customer to be notified about failed transaction.
     - When the target service is down, multiples retries will keep on failing. Use circuit breaker capability and forward the failed requests to a separate queue to be processed later. End customer need to be notified accordingly.
     
     A typical example where additional processing by PSP or Card Schemes is delaying the overall payment processing and how Payment System should handle such delays gracefully - 
     - PSP would return a pending status to Merchant website to display the same to end Customer.
     - PSP tracks the pending payment on our behalf, and notifies the payment service of any status update via the webhook the payment service registered with the PSP.
     - When the payment request is finally completed, the PSP calls the registered webhook mentioned above. The payment service updates its internal system and completes the shipment to the customer.
     
  4. For any Payment System, avoiding **duplicate payments** or handling **multiple clicks** of payment button is a prime concern. Below flow explains the problem and corresponding solution.
     
      ![ScreenShot](/images/duplicate-payment.PNG?raw=true)      
                     
     -  Customer makes a payment against certain product on Merchant website which gets registered by the Payment System and ultimately processed by the PSP.
     -  The payment confirmation from PSP, however, never make it up to the end Customer due to network issues.           
     -  The corresponding error or failure might prompt the Customer to re-submit the payment once again.
     
     Below flow describes how above situation can be handled with **Idempotency** i.e. from an api perspective an idempotent operation is one that has no additional effect if it gets called more than once with the same input request.    
         
        ![ScreenShot](/images/idempotency.PNG?raw=true)
        
     -  Payment System will register the payment and generate an UUID as payment_order_id/checkout_order_id.
     -  This UUID will be set to '_idempotency-key_' HTTP header while placing the payment request to PSP.
     -  PSP will register the payment against the '_idempotency-key_' from above and process the payment.
     -  If any error during reverse flow, either Payment System will retry or Customer will re-submit the payment.
     -  As payment and order details is same during retry and re-submit, same '_idempotency-key_' is posted to PSP.
     -  PSP tries to insert the payment request into database but will get a Unique Constraint error as the '_idempotency-key_' is already registered during first submission.
     -  PSP will respond back with the latest status of the previous request. 
     
     For scenarios where customer clicks the "pay" button quickly twice
     - when a user clicks "pay" an idempotency key or checkout_id is sent to the payment system as part of the HTTP request.
     - If multiple concurrent requests are detected with the same idempotency key, only one request is processed and the others receive the "429 Too Many Requests" status code. 
   
  5. All payment and card details need to **protected** or **encrypted** both at rest and on transit
     - Data at rest can be encrypted using third party software
     - Data on transit can be encrypted using SSL/TLS protocol and use HTTPS to transmit the data. Details available [here](#)
     - Implement adequate Access Control to allow data access to only authorized users
     - Handle software vulnerabilities and update software, OS and libraries regularly
     - Backup data regularly to avoid any accidental loss of data. 
     
  6. Data **Consistency** 
     - In a distributed environment, the communication between any two services can fail, causing data inconsistency.
     - To maintain data consistency between internal services, ensuring exactly-once processing by idempotency is very important. 
     - To maintain data consistency between the internal service and external service (PSP), we usually rely on Reconciliation. 
     - Data might be replicated among different database replicas to increase reliability. Then, replication lag could cause inconsistent data between the primary database and the replicas.
     - Ensure all replicas are always in-sync. We could use [consensus algorithms]() such as Paxos and Raft, or use consensus-based distributed databases such as YugabyteDB or CockroachDB.
     
  7. **Reconciliation**   
     -  Communication between the internal service and external service (PSP) may fail at any stage resulting in inconsistent state between systems.
     -  Reconciliation is a practice that periodically compares the states among related services in order to verify that they are in agreement. It is usually the last line of defense in the payment system.
     -  Every night the PSP or banks send a settlement file to their clients. The settlement file contains the balance of the bank account, together with all the transactions that took place on this bank account during the day.
     -  The payment system parses the settlement file and compares the details with the ledger system.
     -  Reconciliation is also used to verify that the payment system is internally consistent between Wallet and Ledger system.
     -  To fix mismatches found during reconciliation, we usually rely on the finance team to perform manual adjustments or automate using a defined process.
     
  8. **API Gateway**   
     - API Gateways need to be integrated with all services.
     - It'll authenticate the user, can do fraud detection and evaluate encryption 
     - It can monitor all transaction to services.
     
  9. **Load Balancers**  
     - Load balancer need to introduced between Merchant website and Payment system.
     - [Type of Load Balancer]() need to be evaluated based on load for on a payment system. IP hashing/consistent hashing is preferred for payment systems. 
     
  10. Scalibility of database - 
     - distribute/replicate the data across multiple DB servers following a master/slave architecture.
     - master can be used for write and slaves for read.
     - data can be [partitioned]() across multiple servers for quick access.
     - cache can be introduced infront of DB server for faster access. 
     - In-memory databases can be introduced for faster writes which later has to be synced with relational database. 
     - [Cassandra]() uses consensus algorithm for syncing which makes it suitable in scenarios where frequent syncing is required.
        
 
## APIs for payment service 
**POST /v1/payments**

This endpoint executes a payment event. As mentioned above, a single payment event may contain multiple payment orders. The request parameters contains below fields

| Field | Description | Type |  
| ----- | :---------: | ---: | 
| buyer_info | The details of buyer | json |
| seller_info | The details of seller | json |
| payment_info | details of type of payment like card, UPI, Netbanking etc | json |
| payment_orders | List of all orders and amount for that payment event | json | 



**GET /v1/payments/{:id}**

This endpoint returns the execution status of a single payment order based on payment_order_id or checkout_id.

## Data Modelling 
**payment_event**

| Column | Description | Type |
| ----- | :---------: | ---: |
| checkout_id(PK) | A globally unique id for that checkout | string | 
| buyer_info |  The details of buyer      |  string    |
| payment_token | This could be encrypted credit card information or a payment token. This value is PSP specific | string |
| payment_status | Status of payment | string | 


**payment_orders**

| Column | Description | Type |
| ----- | :---------: | ---: |
| payment_order_id(PK) | A globally unique id for that payment | string  |
| checkout_id(FK) | A globally unique id for that checkout | string |
| seller_account |  Details of seller      | string     | 
| amount | The transaction amount of the order | string |
| currency | The currency of the order | string |
| ledger_updated | Is ledger updated after payment processing | boolean |
| wallet_updated | Is wallet updated after payment processing | boolean |


## Back-of-Envelope Calculations 
Below data are just assumptions to replicate a real time scenarios 
 - 1 user does 10 transaction per month.
 - 500 million users are registered by the Merchant website
 - Total transactions in a month if 500 million users has actively made payments 
            500 million x 10 = 5 bil transaction in a month 
 - To complete 1 transaction, system has to execute 5 queries i.e. for 5 billion transactions, system need to execute 25 billion queries over a month. 
 - To evaluate the performance at per second level - 
            25 billion / (30 days x 100k seconds in a day) = 8k/seconds
            i.e 8k transactions per second.
            
 - A decent server with 64GB RAM and 8 core processor can process 1K - 5K write transactions per second. Decision of a DB server should taken based on Load/Stress testing.
 - A Postgres relational DB server can easily process 8k transactions in a second and would a suitable option for our use case.
 - Another factor is Data Volume , 1 write query persists 1kb of data, so 25 billion write queries will require
            25 billion * 1 kb = 25 terabytes of data over a month
 - If data has to be stored for 5 years - 
            25 terabytes per month * (5 year * 12 month) = 1.5k terabytes of data in 5 years.
 - Typical DB server can accommodate till petabyte of data, moreover above data will be distributed across multiple DB servers and old data can also be archived to some archive store.
 
 
 - To evaluate the required number of servers for a payment system, let's assume 
                1 user logs in atleast 2 times a day to the merchant website to place an order and do payment.
                1 such operation require atleast 5 api calls across all the components of a payment system.
                so 500 million users will make (500 million * 2 times * 5 api calls) = 5 billion api calls in a day i.e. 
                            5 billion / (24 * 60 * 60 )= 50000 api calls/second
 - 1 basic server can handle 1k api calls per second, so will require (50000/1k) = 50 servers
 - 1 api call may need to call 3 microservices to complete a processing, so (50 * 3) = 150 servers will be needed to support 500 million users.                                                          
                    
                        

 


