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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

data class FavoriteItem(
    val city: String,
    val maxTemp: String,
    val minTemp: String,
    val currentTemp: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen() {
    val favorites = listOf(
        FavoriteItem("New York", "30°", "20°", 25.0),
        FavoriteItem("London", "28°", "18°", 22.0),
        FavoriteItem("Tokyo", "32°", "25°", 27.0)
    )

    // Remember state for the search bar
    val queryState = remember { mutableStateOf("") }
    val activeState = remember { mutableStateOf(false) }
    val searchBarState = SearchBarState(
        query = queryState.value,
        active = activeState.value,
        onQueryChange = { newQuery -> queryState.value = newQuery },
        onActiveChange = { active -> activeState.value = active }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    .background(gradientBrush(isSystemInDarkTheme()))
    ) {
        // Custom AppBar at top
        CustomAppBar(
            title = "Favorites",
            leftIcon = {
                IconButton(onClick = {}){
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        )
        // Search bar immediately after the app bar
        SearchBar(
            state = searchBarState,
            inputField = { /* Additional content can be added here if needed */ },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // LazyColumn with FavoriteCard items
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(favorites) { favorite ->
                FavoriteCard(
                    city = favorite.city,
                    maxTemp = favorite.maxTemp,
                    minTemp = favorite.minTemp,
                    currentTemp = favorite.currentTemp,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
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

@Composable
fun TestUi() {
    FavoriteScreen()
}

@Preview
@Composable
fun WeatherCardPreview() {
    TestUi()
}
