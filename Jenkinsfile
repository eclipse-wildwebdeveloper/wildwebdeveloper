pipeline {
	options {
		timeout(time: 30, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'wildwebdeveloper-buildtest-pod-f30-take1'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: mickaelistria/fedora-gtk3-mutter-java-node@sha256:5362b90f4b41ec8391441c17e74aeb9a02ac5a04a5ff4a3030f77fdb627b9f24
    tty: true
    command: [ "uid_entrypoint", "cat" ]
  - name: jnlp
    image: 'eclipsecbi/jenkins-jnlp-agent'
    volumeMounts:
    - mountPath: /home/jenkins/.ssh
      name: volume-known-hosts
  volumes:
  - configMap:
      name: known-hosts
    name: volume-known-hosts
"""
    }
  }
	environment {
		NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
	}
	stages {
		stage('Prepare-environment') {
			steps {
				container('container') {
					sh 'node --version'
					sh 'npm --version'
					sh 'npm config set cache="$WORKSPACE/npm-cache"'
				}
			}
		}
		stage('Build') {
			steps {
				container('container') {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						sh 'mvn clean verify -B -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -PpackAndSign -Dmaven.repo.local=$WORKSPACE/.m2/repository'
					}
				}
			}
			post {
				always {
					junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'repository/target/repository/**,*/target/work/configuration/*.log,*/target/work/data/.metadata/.log,*/target/work/data/languageServers-log/**'
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'master'
			}
			steps {
				sshagent ( ['project-storage.eclipse.org-bot-ssh']) {
					sh 'ssh genie.wildwebdeveloper@build.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
					sh 'ssh genie.wildwebdeveloper@build.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
					sh 'scp -r repository/target/repository/* genie.wildwebdeveloper@build.eclipse.org:/home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots'
				}
			}
		}
	}
}
