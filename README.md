# Asgard

A library for simple schema-first authorization on top of GraphQL-Java.

## Usage

Add dependency via JitPack:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.deffunio.asgard</groupId>
    <artifactId>asgard-core</artifactId>
    <version>0.2.0</version>
</dependency>
```

Then add the following GraphQL directive:
```graphql
directive @authorize(roles: [String!]) repeatable on FIELD_DEFINITION
```

Use this directive in your schema:
```graphql
type Query {
  favoriteActors: [Actor!] @authorize(roles: ["USER"])
}
```

And finally add the following to the RuntimeWiring builder, for Micronaut:
```java
RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .fieldVisibility(MicronautComponentsFactory.authorizeFieldVisibility(securityService));
```
for Spring:
```java
RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .fieldVisibility(SpringComponentsFactory.authorizeFieldVisibility());
```
