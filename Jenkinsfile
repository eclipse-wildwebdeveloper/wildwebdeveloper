pipeline {
	options {
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'buildtestPod'
      defaultContainer 'container'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: kdvolder/mvn-plus-npm
    tty: true
    command: [ "cat" ]
"""
    }
  }
	environment {
		NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
	}
	stages {
		stage('Prepare-source') {
			steps {
				git url: 'https://github.com/eclipse/wildwebdeveloper.git'
				cleanWs()
				checkout scm
			}
		}
		stage('Prepare-environment') {
			steps {
				sh 'npm config set cache="$(pwd)/target/npm-cache"'
			}
		}
		stage('Build') {
			steps {
				sh 'mvn --version'
				sh 'mvn clean verify -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -DskipTests -PpackAndSign -Dmaven.repo.local=/tmp/.m2/repository'
			}
			post {
				always {
					//junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'repository/target/repository/**' 
				}
			}
		}
//		stage('Deploy') {
//			when {
//				branch 'master'
//			}
//			steps {
//				// TODO compute the target URL (snapshots) according to branch name (0.5-snapshots...)
//				sh 'rm -rf download.eclipse.org/wildwebdeveloper/snapshots'
//				sh 'mkdir -p download.eclipse.org/wildwebdeveloper/snapshots'
//				sh 'cp -r repository/target/repository/* download.eclipse.org/wildwebdeveloper/snapshots'
//				sh 'zip -R download.eclipse.org/wildwebdeveloper/snapshots/repository.zip repository/target/repository/*'
//			}
//		}
	}
}
