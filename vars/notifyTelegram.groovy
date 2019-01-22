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
    canceled: "\u26a0"
  ]

  def emoji = emojiHash[currentBuild.currentResult]

  def changes = getCurrentBuildChanges().inject("") {
    result, entry -> result + "  \u2219 ${entry.author}: <code>${entry.msg}</code>"
  }

  def message = "$emoji <strong>$currentBuild.fullProjectName</strong> "
  message += "build <a href='${currentBuild.absoluteUrl}display/redirect'>#$currentBuild.number</a> "
  def causedBy = currentBuild.getBuildCauses();
  if (causedBy.size() > 0) {
    message += (changes == "" ? "" : "caused by ") + "${causedBy[0].shortDescription.toLowerCase()} "
  }
  message += "is <strong>$currentBuild.currentResult</strong>\n"
  message += "\nscm target is <code>${env.GIT_COMMIT[0..8]}</code>\n\n"
  message += "scm changes:\n"
  message += changes == "" ? "no changes" : changes

  return message
}

def call(token, chat_id, branches = null, url = 'https://api.telegram.org') {
  if (branches == null || !branches.contains(env.BRANCH_NAME)) return;

  httpRequest(
    consoleLogResponseBody: true,
    httpMode: 'POST',
    contentType: 'APPLICATION_JSON_UTF8',
    requestBody: """{"parse_mode": "HTML", "chat_id": $chat_id, "text": "${makeText()}"}""",
    url: url + '/bot' + token + '/sendMessage'
  )
}


