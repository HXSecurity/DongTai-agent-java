OLD_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
NEW_VERSION=$1

echo "curent path: `pwd`, change version $OLD_VERSION to $NEW_VERSION"

sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" dongtai-api/src/main/io/dongtai/api/jakarta/JakartaResponseWrapper.java
sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" dongtai-api/src/main/io/dongtai/api/servlet2/ServletResponseWrapper.java
sed -i "s/v$OLD_VERSION/v$NEW_VERSION/g" dongtai-agent/src/main/java/com/secnium/iast/agent/Constant.java
sed -i "s/$OLD_VERSION/$NEW_VERSION/g" dongtai-agent/src/main/resources/iast.properties

mvn -B package -Dmaven.test.skip=true

mvn -q versions:set -DnewVersion="$NEW_VERSION"
mvn -q versions:update-child-modules
mvn -q versions:commit

mvn -B -Dmaven.test.skip=true -DuseGitHubPackages=true deploy
exit 0

# versions:set is a feature in JDK 1.7+ and Maven 3.3.1+
mvn -q versions:set -DnewVersion="$NEW_VERSION"
mvn -q versions:update-child-modules
mvn -q versions:commit

git config --global user.name '$GITHUB_ACTOR-bot'
git config --global user.email '$GITHUB_ACTOR-bot@dongtai.io'
git add .
git commit -m "Update: change version from $OLD_VERSION to $NEW_VERSION"

git push "https://$GITHUB_ACTOR:$GITHUB_TOKEN@github.com/$GITHUB_REPOSITORY.git" refs/heads/release-$NEW_VERSION:refs/heads/release-$NEW_VERSION
