### Build

```sh
mvn package

rm ./keycloak/providers/*
cp ./target/nikohadouken*.jar ./keycloak/providers/
docker compose restart keycloak
```


Keycloak Classes
https://www.keycloak.org/docs-api/20.0.3/javadocs/allclasses-index.html