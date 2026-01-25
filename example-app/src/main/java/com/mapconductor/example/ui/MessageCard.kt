package com.mapconductor.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    isCardOpen: Boolean = false,
    title: String? = null,
    maxHeight: Dp = 100.dp,
    content: @Composable () -> Unit,
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(isCardOpen) }

    Card(
        modifier =
            modifier
                .animateContentSize(animationSpec = tween(300)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                title?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }
                Icon(
                    imageVector =
                        if (isCardExpanded) {
                            Icons.Default.KeyboardArrowDown
                        } else {
                            Icons.Default.KeyboardArrowUp
                        },
                    contentDescription = if (isCardExpanded) "Collapse" else "Expand",
                    modifier =
                        Modifier
                            .clickable { isCardExpanded = !isCardExpanded }
                            .size(24.dp),
                )
            }

            if (isCardExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier =
                        Modifier
                            .heightIn(max = maxHeight)
                            .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }
            }
        }
    }
}
