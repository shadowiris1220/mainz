FROM openjdk:11
COPY /target/universal/stage/bin /home/elbing/bin
COPY /target/universal/stage/lib /home/elbing/lib
ENTRYPOINT ["home/elbing/bin/elbing"]
