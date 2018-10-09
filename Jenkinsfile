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
    image: maven:latest
    command: [ "echo" ]
    tty: true
    securityContext:
      runAsUser: 0
"""
    }
  }
	stages {
		stage('Prepare-source') {
			steps {
				git url: 'https://github.com/eclipse/wildwebdeveloper.git'
				cleanWs()
				checkout scm
			}
		}
		stage('Prepare environment') {
			steps {
				container('container') {
					sh 'sudo apt-get install -y --no-install-recommends Xvnc gedit' 
				}
			}
		}
		stage('Build') {
			steps {
				container('container') {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						withEnv([]) { // Which environment variable we should use for node ?
							withMaven(maven: 'apache-maven-latest', jdk: 'jdk1.8.0-latest', mavenLocalRepo: '.repository') {
								sh 'mvn clean verify -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -PpackAndSign'
							}
						}
					}
				}
			}
			post {
				success {
					junit '*/target/surefire-reports/TEST-*.xml' 
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'master'
				// TODO deploy all branch from Eclipse.org Git repo
			}
			steps {
				// TODO compute the target URL (snapshots) according to branch name (0.5-snapshots...)
				sh 'rm -rf /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
				sh 'mkdir -p /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
				sh 'cp -r repository/target/repository/* /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
				sh 'zip -R /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots/repository.zip repository/target/repository/*'
			}
		}
	}
}
