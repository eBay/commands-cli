# Release Instructions

There are two main use cases: 
1. **[Release version](#release-version)** - includes new features or bug fixes
   (advancing the major or minor version)
1. **[Support version](#support-version)** - creating a patch for a released version

The following procedures have the following assumptions:
1. The build in [Travis-CI](https://travis-ci.org/eBay/commands-cli) is triggered also for new git tags
1. When new git tags are built in Travis-CI deployment to [Bintray](https://bintray.com/ebay/maven-oss/commands-cli) and sync to [Maven Central](http://repo.maven.apache.org/maven2/com/ebay/sd/commons/commands-cli/) are also executed
1. The `master` branch is protected - direct commits are disabled
1. New code is merged to `master` from either forks or other branches only by pull requests

## Release Version

**Guidelines:**
1. Increase the major version only for breaking API changes or very big changes
1. Increase the minor version for new features and bug fixes 

**Process:**
1. Create a release branch from `master`
   ```
   git checkout master
   git pull
   git checkout -b release/<version>
   ```
1. Change the project version to `<version>`
   ```
   mvn versions:set -DnewVersion=<version>
   git add pom.xml
   git commit -m 'Relesae version <version>'
   git push origin release/<version>
   ```
   Follow the build process in Travis to make sure it passes successfully
1. Tag the release version
   ```
   git tag v<version>
   git push --tags
   ```
   Follow the build process in Travis to make sure it passes successfully  
1. Check and verify deployment:
   1. The new version was uploaded and published to Bintray
   1. The new version was synced to Maven Central (might take ~10 minutes to update)
1. Change the project version to the next snapshot version
   ```
   mvn versions:set -DnewVersion=<version+1>-SNAPSHOT
   git add pom.xml
   git commit -m 'Next snapshot version: <version+1>-SNAPSHOT'
   git push origin release/<version>
   ``` 
1. Create a pull request to merge the release branch back to `master`
1. Create and publish a release in GitHub, based on the new tag 
1. Delete the release branch

## Support Version 

**Guidelines:**
1. Increase the patch version for mandatory hot fixes or for non-functional changes

**Process:**  
The process for releasing a support (patch) version is very similar to the process of a release version,
but has the following differences:
1. Create the support branch from the tag of the base version you want to patch
   ```
   git fetch
   git checkout v<base-version>
   git checkout -b support/<version>
   ```
1. Change the project version to `<version>`
1. Make your changes
1. Tag the support version  
1. Check and verify deployment
1. Change the project version to the current snapshot version
1. Create a pull request to merge the support branch into `master`
1. Create and publish a release in GitHub, based on the new tag 
1. Delete the support branch