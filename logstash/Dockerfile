FROM logstash:7.12.1

LABEL authors="qc200"

WORKDIR /
COPY logstash.yml /usr/share/logstash/config/logstash.yml
COPY logstash.conf /usr/share/logstash/conf.d/logstash.conf
EXPOSE 9601

