pipeline {
	options {
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'wildwebdeveloper-buildtest-pod'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: mickaelistria/wildwebdeveloper-build-test-dependencies@sha256:953ba8b8850ca81a4fe465a8f9f5e5d3b18985b75ecc0ba44d7d2178a232fa9c
    tty: true
    command: [ "uid_entrypoint", "cat" ]
  - name: jnlp
    image: 'eclipsecbi/jenkins-jnlp-agent'
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
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
					sh 'npm config set cache="$WORKSPACE/npm-cache"'
					sh 'mkdir -p ${HOME}/.vnc && echo "123456" | vncpasswd -f > ${HOME}/.vnc/passwd && chmod 600 ${HOME}/.vnc/passwd'
				}
			}
		}
		stage('Build') {
			steps {
				container('container') {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						sh 'mvn clean verify -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -PpackAndSign -Dmaven.repo.local=$WORKSPACE/.m2/repository'
					}
				}
			}
			post {
				always {
					junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'repository/target/repository/**' 
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
