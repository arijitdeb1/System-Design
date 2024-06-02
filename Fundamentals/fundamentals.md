# Fundamentals of System Design 
1. Ask question to capture - 
   * Functional Requirements.
   * Non Functional Requirements.
   * System Constraints.
2. Define the System APIs
3. Create a Software Architecture Diagram ro fulfill the Functional requirements.
4. Refine the Software Architecture Diagram to address the Non Functional requirements.


## Clarify the Requirements
**`Features/Functional Requirement`**
- describe the system behavior - "*what the system must do*"
- tied to the objective of the system
- what are the input, user actions, events to the System.
- what will be the output of the System.
- find all the actors or participant resources of the system
- capture all possible use cases/scenarios
- describe the use case flow of events and corresponding actions using a **Sequence** flow diagram. 

**`Quality Attributes/Non Functional Requirements`**
- System properties that "_system must have_"
- examples :
    - Scalability
    - Availability/ Fault Tolerance
    - Reliability / Durability
    - Security
    - Performance 
    - Partition Tolerance etc
    
**`System Constraints/Limitations or Boundaries`**
- Time constraints/deadlines
- Resource constraint
- Compliance constraints
- Any custom constraints etc.    

**`Fault Tolerance`**
- Failure Prevention, Eliminate any Single Point of Failure.
- Failure Detection and Isolation, Monitoring capability, 
- Recovery, Restart, Retry or Rollback


**`Service Level Agreements`**

- [aws](https://aws.amazon.com/legal/service-level-agreements/?aws-sla-cards.sort-by=item.additionalFields.serviceNameLower&aws-sla-cards.sort-order=asc&awsf.tech-category-filter=*all)
- [google cloud](https://cloud.google.com/terms/sla)

## API Design
1. Identifying Entities 
       
       example: user, 
                post, 
                image, 
                comment
                
2. Mapping Entities to URIs 

        example: /users, 
                  /users/{user-id}, 
                  /posts, 
                  /posts/{post-id}, 
                  /posts/{post-id}/images, 
                  /posts/{post-id}/images/{image-id}

3. Defining Resource representation.

        example: GET /posts/{post-id}
4. Assigning HTTP methods to Operations and Resources.

## Load Balancer


## Message Broker

## API Gateway
Netflix Zuul, Amazon API Gateway, Apigee
## Content Delivery Network(CDN)
Cloudflare, Fastly, Akamai

## Data Storage
| Relational Database | Non Relational Database | 
| ----- | :---------: | 
| - [ ] Structured schema - Table with pre-defined set of columns and data types | No pre-defined Schema and can be altered as needed |
| Ability to form complex and flexible queries joining multiple tables | Don't need Table to store data and support data structures like List, Map, Arrays etc |
| ACID transaction guarantees | Faster Reads/Queries |
| Changing pe-defined schema at later point results in maintenance overhead | Analysis of data becomes hard due to flexible schema|
| - [x] Hard to Scale | Joining multiple group of data is hard |
| Slower Read operations |  |

## Relational Database

## Non Relational Database
1. Key/Value Store - Redis, Aerospike, Amazon DynamoDB
2. Document Store - Cassandra, MongoDB
3. Graph Database - Amazon Neptune, NEO4J

## Data Availability and Scalability
- database Indexing
- database Replication
- database Partition/Sharding

## CAP Theorem

## Object Store vs Block Store

## Architecture Pattern
- Multi Tier Architecture
- Multi Layer Architecture
- Microservice Architecture
- Event Driven Architecture(Event Sourcing Pattern, CQRS pattern)

## Big Data Architecture
- Big data processing strategies (Batch, Stream)
- Lambda Architec   ture 
 
