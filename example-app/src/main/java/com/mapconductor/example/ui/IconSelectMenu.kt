package com.mapconductor.example.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class IconItem<T>(
    val key: String,
    val label: String,
    @field:DrawableRes val lightIconResId: Int,
    @field:DrawableRes val darkIconResId: Int,
    val value: T,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelectMenu(
    itemList: List<IconItem<*>>,
    modifier: Modifier = Modifier.width(200.dp),
    selectedIndex: Int = 0,
    isDark: Boolean = isSystemInDarkTheme(),
    onSelect: (Int, IconItem<*>) -> Unit = { index, item ->
        println("Selected index: $index, item: $item")
    },
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = itemList.get(selectedIndex)

    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            readOnly = true,
            value = selected.label,
            onValueChange = {},
            label = { Text("Currently used") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            },
            leadingIcon = {
                with(selected) {
                    Icon(
                        painter =
                            painterResource(
                                id =
                                    when (isDark) {
                                        true -> darkIconResId
                                        false -> lightIconResId
                                    },
                            ),
                        contentDescription = label,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified,
                    )
                }
            },
            modifier =
                Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            modifier = modifier,
            onDismissRequest = { expanded = false },
        ) {
            itemList.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter =
                                    painterResource(
                                        id =
                                            when (isDark) {
                                                true -> item.darkIconResId
                                                false -> item.lightIconResId
                                            },
                                    ),
                                contentDescription = item.label,
                                modifier = Modifier.size(30.dp),
                                tint = Color.Unspecified,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.label)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelect(index, item)
                    },
                )
            }
        }
    }
}
