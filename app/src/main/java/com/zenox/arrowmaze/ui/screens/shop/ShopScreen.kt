package com.zenox.arrowmaze.ui.screens.shop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.zenox.arrowmaze.domain.model.ShopCategory
import com.zenox.arrowmaze.ui.theme.CoinGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = ShopCategory.entries

    BackHandler { navController.popBackStack() }

    // Purchase message snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.purchaseMessage) {
        state.purchaseMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Coins display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.MonetizationOn,
                            contentDescription = null,
                            tint = CoinGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "${state.coins}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues)) {
                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(state.selectedCategory),
                    edgePadding = 16.dp
                ) {
                    categories.forEach { category ->
                        Tab(
                            selected = state.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            text = { Text(category.label, fontWeight = FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        ShopItemCard(
                            item = item,
                            onBuy = { viewModel.purchaseItem(item) },
                            onEquip = { viewModel.equipItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: com.zenox.arrowmaze.domain.model.ShopItem,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    val previewColor = Color(item.previewColor)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            // Color preview
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium),
                color = previewColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (item.isEquipped) {
                        Icon(Icons.Default.Check, contentDescription = "Equipped", tint = Color.White)
                    } else if (!item.isOwned && !item.isFree) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )

            Spacer(modifier = Modifier.height(8.dp))

            when {
                item.isEquipped -> {
                    FilledTonalButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
                        Text("Equipped", style = MaterialTheme.typography.labelSmall)
                    }
                }
                item.isOwned -> {
                    Button(onClick = onEquip, modifier = Modifier.fillMaxWidth()) {
                        Text("Equip", style = MaterialTheme.typography.labelSmall)
                    }
                }
                item.isFree -> {
                    Button(onClick = onBuy, modifier = Modifier.fillMaxWidth()) {
                        Text("Free", style = MaterialTheme.typography.labelSmall)
                    }
                }
                else -> {
                    OutlinedButton(onClick = onBuy, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${item.price}", style = MaterialTheme.typography.labelSmall, color = CoinGold)
                    }
                }
            }
        }
    }
}
