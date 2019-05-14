package com.adlerd.gui.swing

import com.adlerd.CommandLine
import com.adlerd.Console
import com.adlerd.util.ErrorLog
import com.adlerd.CompSim
import com.adlerd.Machine
import com.adlerd.util.exceptions.GenericException
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import javax.swing.*
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener
import javax.swing.filechooser.FileFilter
import javax.swing.table.TableCellRenderer

class LC3GUI(private val machine: Machine, var2: CommandLine) : ActionListener, TableModelListener {
    //   public static String LOOKANDFEEL = "Motif";
    //   public static String LOOKANDFEEL = "GTK+";
    private val haltedColor: Color = Color(161, 37, 40)
    private val runningColor: Color = Color(43, 129, 51)
    private val suspendedColor: Color = Color(209, 205, 93)
    private val continueButton: JButton = JButton("Continue")
    private val nextButton: JButton = JButton("Next")
    private val stepButton: JButton = JButton("Step")
    private val stopButton: JButton = JButton("Stop")
    private val fileChooser: JFileChooser = JFileChooser(".")
    private val frame: JFrame = JFrame("Little Computer Kotlin (KT) - ${CompSim.VERSION} - ${CompSim.isa}")
    private val statusLabel: JLabel = JLabel("")
    private val aboutMenu: JMenu = JMenu("About")
    private val fileMenu: JMenu = JMenu("File")
    private val menuBar: JMenuBar = JMenuBar()
    private val openItem: JMenuItem = JMenuItem("Open .obj File")
    private val quitItem: JMenuItem = JMenuItem("Quit")
    private val commandItem: JMenuItem = JMenuItem("Open Command Output Window")
    private val versionItem: JMenuItem = JMenuItem("Simulator Version")
    private val leftPanel: JPanel = JPanel()
    private val controlPanel: JPanel = JPanel()
    private val memoryPanel: JPanel = JPanel(BorderLayout())
    private val devicePanel: JPanel = JPanel()
    private val registerPanel: JPanel = JPanel()
    private val openActionCommand: String = "Open"
    private val quitActionCommand: String = "Quit"
    private val openCOWActionCommand: String = "OutputWindow"
    private val versionActionCommand: String = "Version"
    private val nextButtonCommand: String = "Next"
    private val stepButtonCommand: String = "Step"
    private val continueButtonCommand: String = "Continue"
    private val stopButtonCommand: String = "Stop"
    private val statusLabelRunning: String = "    Running "
    private val statusLabelSuspended: String = "Suspended "
    private val statusLabelHalted: String = "       Halted "

    private val regTable: JTable
    private val commandPanel: CommandLinePanel
    private val commandOutputWindow: CommandOutputWindow
    private val memoryTable: JTable
    private val memScrollPane: JScrollPane
    private val ioPanel: TextConsolePanel
    private val video: VideoConsole

