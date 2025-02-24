## Internals:

A collection of **nodes** that together manage the data and provide search and indexing capabilities.

- All nodes in a cluster share the same cluster name and can discover each other automatically.
- Roles of nodes:
  `Master Node`: Manages cluster-wide operations like creating or deleting indices and tracking cluster metadata.
  `Data Node`: Stores data and executes data-related operations like CRUD, search, and aggregations.
  `Ingest Node`: Preprocesses documents before indexing.
  `Coordinator Node`: Distributes search and indexing requests without holding data.
  `Machine Learning Node`: Executes machine learning jobs.


You are managing a product catalog with the following data:

| Product ID | Name          | Category     | Price  |
|------------|---------------|--------------|--------|
| 1          | Laptop        | Electronics  | 1000   |
| 2          | Smartphone    | Electronics  | 700    |
| 3          | Running Shoes | Footwear     | 120    |
| 4          | Microwave     | Electronics  | 150    |
| 5          | Sneakers      | Footwear     | 100    |

You want to store and search this data efficiently in Elasticsearch.

### Index:
An `index` is like a database table or namespace that organizes data in Elasticsearch. In this case:

- Index Name: **products_catalog**
- Contains all the product data.
- Internally, an index is divided into `shards`.

### Shard:
A `shard` is a subset of the data in an `index`. Elasticsearch automatically divides the data into shards to allow parallel processing.

- Let's say products_catalog is configured with 3 primary shards.
- Elasticsearch uses a hashing algorithm on the document ID to decide which shard will store a document.
- Example of data distribution among 3 shards:
  Shard 0: Product IDs 1, 4
  Shard 1: Product IDs 2, 5
  Shard 2: Product ID 3
- Each shard is an independent `Lucene` index.

### Replica:
A `replica` is a copy of a shard, providing fault tolerance and load balancing.

### Segment:
Each `shard` contains `segments`, which are immutable chunks of data stored by `Lucene`.

- Segments are created when new data is indexed.
- Over time, smaller segments are merged into larger ones to optimize search performance.
- Example:
      Shard 0 stores Product IDs 1 and 4. It might contain:
      Segment A: Product ID 1 (Laptop)
      Segment B: Product ID 4 (Microwave)
- When Elasticsearch merges segments:
      New Segment C: Contains both Product IDs 1 and 4.


### Flow Example:
1. `Indexing`:

- You add a document: `{ "Product ID": 6, "Name": "Tablet", "Category": "Electronics", "Price": 400 }.`
- Elasticsearch:
* Hashes the document ID (6).
* Determines it belongs to Shard 1. 
* Writes the document to a new segment in Shard 1.
* Synchronizes the document with Replica 1.

2. `Search`:

- Query: "`Find all products in the 'Electronics' category.`"
- Elasticsearch:
* Sends the query to all primary and replica shards.
* Each shard processes the query locally and returns results.
* Results are aggregated at the `coordinating node` and sent to the client.
