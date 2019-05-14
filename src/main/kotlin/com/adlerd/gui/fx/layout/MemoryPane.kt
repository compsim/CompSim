package com.adlerd.gui.fx.layout

import com.adlerd.Machine
import com.adlerd.Word
import com.adlerd.gui.fx.control.CheckBoxTableCell
import com.adlerd.helpers.MemoryRow
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TitledPane
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox


class MemoryPane(val machine: Machine): TitledPane() {
        val memoryPaneVBox = VBox()
        val memoryTable = MemoryTable(machine)
        val debuggerToolBar = DebuggerToolBar(machine)

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
            scrollTo(0x3000)
            selectRow(0x3000)
        }
    }

    class MemoryTable(val machine: Machine): TableView<MemoryRow>() {

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

            // com.adlerd.Instruction Column
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
            machine.data.add(MemoryRow(false, i, "x0000", ".FILL x0000"))
        }
        updateTable()
    }

    private fun selectRow(row: Int) {
        memoryTable.selectionModel.select(row)
    }

    private fun resetData() {
        machine.data.remove(0, machine.data.size)
    }

    private fun updateTable() {
        memoryTable.items.clear()
        memoryTable.items = machine.data
    }

    fun scrollTo(cellNum: Int) {
        if (cellNum in 0..0xffff) {
            try {
                memoryTable.scrollTo(machine.data[cellNum])
            } catch (e: NumberFormatException) {
                println("\"$cellNum\" is not a valid Int!")
            }
            println(Word.toHex(cellNum))
        } else {
            println("\"$cellNum\" is not in 0x0000..0xffff!")
        }
    }
}

