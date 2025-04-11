package com.example.exemploapps2pay
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.saurus.saurustef_util.TEFDados
import com.example.exemploapps2pay.databinding.ActivityMainBinding
import com.example.exemploapps2pay.model.TransacaoInfo
import com.example.exemploapps2pay.service.PaymentService
import com.example.exemploapps2pay.service.PaymentService.Companion.REQUEST_ESTORNO
import com.example.exemploapps2pay.service.PaymentService.Companion.REQUEST_PAGAMENTO
import com.example.exemploapps2pay.service.PaymentService.Companion.STATUS_TRANSACAO
import com.example.exemploapps2pay.service.PaymentService.Companion.TIPOS_COMANDO
import com.example.exemploapps2pay.service.PaymentService.Companion.TIPOS_PAGAMENTO
import com.example.exemploapps2pay.service.PaymentService.Companion.aplicarMascaraMoeda
import com.example.exemploapps2pay.service.PaymentService.Companion.converterValorParaDouble
import com.example.exemploapps2pay.service.PaymentService.Companion.validarValor

import com.google.gson.Gson


/**
 * Tela principal da aplicação, responsável por gerenciar a interface de pagamento.
 *
 * Nessa tela o usuário pode inserir um valor, escolher o tipo de pagamento (Débito, Crédito ou Pix)
 * e, se necessário, informar a quantidade de parcelas.
 * O botão "Pagar" executa a ação correspondente ao tipo de pagamento selecionado.
 */
class MainActivity : AppCompatActivity() {

    // Binding gerado a partir do layout activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // Transação atual em andamento
    private lateinit var transacaoAtual: TransacaoInfo

