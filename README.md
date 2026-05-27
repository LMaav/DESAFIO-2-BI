# Sistema de Gestão de Estacionamento

Aplicativo Android (Kotlin) que gerencia entrada/saída de veículos, vagas, clientes
e cálculo de custo. Autenticação via **Firebase Auth** e dados no **Cloud Firestore**.

Projeto feito para o desafio de Desenvolvimento Mobile — versão **simples e direta**,
usando Activities + um Repository que conversa com o Firebase (sem bibliotecas complexas).

---

## 1. O que você precisa

- **Android Studio** (versão recente — Koala ou mais nova).
- Uma conta Google para usar o **Firebase**.
- Um emulador ou celular Android para rodar.

---

## 2. Configurar o Firebase (passo a passo)

O app só funciona depois de ligar ele ao SEU projeto Firebase.

### 2.1 Criar o projeto
1. Acesse https://console.firebase.google.com
2. Clique em **Adicionar projeto** → dê um nome (ex: `estacionamento`) → conclua.

### 2.2 Registrar o app Android
1. No projeto, clique no ícone **Android** para adicionar um app.
2. Em **Nome do pacote Android**, digite exatamente:
   ```
   com.unisanta.estacionamento
   ```
3. Clique em **Registrar app**.
4. Baixe o arquivo **`google-services.json`**.
5. Coloque esse arquivo dentro da pasta **`app/`** deste projeto.
   (pode apagar o arquivo `COLOQUE_AQUI_O_google-services.json.txt`)

### 2.3 Ativar a Autenticação
1. No menu lateral: **Criação → Authentication → Começar**.
2. Aba **Sign-in method** → ative **E-mail/senha** → salvar.

### 2.4 Criar o banco Firestore
1. No menu lateral: **Criação → Firestore Database → Criar banco de dados**.
2. Escolha **Iniciar no modo de teste** (libera leitura/escrita por 30 dias — ótimo para o desafio).
3. Escolha a localização e conclua.

> As coleções (`clientes`, `carros`, `estacionamento`) são criadas **sozinhas** quando
> você usar o app pela primeira vez. Não precisa criar nada na mão.

---

## 3. Abrir e rodar

1. Abra o Android Studio → **Open** → selecione a pasta **`EstacionamentoApp`**.
2. Espere o Gradle sincronizar (ele baixa as dependências e o Gradle Wrapper sozinho).
3. Confirme que o `google-services.json` está em `app/`.
4. Clique em **Run ▶** com um emulador ou celular conectado.

---

## 4. Como usar o app

1. **Cadastre** uma conta (nome, e-mail, senha) — isso cria seu login de operador.
2. Na **Home** você vê o total de vagas e quantas estão livres.
3. Vá em **Clientes** e adicione um cliente (o dono do carro).
4. Vá em **Cadastrar Carro** (placa, modelo, cor, escolha o cliente).
5. Em **Gerenciar Vagas**:
   - Toque numa vaga **verde (livre)** → escolha o carro → **Registrar Entrada**.
   - Toque numa vaga **vermelha (ocupada)** → abre os detalhes → **Registrar Saída** e **Calcular Custo**.

---

## 5. Onde está cada coisa (organização do código)

```
app/src/main/java/com/unisanta/estacionamento/
├── data/
│   ├── Cliente.kt                  -> modelo da coleção "clientes"
│   ├── Carro.kt                    -> modelo "carros" + calcularCustoEstacionamento()
│   ├── Estacionamento.kt           -> modelo "estacionamento/config" + verificarDisponibilidade()
│   └── EstacionamentoRepository.kt -> TODA a conversa com o Firestore
└── ui/
    ├── SplashActivity.kt           -> decide Login ou Home (auth persistente)
    ├── LoginActivity.kt            -> login (Firebase Auth)
    ├── CadastroActivity.kt         -> cria conta + documento do cliente
    ├── HomeActivity.kt             -> dashboard + lista em tempo real
    ├── GerenciarVagasActivity.kt   -> grid de vagas (verde/vermelho)
    ├── CadastrarCarroActivity.kt   -> formulário de carro
    ├── DetalhesCarroActivity.kt    -> entrada, saída e cálculo de custo
    └── ClientesActivity.kt         -> lista e adiciona clientes
```

## 6. Requisitos do desafio cobertos

| Requisito | Onde está |
|-----------|-----------|
| RF-01 Login | LoginActivity |
| RF-02 Cadastro | CadastroActivity + criarClienteParaOperador() |
| RF-03 Logout | HomeActivity.logout() |
| RF-04 Auth persistente | SplashActivity |
| RF-05 Cadastrar carro | CadastrarCarroActivity + adicionarCarro() |
| RF-06 Entrada | entrarNoEstacionamento() |
| RF-07 Saída | sairDoEstacionamento() |
| RF-08 Calcular custo | Carro.calcularCustoEstacionamento() |
| RF-10 Listar estacionados (tempo real) | observarCarrosEstacionados() |
| RF-11 Adicionar (valida placa/vaga) | adicionarCarro() |
| RF-12 Remover | removerCarro() |
| RF-13 Buscar carro | buscarCarro() |
| RF-14 Disponibilidade | Estacionamento.verificarDisponibilidade() |
| RF-15 Exibir vagas e carros | GerenciarVagasActivity |
