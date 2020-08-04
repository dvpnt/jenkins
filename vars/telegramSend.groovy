def getCurrentBuildChanges() {
  def changeLogSets = currentBuild.changeSets
  def changes = []

  for (int i = 0; i < changeLogSets.size(); i++) {
      def entries = changeLogSets[i].items
      for (int j = 0; j < entries.length; j++) {
          def entry = entries[j]
          changes << entries[j]
      }
  }

  return changes
}

def makeText() {
  def emojiHash = [
    SUCCESS: "\u2705",
    FAILURE: "\ufe0f\u26d4\ufe0f",
    UNSTABLE: "\ufe0f\u26d4\ufe0f",
    ABORTED: "\u26a0"
  ]

  def emoji = emojiHash[currentBuild.currentResult]

  def changes = getCurrentBuildChanges().inject("") {
    result, entry -> result + "  \u2219 ${entry.author}: <code>${entry.msg}</code>\n"
  }

  def message = "$emoji <strong>$currentBuild.fullProjectName</strong> "
  message += "build <a href='${currentBuild.absoluteUrl}display/redirect'>#$currentBuild.number</a> "

  def causes = currentBuild.getBuildCauses()
  def cause = causes.size() > 0 ? causes[0].shortDescription.toLowerCase() : "";

  if (cause) {
    if (cause =~ "^started by") {
      message += "${cause} "
    } else {
      message += "caused by ${cause} "
    }
  }

  message += "is <strong>$currentBuild.currentResult</strong>\n"
  message += "\nscm target is <code>${env.GIT_COMMIT[0..8]}</code>\n\n"
  message += changes == "" ? "no scm changes" : "scm changes:\n${changes}"

  return message
}

def send(params) {
  assert params.token != null : 'token is required'
  assert params.chat_id != null : 'chat_id is required'

  def branchPattern = params.branchPattern == null ? /(develop|master)/ : params.branchPattern

  if (!(env.BRANCH_NAME =~ branchPattern)) return;

  httpRequest(
    httpMode: 'POST',
    contentType: 'APPLICATION_JSON_UTF8',
    requestBody: """{"parse_mode": "HTML", "chat_id": ${params.chat_id}, "text": "${makeText()}"}""",
    url: params.url + '/bot' + params.token + '/sendMessage'
  )
}

def call(params = [:]) {
  withCredentials([string(credentialsId: 'telegramNotificationBotToken', variable: 'token')]) {
    send(
      token: params.token ?: token,
      chat_id: params.chat_id ?: env.TELEGRAM_NOTIFICATION_CHAT_ID,
      url: params.url ?: env.TELEGRAM_NOTIFICATION_API_URL ?: 'https://api.telegram.org',
      branchPattern: params.branchPattern
    )
  }
}
