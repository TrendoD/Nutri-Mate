package com.example.nutrimate.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.main.NavItem
import com.example.nutrimate.ui.theme.GrayText
import com.example.nutrimate.ui.theme.GreenDark
import com.example.nutrimate.ui.theme.GreenPrimary

// Colors specific to this screen
private val BackgroundColor = Color(0xFFF5F5F5)
private val CardBackgroundColor = Color.White
private val SectionTitleColor = Color(0xFF757575)
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF757575)
private val DividerColor = Color(0xFFE0E0E0)
private val DeleteAccountColor = Color(0xFFF44336)
private val ClearDataColor = Color(0xFFFF5722)

data class SettingsScreenState(
    // Empty data class - no state needed for simplified settings
    val placeholder: Unit = Unit
)

@Composable
fun SettingsScreen(
    state: SettingsScreenState,
    selectedNavItem: NavItem = NavItem.SETTINGS,
    onNavItemClick: (NavItem) -> Unit = {},
    onMyProfileClick: () -> Unit = {},
    onClearFoodLogClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    var currentNavItem by remember { mutableStateOf(selectedNavItem) }
    
    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentNavItem == item,
                        onClick = {
                            currentNavItem = item
                            onNavItemClick(item)
                        },
                        icon = {
                            if (item.useCustomIcon) {
                                val iconColor = if (currentNavItem == item) GreenDark else GrayText
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    val barWidth = size.width / 5
                                    val gap = barWidth / 2
                                    val heights = listOf(0.5f, 0.75f, 1f)
                                    heights.forEachIndexed { index, heightFraction ->
                                        val barHeight = size.height * heightFraction
                                        val left = gap + index * (barWidth + gap)
                                        drawRoundRect(
                                            color = iconColor,
                                            topLeft = Offset(left, size.height - barHeight),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = CornerRadius(4f, 4f)
                                        )
                                    }
                                }
                            } else {
                                val icon = if (currentNavItem == item) item.selectedIcon else item.unselectedIcon
                                icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        label = { Text(text = item.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenDark,
                            selectedTextColor = GreenDark,
                            unselectedIconColor = GrayText,
                            unselectedTextColor = GrayText,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Pengaturan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account Section
            SectionTitle(title = "Akun")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingsCard {
                SettingsItem(
                    title = "Profil Saya",
                    subtitle = "Lihat dan edit data diri Anda",
                    onClick = onMyProfileClick,
                    showArrow = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data Management Section
            SectionTitle(title = "Manajemen Data")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingsCard {
                Column {
                    SettingsItem(
                        title = "Hapus Semua Catatan Makanan",
                        titleColor = ClearDataColor,
                        subtitle = "Hapus semua riwayat catatan makanan Anda",
                        onClick = onClearFoodLogClick,
                        showArrow = false
                    )
                    
                    SettingsDivider()
                    
                    SettingsItem(
                        title = "Hapus Akun",
                        titleColor = DeleteAccountColor,
                        subtitle = "Hapus akun dan semua data Anda secara permanen",
                        onClick = onDeleteAccountClick,
                        showArrow = false
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer
            Text(
                text = "NutriMate - Makanan Sehat, Hidup Sehat",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = SectionTitleColor
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    titleColor: Color = TextPrimary,
    subtitle: String? = null,
    subtitleColor: Color = TextSecondary,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp)
            .then(if (!enabled) Modifier.background(Color.Transparent) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (!enabled) Modifier else Modifier)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (enabled) subtitleColor else subtitleColor.copy(alpha = 0.5f)
                )
            }
        }
        
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled) GrayText else GrayText.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        state = SettingsScreenState()
    )
}
