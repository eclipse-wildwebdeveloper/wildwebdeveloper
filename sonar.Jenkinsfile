pipeline {
	options {
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'wildwebdeveloper-buildtest-sonar-pod'
      defaultContainer 'container'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: mickaelistria/wildwebdeveloper-build-test-dependencies@sha256:953ba8b8850ca81a4fe465a8f9f5e5d3b18985b75ecc0ba44d7d2178a232fa9c
    tty: true
    command: [ "uid_entrypoint", "cat" ]
"""
    }
  }
	environment {
		NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
	}
	stages {
		stage('Prepare-environment') {
			steps {
				sh 'npm config set cache="$WORKSPACE/npm-cache"'
				sh 'mkdir -p ${HOME}/.vnc && echo "123456" | vncpasswd -f > ${HOME}/.vnc/passwd && chmod 600 ${HOME}/.vnc/passwd'
			}
		}
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh 'mvn clean verify -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -Dmaven.repo.local=$WORKSPACE/.m2/repository'
				}
			}
			post {
				always {
					junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'repository/target/repository/**' 
				}
			}
		}
		stage('Sonar analysis') {
			steps {
				sh 'mvn sonar:sonar -Dsonar.projectKey=eclipse-wildwebdeveloper -Dsonar.organization=eclipse-wildwebdeveloper -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=fc2945356a47f2f45dfc4f27c49936b054e0bf7a'
			}
		}
	}
}
