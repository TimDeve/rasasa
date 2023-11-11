package com.timdeve.poche.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.timdeve.poche.PocheDestinations

@Preview
@Composable
fun BottomBarFull() {
    BottomBar(bottomBarHeight = 80.dp)
}

@Preview
@Composable
fun BottomBarHalf() {
    BottomBar(bottomBarHeight = 40.dp)
}

@Composable
fun BottomBar(bottomBarHeight: Dp, navController: NavHostController = rememberNavController()) {
    val shape = RoundedCornerShape(50, 50)
    BottomAppBar(
        containerColor = colorScheme.surfaceColorAtElevation(48.dp),
        contentColor = colorScheme.primary,
        modifier = Modifier
            .height(bottomBarHeight)
            .padding(start = 6.dp, end = 6.dp)
            .shadow(4.dp, shape)
            .clip(shape)
    ) {
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                    )
                    Text("Home")
                }
            },
            selected = false,
            onClick = {
                navController.popBackStack(
                    route = PocheDestinations.HOME_ROUTE,
                    inclusive = false
                )
            }
        )
        NavigationBarItem(
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Lists",
                    )
                    Text("Lists")
                }
            },
            selected = false,
            onClick = { navController.navigate(PocheDestinations.LISTS_ROUTE) }
        )
    }
}


