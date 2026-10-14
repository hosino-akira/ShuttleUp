package com.shuttleup.backend.exception;

/** 同じユーザーのトレーニングは1日1件まで。 */
public class TrainingDateAlreadyExistsException extends RuntimeException {
    public TrainingDateAlreadyExistsException() {
        super("この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。");
    }

    public TrainingDateAlreadyExistsException(Throwable cause) {
        super("この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。", cause);
    }
}
