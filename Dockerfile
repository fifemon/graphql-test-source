FROM clojure:openjdk-8-lein as builder
WORKDIR /build
# first copy just project.clj so we can cache deps
COPY project.clj ./
RUN lein deps
# now test and build
COPY src src/
COPY resources resources/
COPY test test/
RUN lein test && lein uberjar

FROM java:openjdk-8-jre

WORKDIR /app
COPY --from=builder /build/target/*-standalone.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]


