The scenario described above gives rise to two fundamental requirements for Dapper: ubiquitous deployment,
and continuous monitoring.

Three concrete design goals result
from these requirements:
• Low overhead: the tracing system should have negligible performance impact on running services. In
some highly optimized services even small monitoring overheads are easily noticeable, and might compel the deployment teams to turn the tracing system
off.
• Application-level transparency: programmers should not need to be aware of the tracing system.
A tracing infrastructure that relies on active collaboration from application-level developers in order
to function becomes extremely fragile, and is often broken due to instrumentation bugs or omissions,therefore violating the ubiquity requirement.

• Scalability: it needs to handle the size of Google’s
services and clusters for at least the next few years.

An additional design goal is for tracing data to be
available for analysis quickly after it is generated: ideally within a minute

True application-level transparency, possibly our most
challenging design goal, was achieved by restricting
Dapper’s core tracing instrumentation to a small corpus
of ubiquitous threading, control flow, and RPC library
code.

Making the system scalable and reducing performance overhead was facilitated by the use of `adaptive sampling`

The resulting system also includes code to collect traces, tools
to visualize them, and libraries and APIs (Application
Programming Interfaces) to analyze large collections of
traces. 

For example, we have found sampling to be necessary for low overhead, especially in highly optimized
Web services which tend to be quite latency sensitive.Perhaps somewhat more surprisingly, we have found that
a sample of just one out of thousands of requests provides sufficient information for many common uses of the trac
ing data.

Two classes of solutions have been proposed to aggregate this information so that one can associate all
record entries with a given initiator (e.g., RequestX in Figure 1)
`black-box` schemes are easier to implement because they don’t require changes to the application, but they
need more data to be accurate. `Annotation-based` schemes are more accurate but require modifications to the 
application code to add the necessary tags.

we model Dapper traces using `trees`, `spans`, and `annotations`.

A `tree` is a directed acyclic graph (DAG) where each node represents a `span`, which is a logical unit of work 
in the system. Each span has a unique identifier, the ID of the span that caused it, and the ID of the trace to
which it belongs. The root of the tree is the span that started the trace.
A `span` can have zero or more children, each of which is caused by the parent span. 
The tree structure allows us to represent the causal relationships between spans.
`trace ID` is a unique identifier for a trace, and is used to associate spans with the same trace.
`annotations` are key-value pairs that can be added to spans to provide additional information about the span.

In the context of Dapper, Google’s distributed tracing system, `sampling` refers to the process of selecting a 
subset of all requests to trace. This is done to reduce the overhead and storage requirements associated with 
tracing every single request in a large-scale system
When a request enters the system, Dapper decides whether to trace it based on a predefined sampling rate.
For example, if the sampling rate is set to 1%, only 1 out of ever