package im.molly.monero

import android.os.Parcelable
import com.google.protobuf.ByteString
import im.molly.monero.proto.OwnedTxOutProto
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Transaction output that belongs to a [WalletNative].
 */
@Parcelize
data class OwnedTxOut
@CalledByNative("wallet.cc")
constructor(
    val txId: ByteArray,
    val amount: Amount,
    val blockHeight: Long,
    val spentInBlockHeight: Long,
) : Parcelable {

    init {
        require(blockHeight <= spentInBlockHeight)
    }

    @IgnoredOnParcel
    val spent: Boolean = spentInBlockHeight != 0L

    @IgnoredOnParcel
    val notSpent = !spent

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OwnedTxOut

        if (!txId.contentEquals(other.txId)) return false

        return true
    }

    override fun hashCode(): Int {
        return txId.contentHashCode()
    }

    companion object {
        fun fromProto(proto: OwnedTxOutProto) = OwnedTxOut(
            txId = proto.txId.toByteArray(),
            amount = proto.amount.toAtomicAmount(),
            blockHeight = proto.blockHeight,
            spentInBlockHeight = proto.spentHeight,
        )
    }

    fun proto(): OwnedTxOutProto = OwnedTxOutProto.newBuilder()
        .setTxId(ByteString.copyFrom(txId))
        .setAmount(amount.atomic)
        .setBlockHeight(blockHeight)
        .setSpentHeight(spentInBlockHeight)
        .build()
}
