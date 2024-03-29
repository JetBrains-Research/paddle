FROM ubuntu:20.04 as downloader

ARG PADDLE_VERSION

RUN apt-get update && apt-get -y install wget
RUN wget -O /paddle.jar "https://github.com/JetBrains-Research/paddle/releases/download/v${PADDLE_VERSION}/paddle-${PADDLE_VERSION}-all.jar"

FROM python:3.10-bullseye as paddle-py-3-10

# Install OpenJDK-17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

COPY --from=downloader /paddle.jar /paddle.jar
RUN echo "java -jar /paddle.jar \$@" >> /bin/paddle && chmod +x /bin/paddle

ENTRYPOINT ["paddle"]

FROM python:3.9-bullseye as paddle-py-3-9

# Install OpenJDK-17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

COPY --from=downloader /paddle.jar /paddle.jar
RUN echo "java -jar /paddle.jar \$@" >> /bin/paddle && chmod +x /bin/paddle

ENTRYPOINT ["paddle"]

FROM python:3.8-bullseye as paddle-py-3-8

# Install OpenJDK-17
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

COPY --from=downloader /paddle.jar /paddle.jar
RUN echo "java -jar /paddle.jar \$@" >> /bin/paddle && chmod +x /bin/paddle

ENTRYPOINT ["paddle"]
