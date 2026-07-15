pipeline {
  options {
    timeout(time: 2, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr:'10'))
    disableConcurrentBuilds(abortPrevious: true)
  }

  agent {
    kubernetes {
      inheritFrom 'wildwebdeveloper-buildtest-pod'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: docker.io/akurtakov/fedora-gtk3-mutter-java-node:f42-node24
    imagePullPolicy: Always
    tty: true
    resources:
      limits:
        memory: "4Gi"
        cpu: "2000m"
      requests:
        memory: "4Gi"
        cpu: "1000m"
  - name: jnlp
    volumeMounts:
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
"""
    }
  }

  environment {
    NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
    MAVEN_OPTS="-Xmx1024m"
    GITHUB_API_CREDENTIALS_ID = 'github-bot-token'
    MAIN_BRANCH = 'master'
    BUILD_TIMESTAMP = sh(returnStdout: true, script: 'date +%Y%m%d-%H%M').trim()
  }

  parameters {
    choice(
      name: 'BUILD_TYPE',
      choices: ['nightly', 'milestone', 'release'],
      description: '''
      Choose the type of build.
      Note that a release build will not promote the build, but rather will promote the most recent milestone build.
      '''
    )

    booleanParam(
      name: 'PROMOTE',
      defaultValue: true,
      description: 'Whether to promote the build to the download server.'
    )
  }

  stages {
    stage('Display Parameters') {
      steps {
        script {
          env.BUILD_TYPE = params.BUILD_TYPE
          if (env.BRANCH_NAME == env.MAIN_BRANCH) {
             env.WITH_CREDENTIALS = 'true'
             env.MAVEN_PROFILES = '-Psign'
          } else {
            env.WITH_CREDENTIALS = 'false'
            env.MAVEN_PROFILES = ''
            env.PROMOTE = 'false'
          }

          def description = """
BUILD_TYPE=${env.BUILD_TYPE}
PROMOTE=${env.PROMOTE}
BUILD_TIMESTAMP=${env.BUILD_TIMESTAMP}
MAVEN_PROFILES=${env.MAVEN_PROFILES}
BRANCH_NAME=${env.BRANCH_NAME}
""".trim()
          echo description
          currentBuild.description = description.replace("\n", "<br/>")
        }
      }
    }

    stage('Prepare-environment') {
      steps {
        container('container') {
          sh 'java -version'
          sh 'mvn --version'
          sh 'node --version'
          sh 'npm --version'
          sh 'npm config set cache="$WORKSPACE/npm-cache"'
        }
      }
    }
    stage('Build') {
      steps {
        container('container') {
          script {
            if (env.WITH_CREDENTIALS == 'true') {
              withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING'), string(credentialsId: 'gpg-passphrase', variable: 'MAVEN_GPG_PASSPHRASE')]) {
                withCredentials([string(credentialsId: "${GITHUB_API_CREDENTIALS_ID}", variable: 'GITHUB_API_TOKEN')]) {
                  mvn()
                }
              }
              if (env.PROMOTE == 'true') {
                stash includes: '.mvn/**,pom.xml,**/pom.xml,repository/target/repository/**,org.eclipse.wildwebdeveloper.xml/**', name: 'container-stash'
              }
            } else {
              mvn()
            }
          }
        }
      }
      post {
        always {
          junit '*/target/surefire-reports/TEST-*.xml'
          archiveArtifacts artifacts: 'repository/target/repository/**,*/target/work/configuration/*.log,*/target/work/data/.metadata/.log,*/target/work/data/languageServers-log/**,org.eclipse.wildwebdeveloper/target/chrome-debug-adapter/chromeDebugAdapter.zip'
        }
      }
    }

    stage('Deploy') {
      when {
        beforeAgent true
        environment name: 'PROMOTE', value: 'true'
      }
      agent any
      tools {
        maven 'apache-maven-latest'
        jdk 'temurin-jdk21-latest'
      }
      options {
        skipDefaultCheckout true
      }
      steps {
        unstash 'container-stash'
        sshagent (['projects-storage.eclipse.org-bot-ssh']) {
          promote();
        }
      }
    }
  }

  post {
    failure {
      mail to: 'ed.merks@gmail.com',
      subject: "[WildWebDeveloper CI] Build Failure ${currentBuild.fullDisplayName}",
      mimeType: 'text/html',
      body: "Project: ${env.JOB_NAME}<br>Build Number: ${env.BUILD_NUMBER}<br>Build URL: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a><br>Console: <a href='${env.BUILD_URL}/console'>${env.BUILD_URL}/console</a>"
    }
    fixed {
      mail to: 'ed.merks@gmail.com',
      subject: "[WildWebDeveloper CI] Back to normal ${currentBuild.fullDisplayName}",
      mimeType: 'text/html',
      body: "Project: ${env.JOB_NAME}<br>Build Number: ${env.BUILD_NUMBER}<br>Build URL: <a href='${env.BUILD_URL}'>${env.BUILD_URL}</a><br>Console: <a href='${env.BUILD_URL}/console'>${env.BUILD_URL}/console</a>"
    }
  }
}

def void mvn() {
  wrap([$class: 'Xvnc', useXauthority: true]) {
    sh '''
      mvn \
      clean \
      verify \
      -B \
      -fae \
      $MAVEN_PROFILES \
      -Ddownload.cache.skip=true \
      -Dmaven.test.error.ignore=true \
      -Dmaven.test.failure.ignore=true \
      -Dmaven.repo.local=$WORKSPACE/.m2/repository \
      -Dgithub.api.token="${GITHUB_API_TOKEN}" \
      -Dtycho.pgp.signer.bc.secretKeys="${KEYRING}" \
    '''
  }
}

def void promote() {
  sh '''
    mvn \
    verify \
    -B \
    -pl :promote \
    -Ppromote \
    -Dbuild.type=$BUILD_TYPE \
    -Dgit.commit=$GIT_COMMIT \
    -Dorg.eclipse.justj.p2.manager.build.url=$JOB_URL \
    '''
}
