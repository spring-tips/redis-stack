# Bootiful Redis Stack

You'll need to add the following types to your Maven build: 

```xml 
    
    
    <repositories>
        <repository>
        <id>snapshots-repo</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
        </repository>
        </repositories>
    <dependencies>
    <dependency>
      <groupId>com.redis.om</groupId>
      <artifactId>redis-om-spring</artifactId>
      <version>0.3.0-SNAPSHOT</version>
    </dependency>
```

Run the Docker image for Redis Stack 



```shell 
docker run -p 6379:6379 --name redismod redislabs/redismod:edge
```