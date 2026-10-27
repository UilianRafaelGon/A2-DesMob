package br.ufpr.controlefinanceiro.controller

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.ufpr.controlefinanceiro.R
import br.ufpr.controlefinanceiro.data.dao.TransacaoDAO
import br.ufpr.controlefinanceiro.model.Transacao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NovaTransacaoActivity : AppCompatActivity() {

    private lateinit var transacaoDAO: TransacaoDAO
    private var transacaoId: Int = 0

    private lateinit var tvTitulo: TextView
    private lateinit var etDescricao: EditText
    private lateinit var etValor: EditText
    private lateinit var rgTipo: RadioGroup
    private lateinit var rbReceita: RadioButton
    private lateinit var rbDespesa: RadioButton
    private lateinit var spCategoria: Spinner
    private lateinit var etData: EditText
    private lateinit var btnCalendario: Button
    private lateinit var spStatus: Spinner
    private lateinit var btnSalvar: Button
    private lateinit var btnExcluir: Button

    private val categorias = arrayOf(
        "Alimentação",
        "Transporte",
        "Moradia",
        "Salário",
        "Lazer",
        "Saúde",
        "Educação",
        "Outros"
    )

    private val statusOpcoes = arrayOf(
        "Pago/Recebido",
        "Pendente"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_transacao)

        transacaoDAO = TransacaoDAO(this)

        tvTitulo = findViewById(R.id.tvTitulo)
        etDescricao = findViewById(R.id.etDescricao)
        etValor = findViewById(R.id.etValor)
        rgTipo = findViewById(R.id.rgTipo)
        rbReceita = findViewById(R.id.rbReceita)
        rbDespesa = findViewById(R.id.rbDespesa)
        spCategoria = findViewById(R.id.spCategoria)
        etData = findViewById(R.id.etData)
        btnCalendario = findViewById(R.id.btnCalendario)
        spStatus = findViewById(R.id.spStatus)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnExcluir = findViewById(R.id.btnExcluir)

        // Configuração dos Spinners com layouts de texto escuro e legível
        val adapterCategoria = ArrayAdapter(
            this,
            R.layout.item_spinner,
            categorias
        )
        adapterCategoria.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spCategoria.adapter = adapterCategoria

        val adapterStatus = ArrayAdapter(
            this,
            R.layout.item_spinner,
            statusOpcoes
        )
        adapterStatus.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spStatus.adapter = adapterStatus

        // Data padrão atual
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etData.setText(formatoData.format(Date()))

        // Máscara automática de data: insere as barras '/' automaticamente ao digitar
        configurarMascaraData()

        // Botão de calendário para abrir seletor gráfico de data (DatePickerDialog)
        btnCalendario.setOnClickListener {
            abrirSeletorData()
        }

        // Verifica se é modo edição (id recebido pela Intent)
        transacaoId = intent.getIntExtra("transacaoId", 0)
        if (transacaoId != 0) {
            tvTitulo.text = getString(R.string.editar_transacao)
            btnExcluir.visibility = View.VISIBLE
            carregarTransacao()
        } else {
            tvTitulo.text = getString(R.string.nova_transacao)
            btnExcluir.visibility = View.GONE
        }
    }

    private fun configurarMascaraData() {
        etData.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                oldLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return

                val currentStr = s.toString()
                // Se o usuário estiver apagando, não força formatação
                if (currentStr.length < oldLength) return

                val clean = currentStr.replace("/", "")
                val formatted = StringBuilder()

                for (i in clean.indices) {
                    formatted.append(clean[i])
                    if ((i == 1 || i == 3) && i != clean.length - 1) {
                        formatted.append("/")
                    }
                }

                if (formatted.toString() != currentStr) {
                    isUpdating = true
                    etData.setText(formatted.toString())
                    etData.setSelection(formatted.length)
                    isUpdating = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun abrirSeletorData() {
        val calendario = Calendar.getInstance()
        val partes = etData.text.toString().split("/")
        var dia = calendario.get(Calendar.DAY_OF_MONTH)
        var mes = calendario.get(Calendar.MONTH)
        var ano = calendario.get(Calendar.YEAR)

        if (partes.size == 3) {
            val d = partes[0].toIntOrNull()
            val m = partes[1].toIntOrNull()
            val a = partes[2].toIntOrNull()
            if (d != null && m != null && a != null && m in 1..12) {
                dia = d
                mes = m - 1
                ano = a
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val dataFormatada = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
                etData.setText(dataFormatada)
            },
            ano,
            mes,
            dia
        )
        datePickerDialog.show()
    }

    private fun carregarTransacao() {
        val transacao = transacaoDAO.getTransacaoById(transacaoId)
        transacao?.let {
            etDescricao.setText(it.descricao)
            etValor.setText(it.valor.toString())

            if (it.tipo == "Receita") {
                rbReceita.isChecked = true
            } else {
                rbDespesa.isChecked = true
            }

            val posCategoria = categorias.indexOf(it.categoria)
            if (posCategoria >= 0) {
                spCategoria.setSelection(posCategoria)
            }

            etData.setText(it.data)

            val posStatus = statusOpcoes.indexOf(it.status)
            if (posStatus >= 0) {
                spStatus.setSelection(posStatus)
            }
        }
    }

    fun salvarTransacao(view: View) {
        val descricao = etDescricao.text.toString().trim()
        val valorTexto = etValor.text.toString().trim()
        val data = etData.text.toString().trim()

        // Validação 1: Descrição não pode ser vazia
        if (descricao.isEmpty()) {
            etDescricao.error = "A descrição é obrigatória"
            etDescricao.requestFocus()
            Toast.makeText(this, "Por favor, preencha a descrição.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação 2: Valor não pode ser vazio
        if (valorTexto.isEmpty()) {
            etValor.error = "O valor é obrigatório"
            etValor.requestFocus()
            Toast.makeText(this, "Por favor, preencha o valor.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação 3: Valor deve ser positivo maior que zero
        val valor = valorTexto.toDoubleOrNull()
        if (valor == null || valor <= 0.0) {
            etValor.error = "O valor deve ser um número maior que zero"
            etValor.requestFocus()
            Toast.makeText(this, "Valor numérico inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validação 4: Data obrigatória e com formato DD/MM/AAAA
        if (data.isEmpty()) {
            etData.error = "A data é obrigatória"
            etData.requestFocus()
            Toast.makeText(this, "Por favor, preencha a data.", Toast.LENGTH_SHORT).show()
            return
        }

        val dataValida = data.matches(Regex("""^\d{2}/\d{2}/\d{4}$"""))
        if (!dataValida) {
            etData.error = "Formato de data inválido. Use DD/MM/AAAA"
            etData.requestFocus()
            Toast.makeText(this, "Formato de data incorreto. Exemplo: 26/10/2026", Toast.LENGTH_SHORT).show()
            return
        }

        val tipo = if (rbReceita.isChecked) "Receita" else "Despesa"
        val categoria = spCategoria.selectedItem.toString()
        val status = spStatus.selectedItem.toString()

        if (transacaoId == 0) {
            val novaTransacao = Transacao(
                descricao = descricao,
                valor = valor,
                tipo = tipo,
                categoria = categoria,
                data = data,
                status = status
            )
            transacaoDAO.addTransacao(novaTransacao)
            Toast.makeText(this, "Transação cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
        } else {
            val transacaoAtualizada = Transacao(
                id = transacaoId,
                descricao = descricao,
                valor = valor,
                tipo = tipo,
                categoria = categoria,
                data = data,
                status = status
            )
            transacaoDAO.updateTransacao(transacaoAtualizada)
            Toast.makeText(this, "Transação atualizada com sucesso!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    fun excluirTransacao(view: View) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir este registro?")
            .setPositiveButton("Sim") { _, _ ->
                transacaoDAO.deleteTransacao(transacaoId)
                Toast.makeText(this, "Transação excluída com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }
}
