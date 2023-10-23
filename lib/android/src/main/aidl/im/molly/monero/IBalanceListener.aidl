package im.molly.monero;

import im.molly.monero.BlockchainTime;
import im.molly.monero.internal.TxInfo;

oneway interface IBalanceListener {
    void onBalanceChanged(in List<TxInfo> txHistory, in BlockchainTime blockchainTime);
    void onRefresh(in BlockchainTime blockchainTime);
}
