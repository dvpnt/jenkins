def call() {
  env.HOST_GID = sh(script: 'echo "$(id -g "$USER")"', returnStdout: true).trim()
  env.HOST_UID = sh(script: 'echo "$(id -u "$USER")"', returnStdout: true).trim()
  env.HOST_USERNAME = env.USER
  env.HOST_GROUPNAME = sh(script: 'echo "$(id -ng "$USER")"', returnStdout: true).trim()
}
