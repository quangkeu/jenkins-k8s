podTemplate(label: 'maven', containers: [
    containerTemplate(
        name: 'maven', 
        image: 'maven:3-jdk-8-alpine', 
        ttyEnabled: true, 
        command: 'cat')
    ]) {
        node('maven') {
            stage('Build a Maven project') {
                git 'https://github.com/quangkeu/jenkins-k8s.git'
                container('maven') {
                    sh """
                    whoami
                    ls
                    """
                }
            }
        }
    }
