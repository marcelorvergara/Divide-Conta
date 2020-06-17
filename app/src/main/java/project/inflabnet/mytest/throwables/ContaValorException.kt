package project.inflabnet.mytest.throwables

class ContaValorException : Throwable(){
    override val message: String?
        get() = "O preço deve ser maior que zero"
}
