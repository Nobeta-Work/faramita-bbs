FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.6

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin \
    install --batch analysis-smartcn