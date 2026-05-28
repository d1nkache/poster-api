#!/bin/sh
set -eu

case "${1:-api}" in
  api)
    exec ./bin/Poster-api
    ;;
  mail-worker)
    exec java -cp "lib/*" com.example.workers.worker.MailWorkerMainKt
    ;;
  otp-worker)
    exec java -cp "lib/*" com.example.workers.worker.OtpWorkerMainKt
    ;;
  *)
    exec "$@"
    ;;
esac
