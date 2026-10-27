package br.ufpr.controlefinanceiro.model

data class Transacao(
    val id: Int = 0,
    val descricao: String,
    val valor: Double,
    val tipo: String,       // "Receita" ou "Despesa"
    val categoria: String,  // "Alimentação", "Moradia", "Transporte", "Salário", "Lazer", "Outros"
    val data: String,       // "DD/MM/AAAA"
    val status: String      // "Pago" ou "Pendente"
) {
    override fun toString(): String {
        val sinal = if (tipo == "Receita") "+" else "-"
        return "$descricao ($tipo)\n$sinal R$ ${"%.2f".format(valor)} | $categoria | $data [$status]"
    }
}
