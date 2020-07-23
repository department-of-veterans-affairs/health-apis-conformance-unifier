FROM vasdvp/health-apis-dev-tools:mvn-3.6-jdk-14

COPY maven/ /opt/va
RUN chmod 700 /opt/va/*sh
ENTRYPOINT ["/opt/va/entrypoint.sh"]
CMD []
