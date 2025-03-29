package com.example.aurora.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aurora.ui.components.CustomAppBar
import com.example.aurora.ui.components.SearchBar
import com.example.aurora.ui.components.SearchBarState
import com.example.aurora.ui.theme.gradientBrush
import kotlin.toString

data class FavoriteItem(
    val city: String,
    val maxTemp: String,
    val minTemp: String,
    val currentTemp: Double
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FavViewModel,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val queryState = remember { mutableStateOf("") }
    val activeState = remember { mutableStateOf(false) }
    val searchBarState = SearchBarState(
        query = queryState.value,
        active = activeState.value,
        onQueryChange = { newQuery -> queryState.value = newQuery },
        onActiveChange = { active -> activeState.value = active }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush(isSystemInDarkTheme()))
    ) {
        CustomAppBar(
            title = "Favorites",
            leftIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        )

//        SearchBar(
//            state = searchBarState,
//            inputField = { },
//            modifier = Modifier.padding(horizontal = 16.dp)
//        )

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = onSearchClick,
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search for a city...",
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (uiState) {
            is FavUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            }
            is FavUiState.Success -> {
                val forecasts = (uiState as FavUiState.Success).forecasts
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(forecasts) { forecast ->
                        // Safe extraction of temperature values
                        val firstItem = forecast.list?.firstOrNull()
                        val maxTemp = firstItem?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt() ?:
                        forecast.list?.mapNotNull { it?.main?.tempMax?.toString()?.toDoubleOrNull()?.toInt() }?.maxOrNull() ?: 0
                        val minTemp = firstItem?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt() ?:
                        forecast.list?.mapNotNull { it?.main?.tempMin?.toString()?.toDoubleOrNull()?.toInt() }?.minOrNull() ?: 0
                        val currentTemp = firstItem?.main?.temp?.toString()?.toDoubleOrNull() ?: 0.0

                        FavoriteCard(
                            city = forecast.city.name,
                            maxTemp = "$maxTemp°",
                            minTemp = "$minTemp°",
                            currentTemp = currentTemp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )
                    }
                }
            }
            is FavUiState.Error -> {
                Text(
                    text = (uiState as FavUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun FavoriteCard(
    city: String,
    maxTemp: String,
    minTemp: String,
    currentTemp: Double,
    modifier: Modifier = Modifier
) {

    var isDark : Boolean = isSystemInDarkTheme()
    var background = gradientBrush(isDark)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)), // Adjusted to match the UI
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(80.dp)
            .background(Color.White.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = city,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text(
                        text = "$maxTemp /",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$minTemp ",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

            }
            Text(
                text = "$currentTemp°",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

//@Composable
//fun TestUi() {
//    FavoriteScreen()
//}
//
//@Preview
//@Composable
//fun WeatherCardPreview() {
//    TestUi()
//}
