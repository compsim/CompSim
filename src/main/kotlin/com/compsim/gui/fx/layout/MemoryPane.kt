package com.compsim.gui.fx.layout

import com.compsim.core.Controller
import com.compsim.core.Word
import com.compsim.gui.fx.control.CheckBoxTableCell
import com.compsim.helpers.MemoryRow
import com.compsim.util.Logger.infoln
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox


class MemoryPane(val controller: Controller): TitledPane() {
        val memoryPaneVBox = VBox()
        val memoryTable = MemoryTable(controller)
        val debuggerToolBar = DebuggerToolBar(controller)

    init {
        this.text = "MemoryRow"
        this.isCollapsible = false
        this.isAnimated = false

        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)

        VBox.setVgrow(memoryTable, Priority.ALWAYS)
        VBox.setVgrow(debuggerToolBar, Priority.NEVER)
        memoryPaneVBox.children.addAll(memoryTable, debuggerToolBar)

        this.content = memoryPaneVBox

        debuggerToolBar.showExecBtn.setOnAction {
            val pc = controller.registers.pc
            scrollTo(pc)
            selectRow(pc)
        }
    }

    class MemoryTable(val controller: Controller): TableView<MemoryRow>() {

        init {
            // Breakpoint Column
            val breakpointCol = TableColumn<MemoryRow, Boolean>("BR")
            breakpointCol.minWidth = 25.0
            breakpointCol.prefWidth = 25.0
            breakpointCol.maxWidth = 25.0
            breakpointCol.isEditable = true
            breakpointCol.isSortable = false
            breakpointCol.cellValueFactory = PropertyValueFactory("breakpoint")
            breakpointCol.setCellFactory { CheckBoxTableCell() }

            // Address Column
            val addressCol = TableColumn<MemoryRow, String>("Address")
//            addressCol.minWidth = 337.0
//            addressCol.maxWidth = Double.MAX_VALUE
            addressCol.isEditable = false
            addressCol.isSortable = false
            addressCol.cellValueFactory = PropertyValueFactory("address")

            // Value Column
            val valueCol = TableColumn<MemoryRow, String>("Value")
            valueCol.minWidth = 75.0
            valueCol.prefWidth = 75.0
            valueCol.maxWidth = 75.0
            valueCol.isEditable = true
            valueCol.isSortable = false
            valueCol.cellValueFactory = PropertyValueFactory("valueText")

            // com.compsim.Instruction Column
            val instructionCol = TableColumn<MemoryRow, String>("Instruction")
//            instructionCol.minWidth = 337.0
//            instructionCol.prefWidth = Double.MAX_VALUE
//            instructionCol.maxWidth = Double.MAX_VALUE
            instructionCol.isEditable = true
            instructionCol.isSortable = false
            instructionCol.cellValueFactory = PropertyValueFactory("instructionText")

            updateObservableListProperties(breakpointCol, addressCol, valueCol, instructionCol)

            this.columnResizePolicy = CONSTRAINED_RESIZE_POLICY
            this.isEditable = true
            this.columns.addAll(breakpointCol, addressCol, valueCol, instructionCol)

            //TODO: Prevent table column reordering
            //tableView.widthProperty().addListener(new ChangeListener<Number>()
            //{
            //    @Override
            //    public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth)
            //    {
            //        TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            //        header.reorderingProperty().addListener(new ChangeListener<Boolean>() {
            //            @Override
            //            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            //                header.setReordering(false);
            //            }
            //        });
            //    }
            //});
        }

        private fun updateObservableListProperties(breakpointCol: TableColumn<MemoryRow, Boolean>, addressCol: TableColumn<MemoryRow, String>, valueCol: TableColumn<MemoryRow, String>, instructionCol: TableColumn<MemoryRow, String>) {
            //Modifying the email property in the ObservableList
            breakpointCol.setOnEditCommit { t ->
                (t.tableView.items[t.tablePosition.row] as MemoryRow).breakpoint = t.newValue as Boolean
            }
            //Modifying the firstName property in the ObservableList
            addressCol.setOnEditCommit { t ->
                (t.tableView.items[t.tablePosition.row] as MemoryRow).address = t.newValue.toString()
            }
            //Modifying the lastName property in the ObservableList
            valueCol.setOnEditCommit { t ->
                (t.tableView.items[t.tablePosition.row] as MemoryRow).valueText = t.newValue.toString()
            }
            //Modifying the lastName property in the ObservableList
            instructionCol.setOnEditCommit { t ->
                (t.tableView.items[t.tablePosition.row] as MemoryRow).instructionText = t.newValue.toString()
            }
        }
    }

    fun updateMemoryRow(index: Int, breakpoint: Boolean, value: String, instruction: String) {
        memoryTable.items[index] = MemoryRow(breakpoint, index, value, instruction)
    }

    /**
     * Function to add a number of sample rows to the memory table.
     *
     * @param memorySize the amount of memory to be generated
     */
    fun generateMemoryRows(memorySize: Int) {
        resetData()
        for (i in 0..memorySize) {
            controller.data.add(MemoryRow(false, i, "x0000", ".FILL x0000"))
        }
        updateTable()
    }

    private fun selectRow(row: Int) {
        memoryTable.selectionModel.select(row)
    }

    private fun resetData() {
        controller.data.remove(0, controller.data.size)
    }

    private fun updateTable() {
        memoryTable.items.clear()
        memoryTable.items = controller.data
    }

    fun scrollTo(cellNum: Int) {
        if (cellNum in 0..0xffff) {
            try {
                memoryTable.scrollTo(controller.data[cellNum])
            } catch (e: NumberFormatException) {
                infoln("\"$cellNum\" is not a valid Int!")
            }
            infoln(Word.toHex(cellNum))
        } else {
            infoln("\"$cellNum\" is not in 0x0000..0xffff!")
        }
    }
}

