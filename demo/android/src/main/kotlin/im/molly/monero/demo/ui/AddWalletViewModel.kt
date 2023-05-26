package im.molly.monero.demo.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import im.molly.monero.MoneroNetwork
import im.molly.monero.RestorePoint
import im.molly.monero.SecretKey
import im.molly.monero.demo.AppModule
import im.molly.monero.demo.data.RemoteNodeRepository
import im.molly.monero.demo.data.WalletRepository
import im.molly.monero.demo.data.model.DefaultMoneroNetwork
import im.molly.monero.demo.data.model.RemoteNode
import im.molly.monero.util.parseHex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddWalletViewModel(
    private val remoteNodeRepository: RemoteNodeRepository = AppModule.remoteNodeRepository,
    private val walletRepository: WalletRepository = AppModule.walletRepository,
) : ViewModel() {

    var network by mutableStateOf(DefaultMoneroNetwork)
        private set

    var walletName by mutableStateOf("")
        private set

    var secretSpendKeyHex by mutableStateOf("")
        private set

    var creationDate by mutableStateOf("")
        private set

    var restoreHeight by mutableStateOf("")
        private set

    val currentRemoteNodes: StateFlow<List<RemoteNode>> =
        snapshotFlow { network }
            .flatMapLatest {
                remoteNodeRepository.getAllRemoteNodes(
                    filterNetworkIds = setOf(network.id),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5000),
                initialValue = listOf(RemoteNode.EMPTY),
            )

    val selectedRemoteNodes = mutableStateMapOf<Long?, Boolean>()

    private fun getSelectedRemoteNodeIds() =
        selectedRemoteNodes.filterValues { checked -> checked }.keys.filterNotNull()

    init {
        val previousNodes = mutableSetOf<RemoteNode>()

        currentRemoteNodes.onEach { remoteNodes ->
            val unseenNodes = remoteNodes.filter { it !in previousNodes }
            unseenNodes.forEach { node ->
                selectedRemoteNodes[node.id] = true
                previousNodes.add(node)
            }
        }.launchIn(viewModelScope)
    }

    fun toggleSelectedNetwork(network: MoneroNetwork) {
        this.network = network
    }

    fun updateWalletName(name: String) {
        this.walletName = name
    }

    fun updateSecretSpendKeyHex(value: String) {
        this.secretSpendKeyHex = value
    }

    fun updateCreationDate(value: String) {
        this.creationDate = value
    }

    fun updateRestoreHeight(value: String) {
        this.restoreHeight = value
    }

    fun validateSecretSpendKeyHex(): Boolean =
        secretSpendKeyHex.length == 64 && runCatching { secretSpendKeyHex.parseHex() }.isSuccess

    fun validateCreationDate(): Boolean =
        creationDate.isEmpty() || runCatching { LocalDate.parse(creationDate) }.isSuccess

    fun validateRestoreHeight(): Boolean =
        restoreHeight.isEmpty() || runCatching { RestorePoint(restoreHeight.toLong()) }.isSuccess

    fun createWallet() = viewModelScope.launch {
        walletRepository.addWallet(network, walletName, getSelectedRemoteNodeIds())
    }

    fun restoreWallet() = viewModelScope.launch {
        val restorePoint = if (creationDate.isNotEmpty()) {
            RestorePoint(creationDate = LocalDate.parse(creationDate))
        } else if (restoreHeight.isNotEmpty()) {
            RestorePoint(blockHeight = restoreHeight.toLong())
        } else {
            RestorePoint(blockHeight = 0)
        }
        SecretKey(secretSpendKeyHex.parseHex()).use { secretSpendKey ->
            walletRepository.restoreWallet(
                network,
                walletName,
                getSelectedRemoteNodeIds(),
                secretSpendKey,
                restorePoint
            )
        }
    }
}
