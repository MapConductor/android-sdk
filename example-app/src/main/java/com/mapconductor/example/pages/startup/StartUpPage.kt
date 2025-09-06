package com.mapconductor.example.pages.startup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.example.R

@Composable
fun StartUpPage(onToggleSidebar: () -> Unit = {}) {
    Scaffold { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr) + 10.dp,
                        vertical = paddingValues.calculateBottomPadding(),
                    ).verticalScroll(rememberScrollState()),
        ) {
            Image(
                painter = painterResource(R.drawable.mapcat),
                contentDescription = "MapConductor",
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Text(
                text = "MAP CONDUCTOR",
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Text(
                text = "UNIFIED MAP SDK API",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(
                modifier = Modifier.size(10.dp),
            )
            Text(
                text = stringResource(R.string.demo_application),
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(
                modifier = Modifier.size(30.dp),
            )
            Text(
                text = stringResource(R.string.startup_abstract).trimIndent(),
            )

            Text(
                text = stringResource(R.string.feature),
                fontSize = 18.sp,
                modifier =
                    Modifier.padding(
                        top = 20.dp,
                        bottom = 10.dp,
                    ),
            )
            Column {
                Text(
                    text = stringResource(R.string.feature_multi_provider).trimIndent(),
                    modifier = Modifier.padding(10.dp),
                )
                Text(
                    text = stringResource(R.string.feature_unigied_interface).trimIndent(),
                    modifier = Modifier.padding(10.dp),
                )
                Text(
                    text = stringResource(R.string.feature_high_performance).trimIndent(),
                    modifier = Modifier.padding(10.dp),
                )
                Text(
                    text = stringResource(R.string.feature_reactive_state).trimIndent(),
                    modifier = Modifier.padding(10.dp),
                )
                Text(
                    text = stringResource(R.string.feature_jetpack_compose).trimIndent(),
                    modifier = Modifier.padding(10.dp),
                )
            }
            Button(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                onClick = onToggleSidebar,
            ) {
                Text(text = "Open menu")
            }
        }
    }
}
