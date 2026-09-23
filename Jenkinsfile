/*
 * Mobile UI Tests - declarative pipeline.
 *
 * Every run is driven by build parameters -> the same -D overrides used locally and in GitHub Actions,
 * so there is no Jenkins-only logic in the framework.
 *
 * Agent requirements (label = AGENT_LABEL):
 *   Android : JDK 21, Maven, Node + Appium 3 (uiautomator2 driver), Android SDK, a running emulator/device
 *   iOS     : a macOS agent with Xcode + Appium xcuitest driver
 * Jenkins plugins used: Pipeline, JUnit, Allure Jenkins Plugin (optional, see post section).
 * Works on Linux/macOS (sh) and Windows (bat) agents.
 */
pipeline {
    agent { label params.AGENT_LABEL ?: 'mobile' }

    parameters {
        choice(name: 'PLATFORM', choices: ['android', 'ios'], description: 'Target platform')
        choice(name: 'EXEC_ENV', choices: ['local', 'browserstack'], description: 'local = agent device, browserstack = cloud')
        string(name: 'TAGS', defaultValue: '@smoke', description: 'Cucumber tag expression, e.g. @smoke, @regression, "@cart and not @ignore"')
        string(name: 'THREADS', defaultValue: '1', description: 'Parallel scenarios (never more than devices)')
        string(name: 'DEVICES', defaultValue: '', description: 'Comma-separated udids for the device pool (empty = single device)')
        string(name: 'APP_PATH', defaultValue: '', description: 'Override app path on the agent (empty = config default)')
        string(name: 'RETRY_COUNT', defaultValue: '1', description: 'Retries per failed scenario (0 = strict)')
        string(name: 'AGENT_LABEL', defaultValue: 'mobile', description: 'Agent label with devices attached')
    }

    triggers {
        // Nightly run with default parameters (smoke). For a nightly @regression use the
        // Parameterized Scheduler plugin or a second job with TAGS=@regression.
        cron('H 2 * * *')
    }

    options {
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        disableConcurrentBuilds()            // one job per device set - avoids two builds fighting over an emulator
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
    }

    environment {
        APPIUM_URL = 'http://127.0.0.1:4723'
        // Agents may be freshly provisioned: never skip Appium's device setup in CI
        SKIP_DEVICE_INITIALIZATION = 'false'
        EXPLICIT_WAIT_SECONDS = '25'
        // Groups this build's cloud sessions on the BrowserStack dashboard
        BS_BUILD_NAME = "jenkins-${env.JOB_NAME}-${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify toolchain') {
            steps {
                runCmd 'java -version'
                runCmd 'mvn -v'
                runCmd 'appium -v'
                script {
                    if (params.PLATFORM == 'android' && params.EXEC_ENV == 'local') {
                        runCmd 'adb devices'
                    }
                }
            }
        }

        stage('Start Appium') {
            when { expression { params.EXEC_ENV == 'local' } }
            steps {
                script {
                    if (appiumIsUp()) {
                        echo 'Appium already running on the agent - reusing it'
                        env.STARTED_APPIUM = 'false'
                    } else {
                        startAppium()
                        env.STARTED_APPIUM = 'true'
                        timeout(time: 1, unit: 'MINUTES') {
                            waitUntil(initialRecurrencePeriod: 2000) { appiumIsUp() }
                        }
                    }
                }
            }
        }

        stage('Upload app to BrowserStack') {
            when { expression { params.EXEC_ENV == 'browserstack' } }
            steps {
                script {
                    if (!params.APP_PATH?.trim()) {
                        error 'APP_PATH is required for BrowserStack: path to the .apk (Android) or RealDevice .ipa (iOS) on the agent'
                    }
                    withCredentials([usernamePassword(credentialsId: 'browserstack',
                            usernameVariable: 'BROWSERSTACK_USERNAME',
                            passwordVariable: 'BROWSERSTACK_ACCESS_KEY')]) {
                        // Git Bash provides "bash" on Windows agents
                        def cmd = "bash scripts/browserstack-upload.sh ${params.PLATFORM} \"${params.APP_PATH.trim()}\""
                        def appUrl = isUnix() ? sh(script: cmd, returnStdout: true)
                                              : bat(script: "@${cmd}", returnStdout: true)
                        env."BROWSERSTACK_APP_${params.PLATFORM.toUpperCase()}" = appUrl.trim()
                        echo "BrowserStack app: ${appUrl.trim()}"
                    }
                }
            }
        }

        stage('Run tests') {
            steps {
                script {
                    def cmd = "mvn -B clean test ${mavenArgs()}"
                    if (params.EXEC_ENV == 'browserstack') {
                        // Jenkins credential (Username with password) id: 'browserstack'
                        withCredentials([usernamePassword(credentialsId: 'browserstack',
                                usernameVariable: 'BROWSERSTACK_USERNAME',
                                passwordVariable: 'BROWSERSTACK_ACCESS_KEY')]) {
                            runCmd cmd
                        }
                    } else {
                        runCmd cmd
                    }
                }
            }
        }
    }

    post {
        always {
            // Test failures don't abort the pipeline (maven.test.failure.ignore); JUnit marks the build
            // UNSTABLE instead, so reports and evidence are always published.
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true

            archiveArtifacts allowEmptyArchive: true, artifacts:
                    'target/cucumber-reports/**, target/screenshots/**, target/logs/**, target/allure-results/**, logs/**'

            script {
                // Requires the Allure Jenkins Plugin + an "Allure Commandline" tool; skipped gracefully otherwise
                try {
                    allure includeProperties: false, results: [[path: 'target/allure-results']]
                } catch (NoSuchMethodError ignored) {
                    echo 'Allure plugin not installed - raw results archived under target/allure-results'
                }
                if (env.STARTED_APPIUM == 'true') {
                    stopAppium()
                }
            }
        }
    }
}

