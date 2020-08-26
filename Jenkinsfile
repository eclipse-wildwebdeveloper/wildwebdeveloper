pipeline {
	options {
		timeout(time: 30, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'10'))
	}
    agent {
		label "centos-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'openjdk-jdk11-latest'
	}
	environment {
		NPM_CONFIG_USERCONFIG = "$WORKSPACE/.npmrc"
		MAVEN_OPTS="-Xmx1024m"
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh '''
						mvn clean verify -B -Dmaven.test.error.ignore=true -Dmaven.test.failure.ignore=true -PpackAndSign -Dmaven.repo.local=$WORKSPACE/.m2/repository
					'''
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
