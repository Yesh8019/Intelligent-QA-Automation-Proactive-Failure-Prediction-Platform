pipeline {
    agent any

    environment {
        // Cached inside the workspace so we don't re-download ~150MB on
        // every single build. Chrome for Testing is a portable zip - no
        // installer, no admin rights needed, which matters here because
        // this Jenkins agent has no OS-level access for installing software.
        TOOLS_DIR = "${WORKSPACE}\\.tools\\chrome-for-testing"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Chrome for Testing') {
            steps {
                script {
                    def output = powershell(returnStdout: true, script: '''
                        $ErrorActionPreference = "Stop"
                        $ProgressPreference = "SilentlyContinue"

                        $toolsDir = "$env:TOOLS_DIR"
                        $json = Invoke-RestMethod -Uri "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json"
                        $stable = $json.channels.Stable
                        $version = $stable.version

                        $versionDir = Join-Path $toolsDir $version
                        $chromeExe  = Join-Path $versionDir "chrome-win64\\chrome.exe"
                        $driverExe  = Join-Path $versionDir "chromedriver-win64\\chromedriver.exe"

                        if (-not (Test-Path $chromeExe)) {
                            Write-Host "Chrome for Testing $version not cached - downloading..."
                            New-Item -ItemType Directory -Force -Path $versionDir | Out-Null

                            $chromeUrl  = ($stable.downloads.chrome       | Where-Object { $_.platform -eq "win64" }).url
                            $driverUrl  = ($stable.downloads.chromedriver | Where-Object { $_.platform -eq "win64" }).url

                            Invoke-WebRequest -Uri $chromeUrl  -OutFile (Join-Path $versionDir "chrome.zip")
                            Invoke-WebRequest -Uri $driverUrl  -OutFile (Join-Path $versionDir "chromedriver.zip")

                            Expand-Archive -Path (Join-Path $versionDir "chrome.zip")       -DestinationPath $versionDir -Force
                            Expand-Archive -Path (Join-Path $versionDir "chromedriver.zip") -DestinationPath $versionDir -Force
                        } else {
                            Write-Host "Chrome for Testing $version already cached."
                        }

                        Write-Output $chromeExe
                        Write-Output $driverExe
                    ''').trim()

                    def lines = output.readLines().findAll { it?.trim() }
                    env.CHROME_BINARY = lines[-2].trim()
                    env.CHROMEDRIVER_PATH = lines[-1].trim()

                    echo "Chrome binary:  ${env.CHROME_BINARY}"
                    echo "ChromeDriver:   ${env.CHROMEDRIVER_PATH}"
                }
            }
        }

        stage('Run Tests') {
            steps {
                // maven.test.failure.ignore=true: lets the pipeline continue
                // even when tests fail. Test failures here are EXPECTED data
                // (our AI analysis tool's whole job is to read and explain
                // them) - not a broken build. Jenkins still records the exact
                // pass/fail counts via the "junit" step below; it just won't
                // hard-stop the pipeline before our future analysis stage
                // gets a chance to run.
                bat """
                    mvn clean test ^
                        -Dchrome.binary="%CHROME_BINARY%" ^
                        -Dwebdriver.chrome.driver="%CHROMEDRIVER_PATH%" ^
                        -Dmaven.test.failure.ignore=true
                """
            }
        }

        stage('Analyze Failures with AI') {
            steps {
                // "anthropic-api-key" must exist as a Jenkins "Secret text"
                // credential (Manage Jenkins -> Credentials -> Add). This
                // keeps the real key out of the Jenkinsfile and out of logs -
                // Jenkins masks it automatically wherever it would be printed.
                withCredentials([string(credentialsId: 'anthropic-api-key', variable: 'ANTHROPIC_API_KEY')]) {
                    dir('analysis-tool') {
                        bat 'mvn -q clean package -DskipTests'
                        bat 'java -jar target\\analysis-tool-1.0-SNAPSHOT.jar'
                    }
                }
            }
        }
    }

    post {
        always {
            // Shows pass/fail trends in Jenkins' built-in test report UI
            junit allowEmptyResults: true, testResults: 'target/cucumber-reports/cucumber-junit-report.xml'

            // Keeps the JSON report (tags + data for the future AI analysis
            // tool) and failure screenshots browsable from the build page,
            // plus the final AI-generated analysis report
            archiveArtifacts artifacts: 'target/cucumber-reports/**, target/screenshots/**, target/analysis-report/**',
                              allowEmptyArchive: true
        }
    }
}
