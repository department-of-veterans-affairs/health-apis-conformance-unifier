FROM ghcr.io/department-of-veterans-affairs/health-apis-docker-octopus/dev-tools:mvn-3.8-jdk-17

COPY maven/ /opt/va
RUN chmod 700 /opt/va/*sh
ENTRYPOINT ["/opt/va/entrypoint.sh"]
CMD []
