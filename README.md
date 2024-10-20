# PhotonDb
Java server for accessing [Photon](https://github.com/phot-on/photon)'s mongodb storage

# Building
## With docker
1. docker build . -t photon-db
2. docker run -d photon-db
## Without docker
1. ./mvnw package -DskipTests
2. java -jar targets/PhotonDB-*.jar
