# How To Choose The Right Database For Your Application
## Types of Databases

### **`Key Value Database`** 
- Redis, Memcached
- Fast read and write
- Read from memory
- Used for caching, pub/sub, leaderboards
- Use cases - 
  - [How Twitter uses Redis to scale]()
  - [How GitHub uses Redis to scale]()
  - [How key value data store scales using consistent hashing?]()


### **`Wide Column Store`**
- Cassandra, Hbase
- Distributed, scalable 
- Used for time series data, IOT, sensor data
- Schemaless, without joins
- Write heavy workloads,
  * it uses LSM(Log Structured Merge) tree for write operations which buffers writes in memory and periodically flushes to disk making writes faster.
  * Read operations are slower compared to write operations as data is stored in multiple column families or partitions.
- Use cases - 
  - [How Netflix uses Cassandra to scale](https://netflixtechblog.com/scaling-time-series-data-storage-part-i-ec2b6d44ba39)
  - [How Cassandra is used for time series data?]()
  - [How does it provide better compression?]()
  - [Why column oriented storage is more suitable for analytical scenarios]()

### **`Document Store`**
- Mongodb, Couchdb, Dynamodb
- Schemaless, flexible, without joins
- Related data is stored together in single document
- Used for content management, blogging platforms
- Store data in JSON format    
- Read heavy workloads
- Write operations are slower compared to read operations as data is stored in JSON format and needs to be parsed before writing to disk.
- Key value pair is stored in a document and documents are stored in a collection. (example: user collection, product collection)
- Document store is a collection of documents and each document is a collection of key value pairs.
- Not good for data which is updated frequently as it needs to update all the documents in the collection.
- Use cases -
  - [How MongoDB is used for content management?]()


### **`Relational Database`** 
- Mysql, Postgresql, Oracle
- ACID properties
- Used for financial transactions, banking, e-commerce
- Scaling is difficult as it's not distributed
- Use case - 
  - [How cockroachdb scales?]()
  - [What are the challenges of distributing a relational database?]()

### **`Graph Database`**
- Neo4j, Arangodb
- Used for social networks, recommendation engines
- Nodes are entities/data
- Edges are relationships
- Traversing relationships is faster
- Use cases - 
  - [How Graph database verifies email addresses?, ip addresses?, fraud detection?]()
  - [How Facebook uses graph database to recommendations?]()

    
### **`Search Engine`**
- Elasticsearch, Solr
- Used for full text search, log analysis, type ahead suggestions
- Data is stored in documents identified by unique keys
- Inverted index stores the unique keys for all documents containing a given data/word.
![ScreenShot](/images/elasticsearch.PNG?raw=true)
- Use cases - 
  - [How Elasticsearch is used for log analysis?]()
  - [How Solr is used for type ahead suggestions?]()


## Comparison of Row-Oriented Database and Column-Oriented Database

| Feature                | Row-Oriented Database                      | Column-Oriented Database                                                                                            |
|------------------------|--------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Type of workload       | Transactional                              | Analytical                                                                                                          |
| Storage                | Store the complete row in a database block | Store the complete column and records under it in a database block                                                  |
| Accessibility use case | Need to fetch all details of a user        | Get the temperatures of a place within a date window                                                                |
| Compression            | Less efficient                             | More efficient - since same type of data gets persisted in a specific DB block, DB can apply compression algo on it |
| When to Use            | when you need to manage structured data with complex relationships and ensure ACID compliance for transactions. | when managing large volumes of data for analytical processing where read performance, data compression, and column-based queries are prioritized.                                                                             |


## Comparison of Cassandra , HBase and Traditional Columnar Database(Amazon Redshift, Google BigQuery)

| Feature      | Cassandra                                                                                                                                        | HBase                                                                                       | Traditional Columnar Database                                                                                                                                                                        |
|--------------|--------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Storage      | Data is stored based on a primary key, which consists of a partition key and optional clustering columns to organize data within partitions      | Similar to Cassandra                                                                        | Stores data in a columnar format, which means data for each column is stored together. This allows for high performance in analytical queries that involve operations on columns(e.g., aggregations) |
| Architecture | No single point of failure. Each node in a Cassandra cluster is equal and data is replicated across multiple nodes for fault tolerance           | Uses a master node for coordinating operations and multiple region servers for data storage | Can be deployed on a single node or distributed system.                                                                                                                                              |
| Consistency  | Eventual Consistency : supports tunable consistency levels, allowing trade-offs between consistency and availability                             | Strong Consistency : Ensures strong consistency across reads and writes                     | Strong Consistency : Ensures strong consistency across reads and writes                                                                                                                              |
| Use case     | Suitable for real-time applications that require high availability and scalability, such as IoT, social media analytics , recommendation engines | Used in conjunction with Hadoop for large-scale data processing and analytics               | Primarily used for data warehousing and BI applications, where complex queries and aggregations over large datasets are common                                                                       |


## Comparison of Relational Database and Document Store

| Feature     | Relational Database      | Document Store                                                                                                      |
|-------------|--------------------------|---------------------------------------------------------------------------------------------------------------------|
| Schema      | Fixed schema             | Flexible schema                                                                                                     |
| Data Model  | Normalized data model    | Denormalized data model                                                                                             |
| Querying    | SQL queries              | JSON queries                                                                                                        |
| Performance | Slower read and write operations | Faster read operations compared to write operations                                                                 |
| When to Use | when you need to manage structured data with complex relationships and ensure ACID compliance for transactions.                         | when you need to manage semi-structured data with a flexible schema, making it ideal for content management systems and blogging platforms.                                                                              |

## Comparison of Database vs Data Warehouse vs Data Lake

| Feature     | Database                 | Data Warehouse                                                                                                                                                                       | Data Lake                                                                                                           |
|-------------|--------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| Data        | Structured data          | Structured data                                                                                                                                                                      | Structured, semi-structured, and unstructured data                                                                   |
| Storage     | Store data in tables     | Store data in tables                                                                                                                                                                 | Store data in raw format                                                                                            |
| Querying    | SQL queries              | SQL queries                                                                                                                                                                          | SQL queries, NoSQL queries, and big data processing queries                                                         |
| Use case    | when you need to manage structured data with complex relationships and ensure ACID compliance for transactions. | when you need to manage large volumes of structured data for analytical processing and reporting where read performance, data compression, and column-based queries are prioritized. | when you need to manage large volumes of structured, semi-structured, and unstructured data for big data processing, machine learning, and advanced analytics. |

