S3 Static Uploader
==================

S3 static uploader is a Maven plugin for uploading static content to AWS S3. A Java web application layout
is not required. 

Our plugin is inspired in S3 WebCache Maven Plugin (https://github.com/aro1976/aws-parent). Our use
case is different from the one used by their plugin, as we were looking for an automated way to deploy a
static site to AWS S3 from a maven build. We're not using S3's mini web server, so all of our resources
should be uploaded without any transformation in their names or layout structure.

The plugin will replicate the directory structure in S3, prefixing file names with directories above
it, starting in the configured input directory.

You may configure the metadata which will be generated for each file when uploaded to S3.

Also, you may configure the ACL which will set the permissions for each file. Note that this
is a restricted value mirroring the values provided by AWS in the enum CannedAccessControlList.

Finally, the parameter refreshExpiredObjects determines if unchanged objects with past 
expiration dates should be refreshed (metadata, mainly expiration date) for better
cache handling of those resources.
 
## Installation
Download the source code and run `mvn install` to add the plugin to your local repository. Also, you 
may reference it from Maven Central as the artifacts have been uploaded there too. Currently, the 
version available is 1.1.

## Setup
Add the following lines to your pom.xml.

    <properties>
        <aws.accessKey>myAccessKey</aws.accessKey>
        <aws.secretKey>mySecretKey</aws.secretKey>
        <aws.bucketName>myBucket</aws.bucketName>
    </properties>
    
Add the plugin to the build section of your pom.xml:

        <plugin>
            <groupId>io.pst.mojo</groupId>
            <artifactId>s3-static-uploader-plugin</artifactId>
            <configuration>
                <accessKey>${aws.accessKey}</accessKey>
                <secretKey>${aws.secretKey}</secretKey>
                <bucketName>${aws.bucketName}</bucketName>
                <refreshExpiredObjects>true</refreshExpiredObjects>
                <includes>
                    <include>
                        <bind>
                            <!-- Could be path expressions or reg. expressions -->
                            <pattern>%regex[([^\s]+(\.(?i)(jpg|png|gif|bmp|tif|pdf|swf|eps))$)]</pattern>
                            <metadataId>static</metadataId>
                        </bind>
                        <constraints>
                            <!-- Constraint for specific files -->
                            <bind>
                                <pattern>**/next*.png</pattern>
                                <metadataId>static-longlived</metadataId>
                            </bind>
                        </constraints>
                    </include>
                    <include>
                        <bind>
                            <pattern>%regex[([^\s]+(\.(?i)(html|css|js))$)]</pattern>
                            <metadataId>volatile</metadataId>
                        </bind>
                    </include>
                    <include>
                        <bind>
                            <!-- Extension less files -->
                            <pattern>%regex[^[^.]+$]</pattern>
                            <metadataId>volatile-naked</metadataId>
                        </bind>
                    </include>
                </includes>
                <excludes>
                    <exclude>WEB-INF/**</exclude>
                </excludes>
                <metadatas>
                    <metadata>
                        <id>static</id>
                        <cacheControl>public, max-age=31536000</cacheControl>
                        <secondsToExpire>31536000</secondsToExpire>
                        <contentEncoding>gzip</contentEncoding>
                        <cannedAcl>PublicRead</cannedAcl>
                    </metadata>
                    <metadata>
                        <id>static-longlived</id>
                        <cacheControl>public, max-age=315360000</cacheControl>
                        <secondsToExpire>315360000</secondsToExpire>
                        <contentEncoding>gzip</contentEncoding>
                        <cannedAcl>PublicRead</cannedAcl>
                    </metadata>
                    <metadata>
                        <id>volatile</id>
                        <cacheControl>private, max-age=86400</cacheControl>
                        <secondsToExpire>86400</secondsToExpire>
                        <contentEncoding>gzip</contentEncoding>
                        <cannedAcl>PublicRead</cannedAcl>
                    </metadata>
                    <metadata>
                        <id>volatile-naked</id>
                        <cacheControl>private, max-age=86400</cacheControl>
                        <secondsToExpire>86400</secondsToExpire>
                        <contentEncoding>gzip</contentEncoding>
                        <contentType>text/html</contentType>
                        <cannedAcl>PublicRead</cannedAcl>
                    </metadata>
                </metadatas>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>upload</goal>
                    </goals>
                    <phase>prepare-package</phase>
                </execution>
            </executions>
        </plugin>

## Usage
To upload your files to AWS S3 after completing setup there are two options:

1. Manual: Run `mvn s3-static-uploader:upload`.

2. Automated: The setup above is already configured to run during the build. Typically,
this is done in a profile for only certain environments.

## Hint
When using this plugin with a project without Java web application layout, packaged as a war and
without web.xml file, maven-war-plugin will complain about no web.xml file found when running mvn
install. You can configure maven-war-plugin to ignore this error:

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>2.3</version>
        <configuration>
            <failOnMissingWebXml>false</failOnMissingWebXml>
        </configuration>
    </plugin>   
    