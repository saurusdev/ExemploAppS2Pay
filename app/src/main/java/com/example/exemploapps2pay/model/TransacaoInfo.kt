package com.example.exemploapps2pay.model

/**
 * Classe que representa os dados de uma transação de pagamento.
 *
 * Concentra todas as informações necessárias para realizar uma transação
 * de pagamento ou estorno no aplicativo externo.
 */
data class TransacaoInfo(
    var id: Int,                    // ID da transação no sistema TEF (usado para estorno)
    val docCliente: String,         // CPF ou CNPJ do cliente
    val nomeCliente: String,        // Nome ou razão social do cliente
    val telefoneCliente: String,    // Telefone do cliente com DDD
    val emailCliente: String,       // Email do cliente
    val idMov: String,              // ID único da movimentação
    val idPag: Int,                 // ID único do pagamento
    val idFatPag: String,           // ID da fatura do pagamento
    var qParc: Int,                 // Quantidade de parcelas
    val dominio: String,            // Documento/domínio da loja
    val nroPDV: Int,                // Número do PDV (Ponto de Venda)
    var valor: Double               // Valor da transação
)