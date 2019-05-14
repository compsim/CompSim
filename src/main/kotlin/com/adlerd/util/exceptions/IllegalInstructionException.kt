import com.adlerd.util.exceptions.GenericException

class IllegalInstructionException(message: String) : GenericException(message) {

    override val exceptionDescription: String
        get() = "IllegalInstructionException: ${this.message}"
}
