package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavItem("dashboard", "Home", Icons.Default.Home)
    object Transactions : NavItem("transactions", "Activity", Icons.Default.Receipt)
    object Analytics : NavItem("analytics", "Analytics", Icons.Default.PieChart)
    object Budget : NavItem("budget", "Budget", Icons.Default.AccountBalanceWallet)
    object Goals : NavItem("goals", "Goals", Icons.Default.Flag)
    object AiAdvisor : NavItem("ai_advisor", "Kai AI", Icons.Default.AutoAwesome)
    object Settings : NavItem("settings", "Settings", Icons.Default.Settings)
}

val mainNavItems = listOf(
    NavItem.Dashboard,
    NavItem.Transactions,
    NavItem.Analytics,
    NavItem.Budget,
    NavItem.Goals,
    NavItem.AiAdvisor,
    NavItem.Settings
)

@Composable
fun KaiBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            mainNavItems.forEach { item ->
                val isSelected = currentRoute == item.route
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    label = "iconColor"
                )
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f),
                    label = "bgColor"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    label = "iconScale"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("nav_${item.route}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
