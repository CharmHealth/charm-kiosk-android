package com.charm.ehr.kiosk.presentation.composable


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.ehr.kiosk.domain.SOAPTemplateTableCell
import com.charm.ehr.kiosk.presentation.theme.MdColors
import com.charm.ehr.kiosk.presentation.theme.MdTheme

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun MDTable(
    modifier: Modifier = Modifier,
    tableData: List<List<SOAPTemplateTableCell>>,
    totalColSpan: Int,
    onCellValueChange: (row: Int, col: Int, value: String) -> Unit,
    isEnable: Boolean = true,
    totalOuterPadding :Dp
) {
    var cellWidth = 100.dp
    val configuration = LocalConfiguration.current
    val verticalLineSpace = 0.5.dp * (totalColSpan + 5)
    val screenWidth =
        configuration.screenWidthDp.dp - verticalLineSpace - totalOuterPadding
    var maxColSize = cellWidth * totalColSpan
    if (maxColSize <= screenWidth) {
        cellWidth = (screenWidth) / totalColSpan
    }
    maxColSize = cellWidth * totalColSpan + (0.5.dp * (totalColSpan - 1))
    Row(
        modifier.horizontalScroll(rememberScrollState())
    ) {
        Column(
            Modifier
                .border(1.dp, MdColors.Grey30, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            tableData.forEachIndexed { rowIndex, rowData ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                ) {
                    rowData.forEachIndexed { colIndex, cell ->
                        var colspan = cellWidth
                        if (cell.colspan.isNotBlank()) {
                            colspan *= cell.colspan.toInt()
                            colspan += (1.dp * (cell.colspan.toInt() - 1))
                        }
                        if (cell.type == "text") {
                            MDBasicTextField(
                                value = cell.value,
                                onValueChange = { changedValue ->
                                    onCellValueChange(rowIndex, colIndex, changedValue)
                                },
                                style = MdTheme.typography.regular14TextStyle,
                                modifier = Modifier
                                    .width(colspan)
                                    .fillMaxHeight()
                                    .heightIn(43.dp)
                                    .padding(8.dp),
                                enabled = isEnable
                            )
                        } else {
                            Box(
                                Modifier.width(colspan).heightIn(43.dp).fillMaxHeight().background(MdColors.BluishWhite).padding(8.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    cell.value,
                                    style = MdTheme.typography.semiBold14TextStyle,
                                    modifier = Modifier
                                )
                            }
                        }
                        if (rowIndex < rowData.size - 1) {
                            MDVerticalLine(Modifier, thickness = 1.dp, color = MdColors.Grey30)
                        }
                    }
                }
                if (rowIndex < tableData.size - 1) {
                    MDHorizontalLine(
                        Modifier.width(maxColSize),
                        thickness = 1.dp,
                        color = MdColors.Grey30
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewMDTable() {
    val tableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = arrayListOf()
    val cell11 = SOAPTemplateTableCell()
    cell11.value = "testfddhsfhds fdfjisdjfdsjif kjdsf"
    cell11.type = "text"
    val cell12 = SOAPTemplateTableCell()
    cell12.value = "test1"
    cell12.type = "text"
    val cell13 = SOAPTemplateTableCell()
    cell13.value = "test2"
    val row1: ArrayList<SOAPTemplateTableCell> = arrayListOf()
    row1.add(cell11)
    row1.add(cell12)
    //row1.add(cell13)
    val cell21 = SOAPTemplateTableCell()
    cell21.value = "test"
    val cell22 = SOAPTemplateTableCell()
    cell22.value = "test1"
    val cell23 = SOAPTemplateTableCell()
    cell23.value = "test2"
    val row2: ArrayList<SOAPTemplateTableCell> = arrayListOf()
    row2.add(cell21)
    row2.add(cell22)
    // row2.add(cell23)
    tableData.add(row1)
    tableData.add(row2)
    MDTable(
        modifier = Modifier.padding(horizontal = MdTheme.dimension.horizontalPadding),
        tableData = tableData,
        totalColSpan = 3, totalOuterPadding = 10.dp,
        onCellValueChange = { row, col, value ->
            tableData[row][col].value = value
        }
    )
}
