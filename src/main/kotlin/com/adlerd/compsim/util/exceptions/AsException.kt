import com.adlerd.compsim.core.Instruction

class AsException : Exception {
    var instruction: Instruction? = null

    constructor(inst: Instruction, var2: String) : super(var2) {
        this.instruction = inst
    }

    constructor(var1: String) : super(var1)

    override val message: String?
        get() {
            var message = "Assembly error: "
//          if (this.instruction != null) {
//              message = message + "[line " + this.instruction!!.getLineNumber() + ", '" + this.instruction!!.getOriginalLine() + "']: "
//          }

            message += super.message
            return message
        }
}