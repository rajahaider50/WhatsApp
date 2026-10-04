package com.whatsapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.WhatsAppDarkSurface
import com.whatsapp.android.ui.theme.WhatsAppLightGreen
import com.whatsapp.android.ui.theme.WhatsAppTextLight
import com.whatsapp.android.ui.theme.WhatsAppTextMuted

enum class WhatsAppTab {
    CHATS, UPDATES, CALLS, SETTINGS
}

@Composable
fun WhatsAppBottomNav(
    selectedTab: WhatsAppTab,
    onTabSelected: (WhatsAppTab) -> Unit,
    unreadChatsCount: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhatsAppDarkSurface)
            .navigationBarsPadding()
    ) {
        NavigationBar(
            containerColor = WhatsAppDarkSurface,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            // Chats Tab
            NavigationBarItem(
                selected = selectedTab == WhatsAppTab.CHATS,
                onClick = { onTabSelected(WhatsAppTab.CHATS) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (unreadChatsCount > 0) {
                                Badge(
                                    containerColor = WhatsAppLightGreen,
                                    contentColor = Color.Black
                                ) {
                                    Text("$unreadChatsCount", fontSize = 11.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedTab == WhatsAppTab.CHATS) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Chats"
                        )
                    }
                },
                label = { Text("Chats", fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = WhatsAppTextLight,
                    indicatorColor = WhatsAppLightGreen,
                    unselectedIconColor = WhatsAppTextMuted,
                    unselectedTextColor = WhatsAppTextMuted
                )
            )

            // Updates / Status Tab
            NavigationBarItem(
                selected = selectedTab == WhatsAppTab.UPDATES,
                onClick = { onTabSelected(WhatsAppTab.UPDATES) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == WhatsAppTab.UPDATES) Icons.Default.Adjust else Icons.Outlined.Adjust,
                        contentDescription = "Updates"
                    )
                },
                label = { Text("Updates", fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = WhatsAppTextLight,
                    indicatorColor = WhatsAppLightGreen,
                    unselectedIconColor = WhatsAppTextMuted,
                    unselectedTextColor = WhatsAppTextMuted
                )
            )

            // Calls Tab
            NavigationBarItem(
                selected = selectedTab == WhatsAppTab.CALLS,
                onClick = { onTabSelected(WhatsAppTab.CALLS) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == WhatsAppTab.CALLS) Icons.Default.Call else Icons.Outlined.Call,
                        contentDescription = "Calls"
                    )
                },
                label = { Text("Calls", fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = WhatsAppTextLight,
                    indicatorColor = WhatsAppLightGreen,
                    unselectedIconColor = WhatsAppTextMuted,
                    unselectedTextColor = WhatsAppTextMuted
                )
            )

            // Settings Tab
            NavigationBarItem(
                selected = selectedTab == WhatsAppTab.SETTINGS,
                onClick = { onTabSelected(WhatsAppTab.SETTINGS) },
                icon = {
                    Icon(
                        imageVector = if (selectedTab == WhatsAppTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { Text("Settings", fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = WhatsAppTextLight,
                    indicatorColor = WhatsAppLightGreen,
                    unselectedIconColor = WhatsAppTextMuted,
                    unselectedTextColor = WhatsAppTextMuted
                )
            )
        }
    }
}
