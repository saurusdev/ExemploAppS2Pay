package com.example.exemploapps2pay.service

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.example.exemploapps2pay.model.TransacaoInfo
import java.util.*
import kotlin.random.Random

/**
 * Serviço responsável por gerenciar todas as operações de pagamento e estorno.
 *
 * Esta classe centraliza as funcionalidades de envio de transações para o aplicativo
 * externo de pagamento e processamento de estornos.
 */
class PaymentService {

    companion object {

        // Códigos de requisição usados para identificar o retorno das intents
        const val REQUEST_PAGAMENTO = 150
        const val REQUEST_ESTORNO = 153

        // Constantes para identificar o tipo de pagamento
        const val DEBITO = 4
        const val CREDITO = 3
        const val PIX = 17
        const val TRANSACAO_S2PAY = 5
        const val INTEGRACAO = 0

        // Resultado de erro quando o app não foi encontrado
        const val APP_NOT_FOUND = -1

        // Pacote do aplicativo externo de pagamento
        const val S2PAY_PACKAGE = "br.com.saurus.s2pay"

        // Tipos de pagamento disponíveis para o usuário selecionar
        val TIPOS_PAGAMENTO = listOf("Débito", "Crédito", "Pix")

        // Descrições dos status de transação
        val STATUS_TRANSACAO = mapOf(
            0 to "0 EmAberto.",
            1 to "1 Confirmada.",
            2 to "2 Estornada.",
            3 to "3 ConfirmacaoPendente.",
            4 to "4 EstornoPendente.",
            5 to "5 EstornoAberto.",
            6 to "6 ConfirmacaoEstornoPendente.",
            7 to "7 Ignorado."
        )

        // Descrições dos tipos de comando
        val TIPOS_COMANDO = mapOf(
            "0" to "Pagamento",
            "1" to "Estorno",
            "2" to "Fidelidade",
            "3" to "Reimpressão"
        )
        /**
         * Cria uma nova transação com informações básicas padrão.
         *
         * @return Um objeto TransacaoInfo com os dados da transação gerados.
         */
        fun criarTransacao(): TransacaoInfo {
            return TransacaoInfo(
                id = 0,
                docCliente = "99999999000191", // CPF ou CNPJ do cliente
                nomeCliente = "Testes", // Razão Social ou Nome do cliente
                telefoneCliente = "11940028922", // Número de telefone do cliente
                emailCliente = "", // Endereço de email do cliente
                idMov = UUID.randomUUID().toString(), // ID único para a movimentação
                idPag = Random.nextInt(), // ID único para o pagamento
                idFatPag = UUID.randomUUID().toString(), // ID único para a fatura
                qParc = 1, // Número de parcelas para o pagamento
                dominio = "99999999000191", // Documento ou domínio da loja
                nroPDV = 1, // Número identificador do PDV
                valor = 0.0 // Valor inicial zero
            )
        }

        /**
         * Envia uma transação de pagamento para o aplicativo externo.
         *
         * @param transacao Informações da transação a ser processada.
         * @param tipoPagamento Tipo de pagamento (Débito, Crédito, Pix).
         * @param activity Contexto da Activity para iniciar a transação.
         * @throws Exception Se ocorrer algum erro durante o processo.
         */
        @Throws(Exception::class)
        fun enviarPagamento(transacao: TransacaoInfo, tipoPagamento: String, activity: Activity) {
            val tpMod = when (tipoPagamento) {
                "Débito" -> DEBITO
                "Pix" -> PIX
                "Crédito" -> CREDITO
                else -> throw IllegalArgumentException("Tipo de pagamento inválido")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage(S2PAY_PACKAGE)
                putExtra("idPag", transacao.idPag)
                putExtra("xDocCliente", transacao.docCliente)
                putExtra("xNomeCliente", transacao.nomeCliente)
                putExtra("xTelefoneCiente", transacao.telefoneCliente)
                putExtra("xEmailCliente", transacao.emailCliente)
                putExtra("idMov", transacao.idMov)
                putExtra("idFaturaPag", transacao.idFatPag)
                putExtra("dominio", transacao.dominio)
                putExtra("nroPDV", transacao.nroPDV)
                putExtra("tpOperacao", INTEGRACAO)
                putExtra("vPagamento", transacao.valor)
                putExtra("versaoIntegracao", "1")
                putExtra("tpMod", tpMod)
                putExtra("tpTransacao", TRANSACAO_S2PAY)
                putExtra("qParcs", transacao.qParc)
            }

            try {
                activity.startActivityForResult(intent, REQUEST_PAGAMENTO)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Aplicativo de pagamento não encontrado", Toast.LENGTH_LONG).show()
                throw e
            }
        }

        /**
         * Envia uma transação de estorno para o aplicativo externo.
         *
         * @param transacao Informações da transação a ser estornada.
         * @param activity Contexto da Activity para iniciar o estorno.
         * @throws Exception Se ocorrer algum erro durante o processo.
         */
        @Throws(Exception::class)
        fun enviarEstorno(transacao: TransacaoInfo, activity: Activity) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage(S2PAY_PACKAGE)
                putExtra("idPag", transacao.idPag)
                putExtra("idFaturaPag", transacao.idFatPag)
                putExtra("dominio", transacao.dominio)
                putExtra("idTef", transacao.id)
                putExtra("nroPDV", transacao.nroPDV)
                putExtra("tpOperacao", 1)
                putExtra("versaoIntegracao", "1")
            }

            try {
                activity.startActivityForResult(intent, REQUEST_ESTORNO)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Aplicativo de pagamento não encontrado", Toast.LENGTH_LONG).show()
                throw e
            }
        }

        /**
         * Formata um campo de texto como moeda (R$).
         *
         * @param editText O campo de texto para aplicar a formatação.
         */
        fun aplicarMascaraMoeda(editText: EditText) {
            editText.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false // Flag para evitar chamadas recursivas

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return

                    isUpdating = true

                    val digits = s.toString().replace(Regex("[^\\d]"), "")
                    if (digits.isEmpty()) {
                        editText.setText("0,00")
                        editText.setSelection(editText.text.length)
                        isUpdating = false
                        return
                    }

                    val formatted = (digits.toBigInteger().toDouble() / 100).let {
                        String.format("%,.2f", it).replace('.', ',')
                    }

                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                    isUpdating = false
                }
            })
        }

        /**
         * Converte um valor formatado como moeda para Double.
         *
         * @param valor Valor em formato de texto (ex: "123,45").
         * @return Valor convertido para Double ou null se inválido.
         */
        fun converterValorParaDouble(valor: String): Double? {
            return try {
                valor.replace(".", "").replace(",", ".").toDoubleOrNull()
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Valida se um valor é positivo e maior que zero.
         *
         * @param valor Valor a ser validado.
         * @return true se válido, false se inválido.
         */
        fun validarValor(valor: Double?): Boolean {
            return valor != null && valor > 0
        }
    }
}

