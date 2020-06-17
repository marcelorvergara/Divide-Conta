package project.inflabnet.mytest.throwables

class ItemNomeException : Throwable() {
    override val message: String?
        get() = "O item deve ter mais que 3 caracteres"
}
