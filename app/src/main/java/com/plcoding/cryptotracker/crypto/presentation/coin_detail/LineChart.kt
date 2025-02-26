package com.plcoding.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.cryptotracker.crypto.domain.CoinPrice
import com.plcoding.cryptotracker.ui.theme.CryptoTrackerTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    dataPoints: List<DataPoint>,
    style: ChartStyle,
    visibleDataPointIndices: IntRange,
    units: String,
    selectedDataPoint: DataPoint? = null,
    onSelectedDataPoint: (DataPoint) -> Unit = {},
    onXLabelWidthChange: (Float) -> Unit = {},
    showHelperLines: Boolean = true
) {

    val textStyle = LocalTextStyle.current.copy(
        fontSize = style.labelFontSize
    )

    val visibleDataPoints = remember(dataPoints, visibleDataPointIndices) {
        dataPoints.slice(visibleDataPointIndices)
    }

    val maxYValue = remember(visibleDataPoints) {
        visibleDataPoints.maxOfOrNull { it.y } ?: 0f
    }
    val minYValue = remember(visibleDataPoints) {
        visibleDataPoints.minOfOrNull { it.y } ?: 0f
    }

    val measurer = rememberTextMeasurer()

    var xLabelWidth by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(key1 = xLabelWidth) {
        onXLabelWidthChange(xLabelWidth)
    }


    val selectedDataPointIndex = remember(selectedDataPoint) {
        dataPoints.indexOf(selectedDataPoint)
    }

    var drawPoints by remember {
        mutableStateOf(listOf<DataPoint>())
    }

    var isShowingDataPoints by remember {
        mutableStateOf(selectedDataPoint != null)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {

        //X- Axis Label Calculations
        val minLabelSpacingYPx = style.minYLabelSpacing.toPx()
        val verticalPaddingPx = style.verticalPadding.toPx()
        val horizontalPaddingPx = style.horizontalPadding.toPx()
        val xAxisLabelSpacingPx = style.xAxisLabelSpacing.toPx()

        val xLabelTextLayoutResults = visibleDataPoints.map {
            measurer.measure(
                text = it.xLabel,
                style = textStyle.copy(textAlign = TextAlign.Center)
            )
        }

        val maxXLabelWidth = xLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0
        val maxXLabelHeight = xLabelTextLayoutResults.maxOfOrNull { it.size.height } ?: 0
        val maxXLabelLineCount = xLabelTextLayoutResults.maxOfOrNull { it.lineCount } ?: 0
        val xLabelLineHeight = maxXLabelHeight / maxXLabelLineCount

        val viewPortHeightPx = size.height -
                (maxXLabelHeight + 2 * verticalPaddingPx
                        + xLabelLineHeight + xAxisLabelSpacingPx)

        val viewPortTopY = verticalPaddingPx + xLabelLineHeight + 10f
        val viewPortRightX = size.width
        val viewPortBottomY = viewPortTopY + viewPortHeightPx

        //Y - AXIS Labels Calculations
        val labelViewPortHeightPx = viewPortHeightPx + xLabelLineHeight
        val labelCountExcludingLastLabel =
            ((labelViewPortHeightPx / (xLabelLineHeight + minLabelSpacingYPx)) - 1).toInt()
        val valueIncrement = (maxYValue - minYValue) / labelCountExcludingLastLabel

        val yLabels = (0..labelCountExcludingLastLabel).map {
            ValueLabel(
                value = minYValue - (valueIncrement * it),
                unit = units
            )
        }

        val yLabelTextLayoutResults = yLabels.map {
            measurer.measure(
                text = it.formatted(),
                style = textStyle
            )
        }
        val maxYLabelWidth = yLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0
        val viewPortLeftX = 2f * horizontalPaddingPx + maxYLabelWidth
        val viewPort = Rect(
            left = viewPortLeftX,
            top = viewPortTopY,
            right = viewPortRightX,
            bottom = viewPortBottomY
        )

        drawRect(
            color = Color.Green,
            topLeft = viewPort.topLeft,
            size = viewPort.size
        )


        xLabelWidth = maxXLabelWidth + xAxisLabelSpacingPx
        //X - Axis
        xLabelTextLayoutResults.forEachIndexed { index, textLayoutResult ->
            val x = viewPortLeftX + xAxisLabelSpacingPx / 2f +
                    xLabelWidth * index
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x,
                    y = viewPortBottomY + xAxisLabelSpacingPx
                ),
                color = if (index == selectedDataPointIndex) {
                    style.selectedColor
                } else {
                    style.unSelectedColor
                }
            )

            //Vertical Lines
            if (showHelperLines) {
                drawLine(
                    color = if (index == selectedDataPointIndex) {
                        style.selectedColor
                    } else {
                        style.unSelectedColor
                    },
                    start = Offset(
                        x = x + textLayoutResult.size.width / 2f,
                        y = viewPortBottomY
                    ),
                    end = Offset(
                        x = x + textLayoutResult.size.width / 2f,
                        y = viewPortTopY
                    ),
                    strokeWidth = if (index == selectedDataPointIndex) {
                           style.helperLinesThicknessPx * 1.8f
                    } else {
                        style.axisLinesThicknessPx
                    }
                )
            }
        }

        //Y - Axis
        val heightRequiredForLabels = xLabelLineHeight * (labelCountExcludingLastLabel + 1)
        val remainingHeightForLabels = (labelViewPortHeightPx - heightRequiredForLabels)
        val spaceBetweenLabels = remainingHeightForLabels / labelCountExcludingLastLabel
        yLabelTextLayoutResults.forEachIndexed { index, textLayoutResult ->
            val x = horizontalPaddingPx + maxYLabelWidth - textLayoutResult.size.width.toFloat()
            val y = viewPortTopY +
                    index * (xLabelLineHeight + spaceBetweenLabels) -
                    xLabelLineHeight / 2f
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = x,
                    y = y
                ),
                color = style.unSelectedColor
            )

            //Helper Lines
            if (showHelperLines) {
                drawLine(
                    color = style.unSelectedColor,
                    start = Offset(
                        x = viewPortLeftX,
                        y = y + textLayoutResult.size.height.toFloat() / 2f
                    ),
                    end = Offset(
                        x = viewPortRightX,
                        y = y + textLayoutResult.size.height.toFloat() / 2f
                    ),
                    strokeWidth = style.helperLinesThicknessPx
                )
            }
        }
    }
}

