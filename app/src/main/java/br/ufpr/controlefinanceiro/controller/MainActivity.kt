package br.ufpr.controlefinanceiro.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.ufpr.controlefinanceiro.R
import br.ufpr.controlefinanceiro.data.dao.TransacaoDAO
import br.ufpr.controlefinanceiro.model.Transacao

class MainActivity : AppCompatActivity() {

    private lateinit var transacaoDAO: TransacaoDAO

    private lateinit var tvSaldo: TextView
    private lateinit var tvTotalReceitas: TextView
    private lateinit var tvTotalDespesas: TextView

    private lateinit var etBusca: EditText
    private lateinit var btnBuscar: Button
    private lateinit var spFiltroTipo: Spinner
    private lateinit var spOrdenacao: Spinner

    private lateinit var lvTransacoes: ListView
    private lateinit var tvEmpty: TextView

    private val opcoesFiltroTipo = arrayOf("Todos os tipos", "Receita", "Despesa")
    private val opcoesOrdenacao = arrayOf("Mais recentes", "Maior valor", "Menor valor", "Descrição (A-Z)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transacaoDAO = TransacaoDAO(this)

        tvSaldo = findViewById(R.id.tvSaldo)
        tvTotalReceitas = findViewById(R.id.tvTotalReceitas)
        tvTotalDespesas = findViewById(R.id.tvTotalDespesas)

        etBusca = findViewById(R.id.etBusca)
        btnBuscar = findViewById(R.id.btnBuscar)
        spFiltroTipo = findViewById(R.id.spFiltroTipo)
        spOrdenacao = findViewById(R.id.spOrdenacao)

        lvTransacoes = findViewById(R.id.lvTransacoes)
        tvEmpty = findViewById(R.id.tvEmpty)

        // Configuração do Spinner de Filtro por Tipo com layouts de texto contrastante
        val adapterFiltro = ArrayAdapter(
            this,
            R.layout.item_spinner,
            opcoesFiltroTipo
        )
        adapterFiltro.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spFiltroTipo.adapter = adapterFiltro

        // Configuração do Spinner de Ordenação com layouts de texto contrastante
        val adapterOrdenacao = ArrayAdapter(
            this,
            R.layout.item_spinner,
            opcoesOrdenacao
        )
        adapterOrdenacao.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spOrdenacao.adapter = adapterOrdenacao

        // Listener para clique nos itens da lista (Requisito 4)
        lvTransacoes.setOnItemClickListener { parent, _, position, _ ->
            val transacaoSelecionada = parent.getItemAtPosition(position) as Transacao
            val intent = Intent(this, NovaTransacaoActivity::class.java).apply {
                putExtra("transacaoId", transacaoSelecionada.id)
            }
            startActivity(intent)
        }

        // Listener para o botão Buscar
        btnBuscar.setOnClickListener {
            carregarLista()
        }

        // Listeners para os Spinners atualizarem a lista automaticamente ao mudar a opção
        val listenerAtualizar = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                carregarLista()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spFiltroTipo.onItemSelectedListener = listenerAtualizar
        spOrdenacao.onItemSelectedListener = listenerAtualizar
    }

    override fun onResume() {
        super.onResume()
        atualizarTotais()
        carregarLista()
    }

    // Regra de Negócio: Cálculo de Totais e Saldo
    private fun atualizarTotais() {
        val (totalReceitas, totalDespesas, saldo) = transacaoDAO.calcularTotais()

        tvTotalReceitas.text = "+ R$ ${"%.2f".format(totalReceitas)}"
        tvTotalDespesas.text = "- R$ ${"%.2f".format(totalDespesas)}"
        tvSaldo.text = "R$ ${"%.2f".format(saldo)}"

        if (saldo >= 0.0) {
            tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvSaldo.setTextColor(ContextCompat.getColor(this, R.color.expense_red))
        }
    }

    // Carrega e filtra a lista de transações com base nas escolhas do usuário
    private fun carregarLista() {
        val busca = etBusca.text.toString().trim()
        val filtroSelecionado = spFiltroTipo.selectedItem?.toString() ?: "Todos os tipos"
        val ordenacaoSelecionada = spOrdenacao.selectedItem?.toString() ?: "Mais recentes"

        val filtroTipoParam = if (filtroSelecionado == "Todos os tipos") "Todos" else filtroSelecionado

        val lista = transacaoDAO.getAllTransacoes(
            filtroTipo = filtroTipoParam,
            busca = busca,
            ordenacao = ordenacaoSelecionada
        )

        if (lista.isEmpty()) {
            lvTransacoes.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            lvTransacoes.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE

            val adapter = TransacaoAdapter(this, lista)
            lvTransacoes.adapter = adapter
        }
    }

    // Ação do Botão para cadastrar nova transação
    fun novaTransacao(view: View) {
        val intent = Intent(this, NovaTransacaoActivity::class.java)
        startActivity(intent)
    }
}
