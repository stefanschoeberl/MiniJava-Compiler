FROM gradle:6.4.1-jdk8 AS build

WORKDIR /home/gradle/bin
RUN ["wget", "https://github.com/WebAssembly/wabt/releases/download/1.0.15/wabt-1.0.15-linux.tar.gz"]
RUN ["tar", "xf", "wabt-1.0.15-linux.tar.gz"]
ENV PATH="${PATH}:/home/gradle/bin/wabt-1.0.15"

WORKDIR /home/gradle/project
COPY . /home/gradle/project/
RUN ["gradle", "demo-browser:build"]

FROM httpd:2.4.43
RUN echo "application/wasm                                wasm" >> /usr/local/apache2/conf/mime.types

COPY --from=build /home/gradle/project/demo-browser/build/dist /usr/local/apache2/htdocs