// ---------------------------------------------------------------- helpers

/** Runs a shell command on Unix agents and a batch command on Windows agents. */
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

/** Build parameters -> the framework's -D overrides (same flags as local and GitHub Actions). */
String mavenArgs() {
    def args = [
            "-Dplatform=${params.PLATFORM}",
            "-Denv=${params.EXEC_ENV}",
            "\"-Dcucumber.filter.tags=${params.TAGS}\"",
            "-Dthreads=${params.THREADS}",
            "-Dretry.count=${params.RETRY_COUNT}",
            "-Dmaven.test.failure.ignore=true"
    ]
    if (params.DEVICES?.trim()) {
        args << "-Ddevices=${params.DEVICES.trim()}"
    }
    // On BrowserStack the app is the uploaded bs:// id (from the upload stage), not a local path
    if (params.APP_PATH?.trim() && params.EXEC_ENV != 'browserstack') {
        args << "\"-Dapp.path=${params.APP_PATH.trim()}\""
    }
    return args.join(' ')
}

boolean appiumIsUp() {
    def probe = isUnix()
            ? "curl -s ${env.APPIUM_URL}/status | grep -q ready"
            : "curl -s ${env.APPIUM_URL}/status | findstr ready"
    return (isUnix() ? sh(script: probe, returnStatus: true) : bat(script: probe, returnStatus: true)) == 0
}

void startAppium() {
    if (isUnix()) {
        // Log lives in logs/ (not target/) so "mvn clean" never deletes a file Appium is writing.
        // JENKINS_NODE_COOKIE stops Jenkins from killing the background server when the step ends
        sh '''
            mkdir -p logs
            JENKINS_NODE_COOKIE=dontKillMe nohup appium --address 127.0.0.1 --port 4723 --log-timestamp \
                > logs/appium-server.log 2>&1 &
        '''
    } else {
        bat '''
            if not exist logs mkdir logs
            start "appium" /B cmd /c "appium --address 127.0.0.1 --port 4723 --log-timestamp > logs\\appium-server.log 2>&1"
        '''
    }
}

void stopAppium() {
    if (isUnix()) {
        sh 'pkill -f "appium --address 127.0.0.1 --port 4723" || true'
    } else {
        bat '''
            for /f "tokens=5" %%p in ('netstat -ano ^| findstr :4723 ^| findstr LISTENING') do taskkill /F /PID %%p
            exit /b 0
        '''
    }
}
