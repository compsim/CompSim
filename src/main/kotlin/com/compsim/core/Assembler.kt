package com.compsim.core

import AsException
import com.compsim.util.SymbolTable
import java.io.*
import java.util.*

internal class Assembler {

    @Throws(AsException::class)
    fun assemble(args: Array<String>): String {
        var inputFile: String? = ""
        val symbolTable = SymbolTable()

        for (arg in args.indices) {
            if (args[arg].isEmpty()) {
                throw AsException("Null arguments are not permitted.")
            }

            inputFile = args[arg]
        }

        if (inputFile != null && inputFile.isNotEmpty()) {
            val fileName = this.baseFileName(inputFile)
            var instructionList: List<Instruction> = this.parse(fileName)
            instructionList = this.passZero(instructionList)
            this.passOne(symbolTable, instructionList)
            this.passTwo(symbolTable, instructionList, fileName)
            this.generateSymbols(symbolTable, instructionList, fileName)
            return ""
        } else {
            throw AsException("No .asm file specified.")
        }
    }

    /**
     * Make sure input file has .asm extension
     * @param inputFile argument name of input .asm file
     * @return the name of the inputFile without the .asm extension
     */
    @Throws(AsException::class)
    fun baseFileName(inputFile: String): String {
        return if (!inputFile.endsWith(".asm")) {
            throw AsException("Input file must have .asm suffix ('$inputFile')")
        } else {
            inputFile.substring(0, inputFile.length - 4)
        }
    }

    /**
     * Read instructions in input file line by line
     * @param fileName name of the file being read
     * @return list of Instructions
     */
    @Throws(AsException::class)
    fun parse(fileName: String): List<Instruction> {
        val file = "$fileName.asm"
        val instructionList = ArrayList<Instruction>()
        var var6 = 1

        try {
            val reader = BufferedReader(FileReader(file))

            while (true) {
                var instruction: Instruction
                var line: String?
                do {
//                    try {
                    line = reader.readLine()
//                    } catch (ise: IllegalStateException) {
//
//                    }
                    if ((line) == null) {
                        reader.close()
                        return instructionList
                    }

                    instruction = Instruction(line, var6++)
                } while (instruction.opcode == null && instruction.label == null)

                instructionList.add(instruction)
            }
        } catch (ioException: IOException) {
            throw AsException("Couldn't read file ($file)")
        }

    }

    @Throws(AsException::class)
    private fun passZero(instructionList: List<Instruction>): List<Instruction> {
        val instructionOutList = ArrayList<Instruction>()
        val instructionIterator = instructionList.iterator()

        while (instructionIterator.hasNext()) {
            val instruction = instructionIterator.next()
            instruction.splitLabels(instructionOutList)
        }

        return instructionOutList
    }

    @Throws(AsException::class)
    fun passOne(symbolTable: SymbolTable, instructionList: List<Instruction>) {
        var address = -1
        val instructionIterator = instructionList.iterator()

        while (instructionIterator.hasNext()) {
            val instruction = instructionIterator.next()
            if (instruction.label != null) {
                if (instruction.label!!.length > 20) {
                    instruction.error("Labels can be no longer than 20 characters ('${instruction.label}').")
                }

                if (address > 65535) {
                    instruction.error("Label cannot be represented in 16 bits ($address)")
                }

                if (!symbolTable.insert(instruction.label!!, address)) {
                    instruction.error("Duplicate label ('" + instruction.label + "')")
                }
            } else {
                instruction.address = address
                val instructionDef = ISA.formatToDef[instruction.format] as InstructionDef

                address = instructionDef.getNextAddress(instruction)
            }
        }

    }

    @Throws(AsException::class)
    fun passTwo(symbolTable: SymbolTable, instructionList: List<Instruction>, fileName: String) {
        val wordArray = ArrayList<Word>()
        val instructionIterator = instructionList.iterator()

        while (instructionIterator.hasNext()) {
            val instruction = instructionIterator.next()
            if (instruction.label == null) {
                val opcode = instruction.opcode
                if (opcode == null) {
                    Console.println(instruction.originalLine)
                }

                val instructionDef = ISA.formatToDef[instruction.format] as InstructionDef
                instructionDef.encode(symbolTable, instruction, wordArray)
            }
        }

        val file = "$fileName.obj"

        try {
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            val wordIterator = wordArray.iterator()

            while (wordIterator.hasNext()) {
                val word = wordIterator.next()
                word.writeWordToFile(outputStream)
            }

            outputStream.close()
        } catch (ioException: IOException) {
            throw AsException("Couldn't write file ($file)")
        }

    }

    @Throws(AsException::class)
    fun generateSymbols(symbolTable: SymbolTable, instructionList: List<Instruction>, fileName: String) {
        val file = "$fileName.sym"
        val labels = symbolTable.labels

        try {
            val writer = BufferedWriter(FileWriter(file))
            writer.write("// Symbol table\n")
            writer.write("// Scope level 0:\n")
            writer.write("//\tSymbol Name       Page Address\n")
            writer.write("//\t----------------  ------------\n")

            while (labels.hasMoreElements()) {
                val label = labels.nextElement() as String
                writer.write("//\t$label")

                var index = 0
                while (index < 16 - label.length) {
                    writer.write(" ")
                    ++index
                }

                index = symbolTable.lookup(label)
                val address = this.formatAddress(index)
                writer.write("  $address\n")
            }

            val instructionIterator = instructionList.iterator()

            while (instructionIterator.hasNext()) {
                val instruction = instructionIterator.next()
                if (instruction.opcode != null) {
                    val var1instructionDef = ISA.formatToDef[instruction.format] as InstructionDef
                    if (!var1instructionDef.isDataDirective) {
                        writer.write("//\t$               ${this.formatAddress(instruction.address)}\n")
                    }
                }
            }

            writer.newLine()
            writer.close()
        } catch (ioException: IOException) {
            throw AsException("Couldn't write file ($file)")
        }

    }

    private fun formatAddress(address: Int): String {
        val output = "0000" + Integer.toHexString(address).toUpperCase()
        return output.substring(output.length - 4)
    }
}
