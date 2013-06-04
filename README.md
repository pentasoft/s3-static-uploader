S3 Static Uploader
==================

S3 static uploader is a Maven plugin for uploading static content to AWS S3. A Java web application layout
is not required. 

This plugin is a rewrite of S3 WebCache Maven Plugin (https://github.com/aro1976/aws-parent). Our use
case is different from the one used by their plugin, as we were looking for an automated way to deploy a
static site to AWS S3 from a maven build. We're not using S3's mini web server, so all of our resources
should be uploaded without any transformation in there names or layout structure.

The plugin will replicate the directory structure in S3, prefixing file names with directories above
it, starting in the configured input directory.

The plugin will add the following HTTP headers to the uploaded resources:

- Content-Type: the plugin will guess the resource MIME type according to file extension.
- Cache-Control: public, s-maxage=315360000, max-age=315360000.
- Expires: 10 years after upload date.
- Content-Encoding: plain or gzip depending on contentEncoding plugin parameter.

The plugin will add the following ACL to the uploaded resources:

- PublicRead: Everyone open/download.
 
## Installation
Download the source code and run `mvn install` to add the plugin to your local repository.

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
        <version>0.0.1-SNAPSHOT</version>
        <configuration>
            <accessKey>${aws.accessKey}</accessKey>
            <secretKey>${aws.secretKey}</secretKey>
            <bucketName>${aws.bucketName}</bucketName>
            <contentEncoding>plain</contentEncoding>
            <includes>
                <include>**/*.gif</include>
                <include>**/*.jpg</include>
                <include>**/*.tif</include>
                <include>**/*.png</include>
                <include>**/*.pdf</include>
                <include>**/*.swf</include>
                <include>**/*.eps</include>
                <include>**/*.js</include>
                <include>**/*.css</include>
            </includes>
            <excludes>
                <exclude>WEB-INF/**</exclude>
            </excludes>
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

## Configuration parameters

- accessKey: AWS access key.
- secretKey: AWS secret key.
- bucketName: AWS S3 bucket name.
- includes: list of regular expressions matching files to include. 
- excludes: list of regular expressions matching files to exclude.
- outputDirectory: the directory where the webapp is built. Default value is: default-value="${project.build.directory}/${project.build.finalName}.
- inputDirectory: the directorry containing the files to be processed: Default value is: ${basedir}/src/main/webapp.
- tmpDirectory: the directory used for encoding files before uploading. Default value is: ${project.build.directory}/temp.
- contentEncoding: content encoding type. Could be plain or gzip. Default value is: gzip.
- extensionLessMimeType: MIME type for extension less files. Default value is text/html.

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

   

    
