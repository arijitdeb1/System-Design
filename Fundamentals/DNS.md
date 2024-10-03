# Domain Name Server(DNS)

![ScreenShot](/images/dns.png?raw=true) 


1. Request for a web page like www.example.com, DNS Resolver will try to look for corresponding IP address in it's cache.
2. If not found in cache, will send request to Root Nameserver.
3. Root Nameserver sends back the location of corresponding Top Level Domain(TLD) nameservers.(eg: .com, .net etc).
4. DNS resolver communicates with the Top Level Domain(TLD) nameservers.
5. TLD nameserver sends back the location of Authoritative Name Servers(ANS) which holds actual mapping between domain and corresponding IP address.
6. Resolver request ANS to provide the mapped IP address.
7. ANS respond back the resolver with the IP Address of the requested web page.
8. On receiving the response from Authoritative Name Server, the resolver will cache the response for specified TTL(Time To Live)


## DNS Resolver/Recursive Resolver
* Generally ISP(Internet Service Providers) or even Routers acts a resolver
* use `ipconfig/all` to find the DNS Resolver for ur internet connection.
* Below image is from local wifi setup where router itself is the DNS provider.
![ScreenShot](/images/ipconfig.PNG?raw=true) 
* If required, it can be changed to well known DNS Resolvers - 
          Google DNS Resolver [8.8.8.8]
          Cloudflare DNS Resolver [1.1.1.1]
* You can also implement your own DNS Resolver.
* DNS resolver caches all response.

## Root Nameserver
* There are 13 DNS root nameserver

![ScreenShot](/images/rootNS.PNG?raw=true) 

* 13 root nameservers doesn't mean there are only 13 server or physical machines but there are multiple physical servers all over the world broadcasting behind the same IP as root nameserver using `Anycast`.
* Root nameserver hold the details of all TLD nameservers and redirect the request based on extension of domain(.com,.net,.org).
* The Root nameserver are overseen by a nonprofit org called the `Internet Corporation for Assigned Names and Numbers(ICANN) `
* ### Anycast
  * Anycast is a routing technique where many hosts have same IP address.
  * Client tries to reach that IP address are routed to nearest host.

## Top Level Domain Nameserver
* A TLD nameserver maintains information for al the domain names that share a common domain extension, such as .com,.net.
* For example, a .com TLD nameserver contains information for every website that ends in .com.
* Management of TLD nameservers is handled by the `Internet Assigned Numbers Authority(IANA)`, which is a branch of ICANN.

## Authoritative Nameserver
* When a new domain is registered(with GoDaddy etc.),ANS holds the actual mapping of domain name and IP address of server/load balancer.
* The ANS contains information of the domain name it serves(e.g. google.com) and it can provide a recursive resolver with the IP address of the server found in the DNS `A Record`, or if the domain has a `CNAME record(alias)` it will provide the recursive resolver with an alias domain, at which point the recursive resolver will have to perform a whole new DNS lookup to procure a record from an ANS.
* `A Record` denotes the IPv4 address of a given domain.
  
  Example of a `A record`:

    ![ScreenShot](/images/Arecord.PNG?raw=true)

* `AAAA Record` denotes the IPv6 address of a given domain.
* Suppose blog.example.com has a `CNAME record`(canonical name) with a value of example.com (without the "blog"). This means when a DNS server hits the DNS records for `blog.example.com`, it actually triggers another DNS lookup to example.com, returning example.com’s IP address via its `A record`. In this case we would say that example.com is the canonical name (or true name) of blog.example.com.
* The `CNAME record` only points the client to the same IP address as the root domain. Once the client hits that IP address, the web server will still handle the URL accordingly. So for instance, blog.example.com might have a CNAME that points to example.com, directing the client to example.com’s IP address. But when the client actually connects to that IP address, the web server will look at the URL, see that it is blog.example.com, and deliver the blog page rather than the home page.

    Example of a `CNAME record`:
    ![ScreenShot](/images/cname.PNG?raw=true) 

A record resolves domain name to an IP address(IPv4)
AAAA record resolves domain name to IP address(IPv6)
CNAME/canoical name - when u type example.com will redirect to www.example.com

computer read domain name from right to left
www.example.com.
www=subdomain
example=2nd level domain
.com=top level domain
.= root domain

subdomains are also used when a website has a different services
running on the same server and using the same IP address.
example.com has an FTP service.
create a subdomain = ftp.example.com for the FTP service and create a
CNAME

the MX(mail exchanger) record is used for email

the SOA(start of authority) record stores administrative information about the DNS zone

a DNS zone is a section of a domain name space that a certain administrator has been delegated
control over

The NS(Name server) provides the name of the authorization name server within a domain
the name server contains all the DNS records necessary for users to find a computer or server on a local network
or on the internet. It is a final authority in a DNS hierarchy.
A NS record will list two name servers a primary and secondary

The SRV(service record) points to a server and a service by incuding a port number

The PTR or pointer record
this is reverse of A or quad A record. they resolve IP addressess to domain names
PTR records are attached to an email n used to prevent email spam.
So whenever an email is received, the email server uses the PTR record to make sure the sendeer
is authentic by matching the domain name in the email with its authentic IP address.
this is known as reverse DNS lookup. But if email that is sent doesn't match with it;s correct
and authentic IP address, the email will be flagged as spam