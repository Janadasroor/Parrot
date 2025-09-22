package com.janad.parrot.presentation.screens.main

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.janad.parrot.data.api.NetworkModule.IMAGES_PATH
import com.janad.parrot.data.models.network.Media
import com.janad.parrot.data.models.network.Product
import com.janad.parrot.data.models.network.UploadState
import com.janad.parrot.presentation.components.ProductCard
import com.janad.parrot.presentation.components.ProductDetailDialog
import com.janad.parrot.presentation.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val productsState by mainViewModel.products.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val deleteState by mainViewModel.deleteState.collectAsState()
    val context = LocalContext.current
    // Load products once
    LaunchedEffect(Unit) {
        mainViewModel.getProducts()
        mainViewModel.sendTestMsg("Hello World")
    }
    LaunchedEffect(deleteState) {
        if (deleteState is UploadState.Error) {
            Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()
        }
        if (deleteState is UploadState.Success) {
            Toast.makeText(context, "Delete Success", Toast.LENGTH_SHORT).show()
        }

        if (deleteState is UploadState.Success) {
            mainViewModel.getProducts()
        }
    }

    //TODO : Remove
    if(productsState?.results?.isNotEmpty() ?: false){
        val fp = productsState?.results?.get(0)
        Log.e("ProductsScreen Debug", productsState?.results?.size.toString())
        Log.e("title", fp?.title ?: "null")
        fp?.let { mainViewModel.updateProduct(fp.id , "ccc edited" , fp.description,it.price.toDouble(),fp.media) }
    }
    else{
        Log.e("ProductsScreen Debug", "Empty")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Content
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Products",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    modifier = Modifier.shadow(8.dp)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                            )
                        )
                    )
            ) {
                when {
                    isLoading -> {
                        LoadingState()
                    }

                    productsState != null && productsState!!.results.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(productsState!!.results) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { selectedProduct = product },
                                    onEdit = { },
                                    onDelete = {
                                        mainViewModel.deleteProduct(product.id)
                                    }
                                )
                            }
                        }
                    }

                    else -> {
                        Log.e("ProductsScreen", productsState?.results?.size.toString())
                        EmptyState()
                    }
                }
            }
        }

        // Product Detail Dialog
        selectedProduct?.let { product ->
            ProductDetailDialog(
                product = product,
                onDismiss = { selectedProduct = null }
            )
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Loading amazing products...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                "No products available",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                "Check back later for new items",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}





@Composable
fun MediaThumbnail(
    media: Media,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            when (media.fileType) {
                "image" -> {
                    AsyncImage(
                        model = IMAGES_PATH + media.fileName,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                }
                "video" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video thumbnail",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

