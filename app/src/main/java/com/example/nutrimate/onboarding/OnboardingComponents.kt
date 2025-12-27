package com.example.nutrimate.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrimate.ui.theme.GrayDark
import com.example.nutrimate.ui.theme.GrayLight
import com.example.nutrimate.ui.theme.GreenPrimary
import com.example.nutrimate.ui.theme.PageBackground

/**
 * Kartu pilihan gender dengan icon dan teks
 */
@Composable
fun OnboardingGenderCard(
    text: String,
    icon: String, // "♂" or "♀"
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.1f) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) GreenPrimary else GrayLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 48.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

/**
 * Kartu opsi untuk Activity Level, Goal dengan title dan description
 */
@Composable
fun OnboardingOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrayLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = GrayDark
            )
        }
    }
}

/**
 * Card menampilkan nilai dengan unit (untuk height/weight)
 */
@Composable
fun ValueDisplayCard(
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = GrayDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Slider horizontal dengan label min/max
 */
@Composable
fun OnboardingHorizontalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueFrom: Float,
    valueTo: Float,
    steps: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueFrom..valueTo,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = GreenPrimary,
                inactiveTrackColor = GrayLight
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valueFrom.toInt().toString(),
                fontSize = 14.sp,
                color = GrayDark
            )
            Text(
                text = valueTo.toInt().toString(),
                fontSize = 14.sp,
                color = GrayDark
            )
        }
    }
}

/**
 * Slider vertikal dengan label min/max untuk height
 */
@Composable
fun OnboardingVerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueFrom: Float,
    valueTo: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical slider using rotation
        Box(
            modifier = Modifier
                .height(250.dp)
                .width(50.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Max label at top
                Text(
                    text = valueTo.toInt().toString(),
                    fontSize = 14.sp,
                    color = GrayDark
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rotated slider
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(40.dp)
                ) {
                    Slider(
                        value = value,
                        onValueChange = onValueChange,
                        valueRange = valueFrom..valueTo,
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = -90f
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    Constraints(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxWidth
                                    )
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.place(-placeable.width, 0)
                                }
                            },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = GreenPrimary,
                            inactiveTrackColor = GrayLight
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Min label at bottom
                Text(
                    text = valueFrom.toInt().toString(),
                    fontSize = 14.sp,
                    color = GrayDark
                )
            }
        }
    }
}

/**
 * Judul halaman onboarding - "Selamat Datang!"
 */
@Composable
fun OnboardingTitle(
    text: String = "Selamat Datang!",
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = modifier
    )
}

/**
 * Subjudul/pertanyaan halaman onboarding
 */
@Composable
fun OnboardingSubtitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = GrayDark,
        modifier = modifier
    )
}

/**
 * Progress indicator dengan segmen dots
 */
@Composable
fun SegmentedProgressIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        color = if (index <= currentPage) GreenPrimary else GrayLight,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

/**
 * Wheel picker item untuk age selector
 */
@Composable
fun WheelPickerItem(
    value: Int,
    isSelected: Boolean,
    distanceFromCenter: Int, // 0 = center, 1 = adjacent, 2 = far
    modifier: Modifier = Modifier
) {
    val alpha = when (distanceFromCenter) {
        0 -> 1f
        1 -> 0.6f
        else -> 0.3f
    }
    
    val fontSize = when (distanceFromCenter) {
        0 -> 72.sp
        1 -> 48.sp
        else -> 32.sp
    }
    
    val fontWeight = if (distanceFromCenter == 0) FontWeight.Bold else FontWeight.Normal
    val color = if (distanceFromCenter == 0) Color.Black else GrayDark.copy(alpha = alpha)
    
    Text(
        text = value.toString(),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}
