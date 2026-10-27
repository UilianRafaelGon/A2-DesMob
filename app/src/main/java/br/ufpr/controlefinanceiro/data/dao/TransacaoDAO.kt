package br.ufpr.controlefinanceiro.data.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import br.ufpr.controlefinanceiro.data.db.DBHelper
import br.ufpr.controlefinanceiro.model.Transacao

class TransacaoDAO(private val context: Context) {

    private val dbHelper = DBHelper(context)

    fun addTransacao(transacao: Transacao): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("descricao", transacao.descricao)
            put("valor", transacao.valor)
            put("tipo", transacao.tipo)
            put("categoria", transacao.categoria)
            put("data", transacao.data)
            put("status", transacao.status)
        }
        val id = db.insert(DBHelper.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAllTransacoes(
        filtroTipo: String = "Todos",
        busca: String = "",
        ordenacao: String = "Mais recentes"
    ): List<Transacao> {
        val db = dbHelper.readableDatabase

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (filtroTipo != "Todos") {
            whereClauses.add("tipo = ?")
            args.add(filtroTipo)
        }

        if (busca.isNotBlank()) {
            whereClauses.add("descricao LIKE ?")
            args.add("%${busca.trim()}%")
        }

        val selection = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else null
        val selectionArgs = if (args.isNotEmpty()) args.toTypedArray() else null

        val orderBy = when (ordenacao) {
            "Maior valor" -> "valor DESC"
            "Menor valor" -> "valor ASC"
            "Descrição (A-Z)" -> "descricao ASC"
            else -> "id DESC"
        }

        val cursor: Cursor = db.query(
            DBHelper.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )

        val lista = mutableListOf<Transacao>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
            val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
            val categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"))
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            lista.add(Transacao(id, descricao, valor, tipo, categoria, data, status))
        }

        cursor.close()
        db.close()
        return lista
    }

    fun getTransacaoById(id: Int): Transacao? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DBHelper.TABLE_NAME,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var transacao: Transacao? = null
        if (cursor.moveToFirst()) {
            val descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"))
            val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
            val categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria"))
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))

            transacao = Transacao(id, descricao, valor, tipo, categoria, data, status)
        }

        cursor.close()
        db.close()
        return transacao
    }

    fun updateTransacao(transacao: Transacao): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("descricao", transacao.descricao)
            put("valor", transacao.valor)
            put("tipo", transacao.tipo)
            put("categoria", transacao.categoria)
            put("data", transacao.data)
            put("status", transacao.status)
        }
        val rowsAffected = db.update(
            DBHelper.TABLE_NAME,
            values,
            "id = ?",
            arrayOf(transacao.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteTransacao(id: Int): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            DBHelper.TABLE_NAME,
            "id = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsDeleted
    }

    // Regra de Negócio: Cálculo de Totais e Saldo
    fun calcularTotais(): Triple<Double, Double, Double> {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.query(
            DBHelper.TABLE_NAME,
            arrayOf("tipo", "valor"),
            null,
            null,
            null,
            null,
            null
        )

        var totalReceitas = 0.0
        var totalDespesas = 0.0

        while (cursor.moveToNext()) {
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
            val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("valor"))
            if (tipo == "Receita") {
                totalReceitas += valor
            } else if (tipo == "Despesa") {
                totalDespesas += valor
            }
        }

        cursor.close()
        db.close()

        val saldo = totalReceitas - totalDespesas
        return Triple(totalReceitas, totalDespesas, saldo)
    }
}
