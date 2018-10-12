pipeline {
	options {
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
  agent {
    kubernetes {
      label 'buildtestPod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: kdvolder/mvn-plus-npm
    tty: true
    command:
      - cat
"""
    }
  }
	environment {
		NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
	}
	stages {
		stage('Test-ssh') {
			steps {
				sshagent ( ['project-storage.eclipse.org-bot-ssh']) {
					sh 'ssh genie.wildwebdeveloper@build.eclipse.org ls -l /home/data/httpd/download.eclipse.org/wildwebdeveloper/'
				}
			}
		}
		stage('Prepare-source') {
			steps {
				git url: 'https://github.com/eclipse/wildwebdeveloper.git'
				cleanWs()
				checkout scm
			}
		}
		stage('Prepare-environment') {
			steps {
				container('container') {
					sh 'npm config set cache="$WORKSPACE/npm-cache"'
				}
			}
		}
		stage('Build') {
			steps {
				container('container') {
					sh 'mvn clean verify -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -DskipTests -PpackAndSign -Dmaven.repo.local=$WORKSPACE/.m2/repository'
				}
			}
			post {
				always {
					//junit '*/target/surefire-reports/TEST-*.xml'
					archiveArtifacts artifacts: 'repository/target/repository/**' 
				}
			}
		}
		stage('Deploy') {
			// TODO maybe compute the target URL (snapshots) according to branch name (0.5-snapshots...)
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