    init {
        val registerFile = machine.registers
        this.regTable = JTable(registerFile)
        var tableColumn = this.regTable.columnModel.getColumn(0)
        tableColumn.maxWidth = 30
        tableColumn.minWidth = 30
        tableColumn = this.regTable.columnModel.getColumn(2)
        tableColumn.maxWidth = 30
        tableColumn.minWidth = 30
        val memory = machine.memory

        this.memoryTable = object : JTable(memory) {
            override fun prepareRenderer(tableCellRenderer: TableCellRenderer, row: Int, column: Int): Component {
                val component = super.prepareRenderer(tableCellRenderer, row, column)
                if (column == 0) {
                    val jCheckBox = JCheckBox()
                    if (row < 65024) {
                        if (this@LC3GUI.machine.memory.isBreakPointSet(row)) {
                            jCheckBox.isSelected = true
                            jCheckBox.background = BreakPointColor
                            jCheckBox.foreground = BreakPointColor
                        } else {
                            jCheckBox.isSelected = false
                            jCheckBox.background = this.background
                        }
                    } else {
                        jCheckBox.isEnabled = false
                        jCheckBox.background = Color.lightGray
                    }

                    return jCheckBox
                } else {
                    when {
                        row == this@LC3GUI.machine.registers.pc -> component.background =
                            pcColor
                        this@LC3GUI.machine.memory.isBreakPointSet(row) -> component.background =
                            BreakPointColor
                        else -> component.background = this.background
                    }

                    return component
                }
            }

            override fun tableChanged(tableModelEvent: TableModelEvent) {
                if (machine != null) {
                    super.tableChanged(tableModelEvent)
                }

            }
        }
        this.memScrollPane = object : JScrollPane(this.memoryTable) {
            override fun createVerticalScrollBar(): JScrollBar {
                return HighlightScrollBar(machine)
            }
        }
        memScrollPane.verticalScrollBar.blockIncrement = memoryTable.model.rowCount / 512
        memScrollPane.verticalScrollBar.unitIncrement = 1
        tableColumn = memoryTable.columnModel.getColumn(0)
        tableColumn.maxWidth = 20
        tableColumn.minWidth = 20
        tableColumn.cellEditor = DefaultCellEditor(JCheckBox())
        tableColumn = memoryTable.columnModel.getColumn(2)
        tableColumn.minWidth = 50
        tableColumn.maxWidth = 50
        this.commandPanel = CommandLinePanel(machine, var2)
        this.commandOutputWindow = CommandOutputWindow("Command Output")
        val windowListener = object : WindowListener {
            override fun windowActivated(windowEvent: WindowEvent) {}

            override fun windowClosed(windowEvent: WindowEvent) {}

            override fun windowClosing(windowEvent: WindowEvent) {
                this@LC3GUI.commandOutputWindow.isVisible = false
            }

            override fun windowDeactivated(windowEvent: WindowEvent) {}

            override fun windowDeiconified(windowEvent: WindowEvent) {}

            override fun windowIconified(windowEvent: WindowEvent) {}

            override fun windowOpened(windowEvent: WindowEvent) {}
        }
        this.commandOutputWindow.addWindowListener(windowListener)
        this.commandOutputWindow.setSize(700, 600)
        Console.registerConsole(this.commandPanel)
        Console.registerConsole(this.commandOutputWindow)
        this.ioPanel =
            TextConsolePanel(machine.memory.keyBoardDevice, machine.memory.monitorDevice)
        this.ioPanel.minimumSize = Dimension(256, 85)
        this.video = VideoConsole(machine)
        this.commandPanel.LC3GUI = this
    }

