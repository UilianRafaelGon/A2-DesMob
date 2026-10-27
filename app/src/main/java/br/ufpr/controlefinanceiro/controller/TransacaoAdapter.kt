package br.ufpr.controlefinanceiro.controller

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import br.ufpr.controlefinanceiro.R
import br.ufpr.controlefinanceiro.model.Transacao

class TransacaoAdapter(
    context: Context,
    private val transacoes: List<Transacao>
) : ArrayAdapter<Transacao>(context, R.layout.item_transacao, transacoes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_transacao,
            parent,
            false
        )

        val transacao = transacoes[position]

        val viewIndicator = view.findViewById<View>(R.id.viewIndicator)
        val tvDescricao = view.findViewById<TextView>(R.id.tvDescricao)
        val tvCategoriaData = view.findViewById<TextView>(R.id.tvCategoriaData)
        val tvValor = view.findViewById<TextView>(R.id.tvValor)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        tvDescricao.text = transacao.descricao
        tvCategoriaData.text = "${transacao.categoria} • ${transacao.data}"

        if (transacao.tipo == "Receita") {
            viewIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.income_green))
            tvValor.setTextColor(ContextCompat.getColor(context, R.color.income_green))
            tvValor.text = "+ R$ ${"%.2f".format(transacao.valor)}"
        } else {
            viewIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.expense_red))
            tvValor.setTextColor(ContextCompat.getColor(context, R.color.expense_red))
            tvValor.text = "- R$ ${"%.2f".format(transacao.valor)}"
        }

        tvStatus.text = transacao.status
        if (transacao.status == "Pendente") {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_pending)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.pending_orange))
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_income)
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.income_green))
        }

        return view
    }
}
