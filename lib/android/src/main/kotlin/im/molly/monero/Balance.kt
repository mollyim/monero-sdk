package im.molly.monero

import java.time.Instant

data class Balance(
    val pendingAmount: MoneroAmount,
    val timeLockedAmounts: List<TimeLocked<MoneroAmount>>,
) {
    val confirmedAmount: MoneroAmount = timeLockedAmounts.sumOf { it.value }
    val totalAmount: MoneroAmount = confirmedAmount + pendingAmount

    fun unlockedAmountAt(
        blockHeight: Int,
        currentInstant: Instant = Instant.now(),
    ): MoneroAmount {
        val targetTime = BlockchainTime(blockHeight, currentInstant)
        return timeLockedAmounts.filter { it.isUnlocked(targetTime) }.sumOf { it.value }
    }

    fun lockedAmountsAt(
        blockHeight: Int,
        currentInstant: Instant = Instant.now(),
    ): Map<BlockchainTimeSpan, MoneroAmount> {
        val targetTime = BlockchainTime(blockHeight, currentInstant)
        return timeLockedAmounts
            .filter { it.isLocked(targetTime) }
            .groupBy({ it.timeUntilUnlock(targetTime) }, { it.value })
            .mapValues { (_, amounts) ->
                amounts.sum()
            }
    }
}

fun Iterable<TimeLocked<Enote>>.calculateBalance(): Balance {
    var pendingAmount = MoneroAmount.ZERO

    val lockedAmounts = mutableListOf<TimeLocked<MoneroAmount>>()

    for (timeLocked in filter { !it.value.spent }) {
        if (timeLocked.value.age == 0) {
            pendingAmount += timeLocked.value.amount
        } else {
            lockedAmounts.add(TimeLocked(timeLocked.value.amount, timeLocked.unlockTime))
        }
    }

    return Balance(pendingAmount, lockedAmounts)
}
