package com.mapconductor.example.ui.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun OverlaySidebar(
    items: List<SidebarItem>,
    selectedItemId: String,
    onItemClick: (SidebarItem) -> Unit,
    isVisible: Boolean,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Overlay background
    if (isVisible) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(10f)
                    .clickable { onToggleSidebar() },
        )
    }

    // Sidebar panel
    AnimatedVisibility(
        visible = isVisible,
        enter =
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 300),
            ),
        exit =
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(durationMillis = 300),
            ),
        modifier = modifier.zIndex(20f),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(280.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 16.dp),
                ) {
                    IconButton(
                        onClick = onToggleSidebar,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Close sidebar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MapConductor Demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                items.forEach { item ->
                    SidebarItemView(
                        item = item,
                        isSelected = item.id == selectedItemId,
                        isExpanded = true,
                        onClick = {
                            onItemClick(item)
                            onToggleSidebar() // Close sidebar after selection
                        },
                    )
                }
            }
        }
    }
}

// Keep the old component as well for backwards compatibility
@Composable
fun Sidebar(
    items: List<SidebarItem>,
    selectedItemId: String,
    onItemClick: (SidebarItem) -> Unit,
    isExpanded: Boolean,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OverlaySidebar(
        items = items,
        selectedItemId = selectedItemId,
        onItemClick = onItemClick,
        isVisible = isExpanded,
        onToggleSidebar = onToggleSidebar,
        modifier = modifier,
    )
}

@Composable
private fun SidebarItemView(
    item: SidebarItem,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        }

    val contentColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            Icon(
//                imageVector = item.icon,
//                contentDescription = if (isExpanded) null else item.title,
//                modifier = Modifier.size(20.dp),
//                tint = contentColor,
//            )

            AnimatedVisibility(
                visible = isExpanded,
                enter =
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300),
                    ),
                exit =
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300),
                    ),
            ) {
                Row {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