    /**
     * Set up program LC3GUI
     */
    fun setUpGUI() {
        initLookAndFeel()
        JFrame.setDefaultLookAndFeelDecorated(true)
        this.machine.setStoppedListener(this.commandPanel)
        this.fileChooser.fileSelectionMode = 2
        this.fileChooser.addChoosableFileFilter(object : FileFilter() {
            override fun accept(file: File): Boolean {
                return if (file.isDirectory) {
                    true
                } else {
                    val fileName = file.name
                    fileName != null && fileName.toLowerCase().endsWith(".obj")
                }
            }

            override fun getDescription(): String {
                return "*.obj"
            }
        })
        this.openItem.actionCommand = "Open"
        this.openItem.addActionListener(this)
        this.fileMenu.add(this.openItem)
        this.commandItem.actionCommand = "OutputWindow"
        this.commandItem.addActionListener(this)
        this.fileMenu.add(this.commandItem)
        this.fileMenu.addSeparator()
        this.quitItem.actionCommand = "Quit"
        this.quitItem.addActionListener(this)
        this.fileMenu.add(this.quitItem)
        this.versionItem.actionCommand = "Version"
        this.versionItem.addActionListener(this)
        this.aboutMenu.add(this.versionItem)
        this.menuBar.add(this.fileMenu)
        this.menuBar.add(this.aboutMenu)
        this.frame.jMenuBar = this.menuBar
        this.setupControlPanel()
        this.setupDevicePanel()
        this.setupMemoryPanel()
        this.setupRegisterPanel()
        this.regTable.model.addTableModelListener(this)
        this.frame.contentPane.layout = GridBagLayout()
        var constraints = GridBagConstraints()
        constraints.fill = 1
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridwidth = 2
        constraints.weighty = 1.0
        constraints.gridwidth = 0
        this.frame.contentPane.add(this.controlPanel, constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.gridwidth = 1
        constraints.gridheight = 1
        constraints.weightx = 0.0
        constraints.fill = 2
        this.frame.contentPane.add(this.registerPanel, constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.weightx = 0.0
        constraints.gridheight = 1
        constraints.gridwidth = 1
        constraints.fill = 1
        this.frame.contentPane.add(this.devicePanel, constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 1
        constraints.gridy = 1
        constraints.gridheight = 2
        constraints.gridwidth = 0
        constraints.fill = 1
        constraints.weightx = 1.0
        this.frame.contentPane.add(this.memoryPanel, constraints)
        this.frame.size = Dimension(700, 725)
        this.frame.defaultCloseOperation = 3
        this.frame.pack()
        this.frame.isVisible = true
        this.scrollToPC()
        this.commandPanel.actionPerformed(null as ActionEvent?)
    }

    /**
     * Set up com.adlerd.Memory Panel
     */
    private fun setupMemoryPanel() {
        this.memoryPanel.add(this.memScrollPane, "Center")
        this.memoryPanel.minimumSize = Dimension(400, 100)
        this.memoryPanel.border =
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("com.adlerd.Memory"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
        this.memoryTable.model.addTableModelListener(this)
        this.memoryTable.model.addTableModelListener(this.video)
        this.memoryTable.model.addTableModelListener(this.memScrollPane.verticalScrollBar as HighlightScrollBar)
        this.memoryTable.preferredScrollableViewportSize = Dimension(400, 460)
    }

    /**
     * Set up Control Panel
     */
    private fun setupControlPanel() {
        val var1 = true
        this.controlPanel.layout = GridBagLayout()
        var constraints = GridBagConstraints()
        constraints.fill = 2
        this.nextButton.actionCommand = "Next"
        this.nextButton.addActionListener(this)
        constraints.weightx = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
        this.controlPanel.add(this.nextButton, constraints)
        this.stepButton.actionCommand = "Step"
        this.stepButton.addActionListener(this)
        constraints.gridx = 1
        constraints.gridy = 0
        this.controlPanel.add(this.stepButton, constraints)
        this.continueButton.actionCommand = "Continue"
        this.continueButton.addActionListener(this)
        constraints.gridx = 2
        constraints.gridy = 0
        this.controlPanel.add(this.continueButton, constraints)
        this.stopButton.actionCommand = "Stop"
        this.stopButton.addActionListener(this)
        constraints.gridx = 3
        constraints.gridy = 0
        this.controlPanel.add(this.stopButton, constraints)
        constraints.gridx = 4
        constraints.gridy = 0
        constraints.fill = 0
        constraints.anchor = 22
        this.setStatusLabelSuspended()
        this.controlPanel.add(this.statusLabel, constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.gridwidth = 6
        this.controlPanel.add(Box.createRigidArea(Dimension(5, 5)), constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 2
        constraints.gridwidth = 6
        constraints.gridheight = 1
        constraints.ipady = 100
        constraints.weightx = 1.0
        constraints.weighty = 1.0
        constraints.fill = 1
        this.controlPanel.add(this.commandPanel, constraints)
        this.controlPanel.minimumSize = Dimension(100, 150)
        this.controlPanel.preferredSize = Dimension(100, 150)
        this.controlPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Controls"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )
        this.controlPanel.isVisible = true
    }

    /**
     * Set up Register Panel
     */
    private fun setupRegisterPanel() {
        this.registerPanel.layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 1.0
        constraints.fill = 2
        this.registerPanel.add(this.regTable, constraints)
        this.registerPanel.border =
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Registers"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
        this.registerPanel.isVisible = true
    }

    /**
     * Set up Device Panel
     */
    private fun setupDevicePanel() {
        this.devicePanel.layout = GridBagLayout()
        var constraints = GridBagConstraints()
        constraints.fill = 10
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.weightx = 1.0
        this.devicePanel.add(this.video, constraints)
        constraints = GridBagConstraints()
        constraints.gridx = 0
        constraints.gridy = 1
        constraints.weightx = 1.0
        constraints.fill = 0
        this.devicePanel.add(this.ioPanel, constraints)
        this.devicePanel.border =
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Devices"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
        this.devicePanel.isVisible = true
    }

    fun scrollToIndex(index: Int) {
        this.memoryTable.scrollRectToVisible(this.memoryTable.getCellRect(index, 0, true))
    }

    fun scrollToPC() {
        this.scrollToPC(0)
    }

    fun scrollToPC(var1: Int) {
        val var2 = this.machine.registers.pc + var1
        this.memoryTable.scrollRectToVisible(this.memoryTable.getCellRect(var2, 0, true))
    }

    override fun tableChanged(var1: TableModelEvent) {
        if (!this.machine.isContinueMode) {
        }

    }

    /**
     * Function to confirm exiting program
     */
    fun confirmExit() {
        val var1 = arrayOf<Any>("Yes", "No")
        val var2 = JOptionPane.showOptionDialog(
            this.frame,
            "Are you sure you want to quit?",
            "Quit verification",
            0,
            3,
            null as Icon?,
            var1,
            var1[1]
        )
        if (var2 == 0) {
            this.machine.cleanup()
            System.exit(0)
        }

    }

    override fun actionPerformed(actionEvent: ActionEvent) {
        try {
            var index: Int
            try {
                index = Integer.parseInt(actionEvent.actionCommand)
                this.scrollToIndex(index)
                return
            } catch (numberFormatException: NumberFormatException) {
                when {
                    "Next" == actionEvent.actionCommand -> this.machine.executeNext()
                    "Step" == actionEvent.actionCommand -> this.machine.executeStep()
                    "Continue" == actionEvent.actionCommand -> this.machine.executeMany()
                    "Quit" == actionEvent.actionCommand -> this.confirmExit()
                    "Stop" == actionEvent.actionCommand -> Console.println(this.machine.stopExecution(true))
                    "OutputWindow" == actionEvent.actionCommand -> this.commandOutputWindow.isVisible = true
                    "Version" == actionEvent.actionCommand -> JOptionPane.showMessageDialog(
                        this.frame,
                        CompSim.VERSION,
                        "Version",
                        1
                    )
                    "Open" == actionEvent.actionCommand -> {
                        index = this.fileChooser.showOpenDialog(this.frame)
                        if (index == 0) {
                            val objectFile = this.fileChooser.selectedFile
                            Console.println(this.machine.loadObjectFile(objectFile))
                        } else {
                            Console.println("Open command cancelled by user.")
                        }
                    }
                }
            }

        } catch (genericException: GenericException) {
            genericException.showMessageDialog(this.frame)
        }

    }

    fun setStatusLabelRunning() {
        this.statusLabel.text = "    Running "
        this.statusLabel.foreground = this.runningColor
    }

    fun setStatusLabelSuspended() {
        this.statusLabel.text = "Suspended "
        this.statusLabel.foreground = this.suspendedColor
    }

    fun setStatusLabelHalted() {
        this.statusLabel.text = "       Halted "
        this.statusLabel.foreground = this.haltedColor
    }

    fun setStatusLabel(var1: Boolean) {
        if (var1) {
            this.setStatusLabelSuspended()
        } else {
            this.setStatusLabelHalted()
        }

    }

    fun setTextConsoleEnabled(isEnabled: Boolean) {
        this.ioPanel.isEnabled = isEnabled
    }

    fun reset() {
        this.setTextConsoleEnabled(true)
        this.commandPanel.reset()
        this.video.reset()
        this.scrollToPC()
    }

    companion object {
        //   public static String LOOKANDFEEL = "Metal";
        var LOOKANDFEEL: String? = "System"
        val BreakPointColor = Color(241, 103, 103)
        val pcColor: Color = Color.YELLOW

        fun initLookAndFeel() {
            var theme: String? = null
            JFrame.setDefaultLookAndFeelDecorated(true)
            if (LOOKANDFEEL != null) {
                theme = when (LOOKANDFEEL) {
                    "Metal" -> UIManager.getCrossPlatformLookAndFeelClassName()
                    "System" -> UIManager.getSystemLookAndFeelClassName()
                    "Motif" -> "com.sun.java.swing.plaf.motif.MotifLookAndFeel"
                    "GTK+" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                    else -> {
                        ErrorLog.logError("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL!!)
                        UIManager.getCrossPlatformLookAndFeelClassName()
                    }
                }

                try {
                    UIManager.setLookAndFeel(theme)
                } catch (var2: ClassNotFoundException) {
                    ErrorLog.logError("Couldn't find class for specified look and feel:" + theme!!)
                    ErrorLog.logError("Did you include the L&F library in the class path?")
                    ErrorLog.logError("Using the default look and feel.")
                } catch (var3: UnsupportedLookAndFeelException) {
                    ErrorLog.logError("Can't use the specified look and feel ($theme) on this platform.")
                    ErrorLog.logError("Using the default look and feel.")
                } catch (var4: Exception) {
                    ErrorLog.logError("Couldn't get specified look and feel ($theme), for some reason.")
                    ErrorLog.logError("Using the default look and feel.")
                    ErrorLog.logError(var4)
                }

            }

        }
    }
}
