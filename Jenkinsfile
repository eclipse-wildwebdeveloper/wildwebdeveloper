pipeline {
	options {
		timeout(time: 2, unit: 'HOURS')
		buildDiscarder(logRotator(numToKeepStr:'10'))
		disableConcurrentBuilds(abortPrevious: true)
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
    image: docker.io/akurtakov/fedora-gtk3-mutter-java-node:f41-node22
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
	}
	stages {
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
					withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING'), string(credentialsId: 'gpg-passphrase', variable: 'MAVEN_GPG_PASSPHRASE')]) {
						withCredentials([string(credentialsId: "${GITHUB_API_CREDENTIALS_ID}", variable: 'GITHUB_API_TOKEN')]) {
							wrap([$class: 'Xvnc', useXauthority: true]) {
								sh '''mvn clean verify -B -fae -Ddownload.cache.skip=true -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -Psign -Dmaven.repo.local=$WORKSPACE/.m2/repository -Dgithub.api.token="${GITHUB_API_TOKEN}" -Dtycho.pgp.signer.bc.secretKeys="${KEYRING}" '''
							}
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
