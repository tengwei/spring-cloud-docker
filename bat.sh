#!/usr/bin/env bash
java -jar ./microservice-consumer-movie-ribbon-hystrix/target/microservice-consumer-movie-ribbon-hystrix-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha1 &
java -jar ./microservice-consumer-movie-ribbon-hystrix/target/microservice-consumer-movie-ribbon-hystrix-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha2 &

java -jar ./microservice-discovery-eureka/target/microservice-discovery-eureka-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha1 &
java -jar ./microservice-discovery-eureka/target/microservice-discovery-eureka-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha2 &

java -jar ./microservice-gateway-zuul/target/microservice-gateway-zuul-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha1 &
java -jar ./microservice-gateway-zuul/target/microservice-gateway-zuul-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha2 &

java -jar ./microservice-hystrix-turbine/target/microservice-hystrix-turbine-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha1 &
#java -jar ./microservice-hystrix-turbine/target/microservice-hystrix-turbine-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha2 &

java -jar ./microservice-provider-user/target/microservice-provider-user-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha1 &
java -jar ./microservice-provider-user/target/microservice-provider-user-0.0.1-SNAPSHOT.jar --spring.profiles.active=ha2 &