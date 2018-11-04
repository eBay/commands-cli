# Travis-CI Integration

## File Descriptions

* [`.travis.yml`](../.travis.yml)  
  The travis job configuration file
* [`.travis/bintray-settings.xml`](bintray-settings.xml)  
  Maven settings file specifically for the deployment phase to Bintray (see [Deployment](#deployment))
* [`.travis/mvn`](mvn)  
  A wrapper script with options and flags for `mvn` command line tool which are commonly used in Travis-CI
* [`.travis/deploy.sh`](deploy.sh)  
  Script for running the deployment flow. Includes: upload artifacts to Bintray, sign the artifacts in Bintray, sync artifacts to Maven Central.
* [`.travis/bintray`](bintray)  
  A utility script for running Bintray version operations. Includes: sign, publish, sync

## Job Phases

### Install

Build the code, skip tests and such

### Test (i.e. `script`)

Run tests and collect code coverage.

### After Success

Upload code coverage to Codacy.

**Environment Variables**  

| Name                   | Required | Secure | Description |
| ---------------------- | :------: | :----: | ----------- |
| `CODACY_PROJECT_TOKEN` | YES      | YES    | The token that identifies the target project in Codacy |   

### Deployment

Deploy release artifacts to [Bintray](https://bintray.com) and sync to [Maven Central](http://repo.maven.apache.org/maven2/). 
This phase should be executed only when a tag is pushed.
Also, since Bintray supports only release versions, if the tag references a snapshot version, this phase will fail.  
If the Sonatype OSS user is not defined, the sync phase is ignored.

**Environment Variables**  

| Name                | Required | Secure | Description |
| ------------------- | :------: | :----: | ----------- |
| `BINTRAY_REPO_SLUG` | YES      | NO     | The slug of the target repository in Bintray. Expected format: `<subject>/<repo>` |
| `BINTRAY_USER`      | YES      | YES    | The user to use for authentication when uploading the artifacts |
| `BINTRAY_API_KEY`   | YES      | YES    | The API key of the Bintray user, used for authentication |
| `BINTRAY_PUBLISH`   | NO       | NO     | Whether to publish the artifacts after they were uploaded successfully. Values: `0,1`, default: `0` | 
| `BINTRAY_OVERRIDE`  | NO       | NO     | Whether to override existing artifacts (if any) when uploading. Values: `0,1`, default: `0` | 
| `SONATYPE_OSS_USER` | YES      | YES    | The user (in [oss.sonatype.org](https://oss.sonatype.org)) to use for syncing version artifacts to Maven Central |
| `SONATYPE_OSS_PASS` | YES      | YES    | The password of the Sonatype user to use for syncing version artifacts to Maven Central |
