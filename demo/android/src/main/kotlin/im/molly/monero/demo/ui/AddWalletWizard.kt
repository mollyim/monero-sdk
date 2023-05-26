package im.molly.monero.demo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import im.molly.monero.MoneroNetwork
import im.molly.monero.demo.data.model.DefaultMoneroNetwork
import im.molly.monero.demo.data.model.RemoteNode
import im.molly.monero.demo.ui.component.SelectListBox
import im.molly.monero.demo.ui.component.Toolbar
import im.molly.monero.demo.ui.theme.AppIcons
import im.molly.monero.demo.ui.theme.AppTheme

@Composable
fun AddWalletFirstStepRoute(
    onBackClick: () -> Unit,
    onNavigateToCreateWallet: () -> Unit,
    onNavigateToRestoreWallet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FirstStepScreen(
        onBackClick = onBackClick,
        onCreateClick = onNavigateToCreateWallet,
        onRestoreClick = onNavigateToRestoreWallet,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstStepScreen(
    onBackClick: () -> Unit,
    onCreateClick: () -> Unit,
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            Toolbar(
                title = "Add wallet",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onCreateClick,
            ) {
                Text("Create a new wallet")
            }
            OutlinedButton(
                onClick = onRestoreClick,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("I already have a wallet")
            }
        }
    }
}

@Composable
fun AddWalletSecondStepRoute(
    showRestoreOptions: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    viewModel: AddWalletViewModel = viewModel(),
) {
    val remoteNodes by viewModel.currentRemoteNodes.collectAsStateWithLifecycle()

    SecondStepScreen(
        showRestoreOptions = showRestoreOptions,
        modifier = modifier,
        onBackClick = onBackClick,
        onCreateClick = {
            if (showRestoreOptions) {
                viewModel.restoreWallet()
            } else {
                viewModel.createWallet()
            }
            onNavigateToHome()
        },
        walletName = viewModel.walletName,
        network = viewModel.network,
        secretSpendKeyHex = viewModel.secretSpendKeyHex,
        secretSpendKeyHexError = !viewModel.validateSecretSpendKeyHex(),
        creationDate = viewModel.creationDate,
        creationDateError = !viewModel.validateCreationDate(),
        restoreHeight = viewModel.restoreHeight,
        restoreHeightError = !viewModel.validateRestoreHeight(),
        onWalletNameChanged = { name -> viewModel.updateWalletName(name) },
        onNetworkChanged = { network -> viewModel.toggleSelectedNetwork(network) },
        onSecretSpendKeyHexChanged = { value -> viewModel.updateSecretSpendKeyHex(value) },
        onCreationDateChanged = { value -> viewModel.updateCreationDate(value) },
        onRestoreHeightChanged = { value -> viewModel.updateRestoreHeight(value) },
        remoteNodes = remoteNodes,
        selectedRemoteNodeIds = viewModel.selectedRemoteNodes,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecondStepScreen(
    showRestoreOptions: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    walletName: String,
    secretSpendKeyHex: String,
    secretSpendKeyHexError: Boolean,
    creationDate: String,
    creationDateError: Boolean,
    restoreHeight: String,
    restoreHeightError: Boolean,
    network: MoneroNetwork,
    onWalletNameChanged: (String) -> Unit = {},
    onNetworkChanged: (MoneroNetwork) -> Unit = {},
    onSecretSpendKeyHexChanged: (String) -> Unit = {},
    onCreationDateChanged: (String) -> Unit = {},
    onRestoreHeightChanged: (String) -> Unit = {},
    remoteNodes: List<RemoteNode>,
    selectedRemoteNodeIds: MutableMap<Long?, Boolean> = mutableMapOf(),
) {
    Scaffold(
        topBar = {
            Toolbar(
                title = if (showRestoreOptions) "Restore wallet" else "New wallet",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            OutlinedTextField(
                value = walletName,
                label = { Text("Wallet name") },
                onValueChange = onWalletNameChanged,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
            )
            SelectListBox(
                label = "Network",
                options = MoneroNetwork.values().map { it.name },
                selectedOption = network.name,
                onOptionClick = {
                    onNetworkChanged(MoneroNetwork.valueOf(it))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
            Text(
                text = "Remote node selection",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(16.dp),
            )
            MultiSelectRemoteNodeList(
                remoteNodes = remoteNodes,
                selectedIds = selectedRemoteNodeIds,
                modifier = Modifier
                    .padding(start = 16.dp),
            )
            if (showRestoreOptions) {
                Text(
                    text = "Deterministic wallet recovery",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(16.dp),
                )
                OutlinedTextField(
                    value = secretSpendKeyHex,
                    label = { Text("Secret spend key") },
                    onValueChange = onSecretSpendKeyHexChanged,
                    singleLine = true,
                    isError = secretSpendKeyHexError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                )
                Text(
                    text = "Synchronization",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(16.dp),
                )
                OutlinedTextField(
                    value = creationDate,
                    label = { Text("Wallet creation date") },
                    onValueChange = onCreationDateChanged,
                    singleLine = true,
                    isError = creationDateError,
                    enabled = restoreHeight.isEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                )
                OutlinedTextField(
                    value = restoreHeight,
                    label = { Text("Restore height") },
                    onValueChange = onRestoreHeightChanged,
                    singleLine = true,
                    isError = restoreHeightError,
                    enabled = creationDate.isEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val validInput = !showRestoreOptions || !(secretSpendKeyHexError || creationDateError || restoreHeightError)
                Button(
                    onClick = onCreateClick,
                    enabled = validInput,
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    Text("Finish")
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateWalletScreenPreview() {
    AppTheme {
        SecondStepScreen(
            showRestoreOptions = false,
            walletName = "Personal",
            network = DefaultMoneroNetwork,
            secretSpendKeyHex = "d2ca26e22489bd9871c910c58dee3ab08e66b9d566825a064c8c0af061cd8706",
            secretSpendKeyHexError = false,
            creationDate = "",
            creationDateError = false,
            restoreHeight = "",
            restoreHeightError = false,
            remoteNodes = listOf(RemoteNode.EMPTY),
            selectedRemoteNodeIds = mutableMapOf(),
        )
    }
}

@Preview
@Composable
private fun RestoreWalletScreenPreview() {
    AppTheme {
        SecondStepScreen(
            showRestoreOptions = true,
            walletName = "Personal",
            network = DefaultMoneroNetwork,
            secretSpendKeyHex = "d2ca26e22489bd9871c910c58dee3ab08e66b9d566825a064c8c0af061cd8706",
            secretSpendKeyHexError = false,
            creationDate = "",
            creationDateError = false,
            restoreHeight = "",
            restoreHeightError = false,
            remoteNodes = listOf(RemoteNode.EMPTY),
            selectedRemoteNodeIds = mutableMapOf(),
        )
    }
}
