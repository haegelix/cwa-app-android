package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory
import de.rki.coronawarnapp.worker.BackgroundConstants
import timber.log.Timber

/**
 * One time background deadman notification worker
 *
 * @see DeadmanNotificationScheduler
 */
class DeadmanNotificationOneTimeWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sender: DeadmanNotificationSender
) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.tag(TAG).d("Background job started. Run attempt: $runAttemptCount")

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.tag(TAG).d("Background job failed after $runAttemptCount attempts. Rescheduling")

            return Result.failure()
        }
        var result = Result.success()
        try {
            sender.sendNotification()
        } catch (e: Exception) {
            result = Result.retry()
        }

        return result
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<DeadmanNotificationOneTimeWorker>

    companion object {
        private val TAG = DeadmanNotificationOneTimeWorker::class.java.simpleName
    }
}
