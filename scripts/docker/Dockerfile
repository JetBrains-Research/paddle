FROM ubuntu:20.04 as downloader

ARG VERSION='0.4.4'

RUN apt-get update && apt-get -y install wget

RUN wget -O /paddle "https://github.com/TanVD/paddle/releases/download/v${VERSION}/paddle-${VERSION}-linux" && chmod +x /paddle

FROM ubuntu:20.04 as paddle

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]

FROM python:2.7-buster as paddle-py-2-7

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]

FROM python:3.7-buster as paddle-py-3-7

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]

FROM python:3.8-buster as paddle-py-3-8

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]

FROM python:3.9-buster as paddle-py-3-9

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]

FROM python:3.10-buster as paddle-py-3-10

RUN useradd -ms /bin/bash paddle
USER paddle
WORKDIR /home/paddle

COPY --from=downloader /paddle /home/paddle/paddle

ENTRYPOINT ["/home/paddle/paddle"]
