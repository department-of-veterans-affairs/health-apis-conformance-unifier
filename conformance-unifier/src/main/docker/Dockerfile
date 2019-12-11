FROM vasdvp/health-apis-maven:3.6-jdk-12

COPY maven/ /opt/va
RUN chmod 700 /opt/va/*sh
ENTRYPOINT ["/opt/va/entrypoint.sh"]
CMD []
