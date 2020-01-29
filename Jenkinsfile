pipeline {
	options {
		timeout(time: 30, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'wildwebdeveloper-buildtest-pod-f31-take6'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: docker.io/mickaelistria/fedora-gtk3-mutter-java-node:31-gtk3.24
    imagePullPolicy: Always
    tty: true
    command: [ "uid_entrypoint", "cat" ]
    resources:
      limits:
        memory: "4Gi"
        cpu: "2000m"
      requests:
        memory: "4Gi"
        cpu: "1000m"
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
		MAVEN_OPTS="-Xmx1024m"
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
				sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
					sh '''
						ssh genie.wildwebdeveloper@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots
						ssh genie.wildwebdeveloper@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots
						scp -r repository/target/repository/* genie.wildwebdeveloper@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/wildwebdeveloper/snapshots
					'''
				}
			}
		}
	}
}
