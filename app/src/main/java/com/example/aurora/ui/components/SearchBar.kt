package com.example.aurora.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

@ExperimentalMaterial3Api
@Composable
fun CustomSearchBar(
    state: SearchBarState,
    inputField: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.inputFieldShape,
    colors: SearchBarColors = SearchBarDefaults.colors(),
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    shadowElevation: Dp,
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = state.query,
                onQueryChange = state.onQueryChange,
                onSearch = { },
                expanded = state.active,
                onExpandedChange = state.onActiveChange,
                enabled = true,
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = null,
            )
        },
        expanded = state.active,
        onExpandedChange = state.onActiveChange,
        modifier = modifier,
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        windowInsets = SearchBarDefaults.windowInsets,
        content = { inputField() }
    )
}

data class SearchBarState(
    val query: String,
    val active: Boolean,
    val onQueryChange: (String) -> Unit,
    val onActiveChange: (Boolean) -> Unit
)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TestSearchBar() {
//    val state = SearchBarState(
//        query = "",
//        active = false,
//        onQueryChange = { },
//        onActiveChange = { }
//    )
//    CustomSearchBar(
//        state = state,
//        inputField = { /* Your input field here */ },
//
//    )
//}
//
//
//@Preview
//@Composable
//fun PreviewSearchBar() {
//    TestSearchBar()
//}