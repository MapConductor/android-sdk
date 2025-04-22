package com.mapconductor.example.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mapconductor.example.AppViewModelImpl
import androidx.lifecycle.viewmodel.compose.viewModel

data class IconItem(
    val key: String,
    val label: String,
    @DrawableRes val iconResId: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSelectMenu(viewModel: AppViewModelImpl = viewModel<AppViewModelImpl>()) {

    val itemList by viewModel.items.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedItem?.label ?: "",
            onValueChange = {},
            label = { Text("使用している地図SDK") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "ドロップダウン")
            },
            leadingIcon = {
                selectedItem?.let {
                    Icon(
                        painter = painterResource(id = it.iconResId),
                        contentDescription = it.label,
                        modifier = Modifier.size(30.dp),
                        tint = Color.Unspecified,
                    )
                }
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            itemList.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.label)
                        }
                    },
                    onClick = {
                        viewModel.selectItem(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