@Preview(widthDp = 1000)
@Composable
private fun LineChartPreview() {
    CryptoTrackerTheme {
        val coinHistoryRandomized = remember {
            (1..20).map {
                CoinPrice(
                    priceUsd = Random.nextFloat() * 1000.0,
                    dateTime = ZonedDateTime.now().plusHours(it.toLong())
                )
            }
        }
        val style = ChartStyle(
            chartLineColor = Color.Black,
            unSelectedColor = Color(0xFF7C7C7C),
            selectedColor = Color.Black,
            helperLinesThicknessPx = 1f,
            axisLinesThicknessPx = 5f,
            labelFontSize = 14.sp,
            minYLabelSpacing = 25.dp,
            verticalPadding = 8.dp,
            horizontalPadding = 8.dp,
            xAxisLabelSpacing = 8.dp
        )
        val dataPoints = remember {
            coinHistoryRandomized.map {
                DataPoint(
                    x = it.dateTime.hour.toFloat(),
                    y = it.priceUsd.toFloat(),
                    xLabel = DateTimeFormatter
                        .ofPattern("ha\nM/d")
                        .format(it.dateTime),
                    yLabel = String()
                )
            }
        }
        LineChart(
            dataPoints = dataPoints,
            style = style,
            visibleDataPointIndices = 0..19,
            units = "$",
            modifier = Modifier
                .width(700.dp)
                .height(300.dp)
                .background(Color.White),
            selectedDataPoint = dataPoints[1]
        )
    }
}