    /**
     * Inicializa a tela, configura os componentes de UI e trata a lógica de seleção e pagamento.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cria uma transação padrão
        transacaoAtual = PaymentService.criarTransacao()

        // Aplica máscara de moeda ao campo de valor
        aplicarMascaraMoeda(binding.edtValor)

        // Configura o Spinner com os tipos de pagamento
        binding.spinnerTipoPagamento.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, TIPOS_PAGAMENTO)

        // Mostra ou oculta o campo de parcelas de acordo com o tipo selecionado
        binding.spinnerTipoPagamento.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val isCredito = TIPOS_PAGAMENTO[position] == "Crédito"
                    binding.edtParcelas.visibility = if (isCredito) View.VISIBLE else View.GONE
                    binding.labelParcelas.visibility = if (isCredito) View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        // Ação ao clicar no botão "Pagar" ou "Estornar"
        binding.btnPagar.setOnClickListener {
            if (binding.btnPagar.text == "Pagar") {
                realizarPagamento()
            } else {
                realizarEstorno()
            }
        }
    }

    /**
     * Processa e realiza uma transação de pagamento com base nos dados informados pelo usuário.
     */
    private fun realizarPagamento() {
        // Tenta converter o valor digitado para Double
        val valor = converterValorParaDouble(binding.edtValor.text.toString())

        // Valida se o valor é válido
        if (!validarValor(valor)) {
            Toast.makeText(this, "Informe um valor válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Atualiza os dados da transação atual
        transacaoAtual = PaymentService.criarTransacao()
        transacaoAtual.valor = valor!!

        // Verifica se é crédito para ajustar parcelas
        if (binding.spinnerTipoPagamento.selectedItem.toString() == "Crédito") {
            transacaoAtual.qParc = binding.edtParcelas.text.toString().toIntOrNull() ?: 1
        }

        // Obtém o tipo de pagamento selecionado
        val tipoSelecionado = binding.spinnerTipoPagamento.selectedItem.toString()

        try {
            // Envia a transação para processamento
            PaymentService.enviarPagamento(transacaoAtual, tipoSelecionado, this)
        } catch (e: Exception) {
            Log.e("PAGAMENTO", "Erro ao processar pagamento: ${e.message}", e)
            Toast.makeText(this, "Erro ao processar pagamento: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Realiza o estorno da transação atual.
     */
    private fun realizarEstorno() {
        try {
            PaymentService.enviarEstorno(transacaoAtual, this)
        } catch (e: Exception) {
            Log.e("ESTORNO", "Erro ao processar estorno: ${e.message}", e)
            Toast.makeText(this, "Erro ao processar estorno: ${e.message}", Toast.LENGTH_LONG)
                .show()
        } finally {
            // Se ocorrer um erro, volta para o modo de pagamento
            binding.btnPagar.text = "Pagar"
        }
    }

    /**
     * Processa o resultado retornado pelo aplicativo externo de pagamento.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            // Verifica se os dados estão presentes
            if (data == null) {
                binding.listView.adapter = ArrayAdapter(
                    this,
                    R.layout.list_item,
                    R.id.textViewItem,
                    listOf("Erro: Dados da transação não retornados")
                )
                binding.btnPagar.text = "Pagar"
                binding.txtListView.visibility = View.GONE
                return
            }

            // Extrai e processa os dados retornados
            val retNumero = data?.getIntExtra("retNumero", 1)
            val retTexto = data?.getStringExtra("retTexto") ?: "Texto não encontrado"
            val tefDados = data?.getSerializableExtra("xTef") as TEFDados?
            val jsonTefDados = Gson().toJson(tefDados)

            Toast.makeText(this, retTexto, Toast.LENGTH_LONG).show()

            // Verificação de resultados nulos ou cancelados
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                binding.listView.adapter = ArrayAdapter(
                    this,
                    R.layout.list_item,
                    R.id.textViewItem,
                    listOf("A operação foi cancelada pelo usuário")
                )
                binding.btnPagar.text = "Pagar"
                binding.txtListView.visibility = View.GONE
                return
            }

            // Coleta a lista de informações a serem exibidas
            val items = mutableListOf(
                "retNumero: $retNumero - ${if (retNumero == 0) "Sucesso" else "Erro"}",
                "retTexto: $retTexto"
            )

            binding.txtListView.visibility = View.GONE

            // Se a transação foi bem-sucedida, adiciona informações detalhadas
            if (retNumero == 0) {
                // Mapear o indStatus para uma descrição legível
                val indStatus = STATUS_TRANSACAO[tefDados?.indStatus] ?: "Status desconhecido"
                val codigoNSU = tefDados?.codNSU?.toString().orEmpty()
                val codigoAutorizacao = tefDados?.codAut?.toString().orEmpty()
                val bandeira = tefDados?.bandeira.orEmpty()
                val tipoDeComando =
                    TIPOS_COMANDO[tefDados?.tpComando.toString()] ?: "Comando desconhecido"

                // Atualiza o ID da transação para possível estorno
                tefDados?.id?.let { transacaoAtual.id = it }

                items.addAll(
                    listOf(
                        "indStatus: $indStatus",
                        "codigoNSU: $codigoNSU",
                        "codigoAutorizacao: $codigoAutorizacao",
                        "bandeira: $bandeira",
                        "tipoDeComando: $tipoDeComando",
                        jsonTefDados
                    )
                )
                // Preenche a ListView com os itens
                binding.listView.adapter = ArrayAdapter(
                    this,
                    R.layout.list_item,
                    R.id.textViewItem,
                    items
                )
                binding.txtListView.visibility = View.VISIBLE
            } else {
                binding.listView.adapter = ArrayAdapter(
                    this,
                    R.layout.list_item,
                    R.id.textViewItem,
                    listOf(retTexto)
                )
                binding.btnPagar.text = "Pagar"
                binding.txtListView.visibility = View.GONE
                return
            }

            // Atualiza texto do botão baseado no tipo de request e resultado
            binding.btnPagar.text = when (requestCode) {
                REQUEST_PAGAMENTO -> if (retNumero == 0) "Estornar" else "Pagar"
                REQUEST_ESTORNO -> if (retNumero == 0) "Pagar" else "Estornar"
                else -> binding.btnPagar.text
            }
        } catch (e: Exception) {
            Log.e("RESULTADO", "Erro ao processar resultado: ${e.message}", e)
            runOnUiThread {
                Toast.makeText(this, "Erro ao processar resultado: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
            binding.btnPagar.text = "Pagar"
        }
    }

}