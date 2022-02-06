#ifndef LOGGING_H_
#define LOGGING_H_

#include <android/log.h>

#include <string>

#include "jvm.h"

namespace monero {

// Logging.kt priority levels.
enum LoggingLevel {
  VERBOSE = ANDROID_LOG_VERBOSE,
  DEBUG = ANDROID_LOG_DEBUG,
  INFO = ANDROID_LOG_INFO,
  WARN = ANDROID_LOG_WARN,
  ERROR = ANDROID_LOG_ERROR,
  ASSERT = ANDROID_LOG_FATAL,
};

// Register easylogging++ post dispatcher and configure log format.
void initializeEasyLogging();

// Log sink to send logs to JVM via Logging.kt API.
class JvmLogSink {
 public:
  JvmLogSink(JvmLogSink&) = delete;
  void operator=(const JvmLogSink&) = delete;

  static JvmLogSink* instance() {
    static JvmLogSink ins;
    return &ins;
  }

  // This is called when a log message is dispatched by easylogging++.
  void write(const std::string& tag, LoggingLevel priority, const std::string& msg);

  void set_logger(JNIEnv* env, const JvmRef<jobject>& logger);

 protected:
  JvmLogSink() = default;

 private:
  ScopedJvmGlobalRef<jobject> m_logger;
};

}  // namespace monero

#endif  // LOGGING_H_
