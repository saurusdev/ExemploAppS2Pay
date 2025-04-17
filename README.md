# 📄 Documentação do Projeto - App de Integração S2pay

## 📌 Objetivo

Este aplicativo tem como finalidade demonstrar exemplos práticos de integração entre aplicativos, com foco na realização de chamadas de pagamento e estorno por meio de intents ou comunicação direta com o app principal (App S2pay).

---

## 📥 Pré-requisitos

### Instalar o App S2pay

O App S2Pay é responsável por processar as requisições de pagamento e estorno. Para instalá-lo:

1. Acesse a Loja Oculta do Safrapay (disponível apenas em maquininhas com o sistema compatível).
2. Na tela principal do Loucher, toque nos três pontinhos (menu de opções).
3. Digite o código da função `9998` para acessar a loja oculta.
4. Pesquise e instale o App S2pay.

### Ambiente de Homologação

Caso esteja utilizando uma maquininha de homologação, será necessário solicitar o atendimento técnico para instalação do App S2pay via ADB.

### Licenciamento

O App S2pay exige uma licença válida para funcionar corretamente.

> Solicite uma licença com a equipe responsável antes de iniciar os testes com o aplicativo de exemplo.

---

## ⚙️ Funcionamento

- Ao clicar em **“Pagar”**, o app irá chamar o App S2pay com os parâmetros necessários.
- A resposta da transação será capturada e exibida na tela.
- Também é possível simular um estorno usando o botão **“Estornar”** após uma transação.

---

## 💳 Tela Principal de Pagamento (`MainActivity`)

### 📌 Descrição Geral

A `MainActivity` gerencia a interface de pagamento, onde o usuário pode:

- Inserir um valor;
- Selecionar tipo de pagamento (Débito, Crédito, ou Pix);
- Informar parcelas (quando aplicável);
- Realizar pagamento ou estorno via TEF usando o SDK da Saurus (`saurustef_util`).

### 🏗️ Componentes Utilizados

- **View Binding**: `ActivityMainBinding`
- **Modelos**: `TransacaoInfo`
- **Serviços**: `PaymentService` (orquestra pagamento e estorno)

### ⚙️ Ciclo de Vida

- `onCreate(Bundle?)`: Inicializa UI, define listeners e configura o Spinner de tipo de pagamento.

### 💳 Funcionalidades Principais

#### 🔹 `realizarPagamento()`

- Converte e valida valor
- Define parcelas
- Envia para o `PaymentService`

#### 🔹 `realizarEstorno()`

- Usa o ID salvo da última transação bem-sucedida para realizar o estorno

---

## 🔄 Tratamento de Retorno do Pagamento

### `onActivityResult(requestCode, resultCode, data)`

- Analisa os dados retornados
- Exibe informações como: autorização, NSU, bandeira, status
- Atualiza UI e estado do botão

---

## 📦 Constantes Importadas

- `TIPOS_PAGAMENTO`
- `TIPOS_COMANDO`
- `STATUS_TRANSACAO`
- `REQUEST_PAGAMENTO` / `REQUEST_ESTORNO`

### Funções utilitárias:

- `aplicarMascaraMoeda(EditText)`
- `converterValorParaDouble(String)`
- `validarValor(Double?)`

---

## 🔐 Dependência Externa

- `TEFDados` (pacote: `br.com.saurus.saurustef_util`) – Dados da transação TEF

---

## 📋 Comportamentos da Interface

- **Campo `edtValor`**: valor com máscara monetária
- **Spinner**: tipo de pagamento (exibe parcelas se "Crédito")
- **Botão `btnPagar`**: alterna entre "Pagar" e "Estornar"
- **ListView**: mostra o resultado da operação

---

## 🧪 Pontos de Atenção e Tratamento de Erros

- Valores inválidos exibem Toast de erro
- Exceções são tratadas e mostradas ao usuário
- `Intent` nulo ou cancelado mostra mensagem padrão

---

## 📈 Fluxo Resumido da Transação

    A[Usuário insere valor e escolhe tipo] --> B[Usuário clica em Pagar]
    B --> C[realizarPagamento()]
    C --> D[enviarPagamento()]
    D --> E[onActivityResult()]
    E --> F[Exibe sucesso ou erro na tela]
    F --> G[Atualiza botão: Estornar ou Pagar]
