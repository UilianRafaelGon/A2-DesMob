package br.ufpr.controlefinanceiro

import br.ufpr.controlefinanceiro.model.Transacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransacaoTest {

    @Test
    fun testCriacaoTransacao() {
        val transacao = Transacao(
            id = 1,
            descricao = "Salário",
            valor = 4500.0,
            tipo = "Receita",
            categoria = "Salário",
            data = "26/10/2026",
            status = "Pago/Recebido"
        )

        assertEquals("Salário", transacao.descricao)
        assertEquals(4500.0, transacao.valor, 0.001)
        assertEquals("Receita", transacao.tipo)
        assertTrue(transacao.toString().contains("Salário"))
    }

    @Test
    fun testCalculoSaldo() {
        val lista = listOf(
            Transacao(1, "Salário", 5000.0, "Receita", "Salário", "01/10/2026", "Pago/Recebido"),
            Transacao(2, "Freelance", 1200.0, "Receita", "Salário", "10/10/2026", "Pago/Recebido"),
            Transacao(3, "Aluguel", 1800.0, "Despesa", "Moradia", "05/10/2026", "Pago/Recebido"),
            Transacao(4, "Mercado", 650.0, "Despesa", "Alimentação", "12/10/2026", "Pago/Recebido")
        )

        val totalReceitas = lista.filter { it.tipo == "Receita" }.sumOf { it.valor }
        val totalDespesas = lista.filter { it.tipo == "Despesa" }.sumOf { it.valor }
        val saldo = totalReceitas - totalDespesas

        assertEquals(6200.0, totalReceitas, 0.001)
        assertEquals(2450.0, totalDespesas, 0.001)
        assertEquals(3750.0, saldo, 0.001)
    }
}
