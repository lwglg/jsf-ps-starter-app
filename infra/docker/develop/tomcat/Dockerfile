# Use the official Tomcat image
FROM tomcat:8.5.6-jdk11

# Remove the default webapp to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy your application WAR file into the webapps directory
# Ensure your .war file is in the same directory as this Dockerfile
COPY target/your-app.war /usr/local/tomcat/webapps/ROOT.war

# Expose the default Tomcat port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
