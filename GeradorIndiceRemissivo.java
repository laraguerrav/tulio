import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet; // Usaremos HashSet para armazenar palavras-chave para busca rápida e eficiente

public class GeradorIndiceRemissivo {
    
    // --- CLASSE PALAVRA ---
    // Representa uma palavra encontrada no texto e suas ocorrências (linhas)
    static class Palavra {
        private final String palavra;
        private final ListaEncadeada<Integer> ocorrencias; // Armazena os números das linhas onde a palavra aparece

        public Palavra(String palavra, int linha) {
            this.palavra = palavra;
            this.ocorrencias = new ListaEncadeada<>();
            this.ocorrencias.adicionar(linha);
        }

        public String getPalavra() {
            return palavra;
        }

        public ListaEncadeada<Integer> getOcorrencias() {
            return ocorrencias;
        }

        public void adicionarOcorrencia(int linha) {
            // Verifica se a linha já foi adicionada para evitar duplicatas, embora
            // o requisito não especifique isso, é uma boa prática.
            // Para simplicidade, vamos apenas adicionar sem verificar duplicatas aqui.
            this.ocorrencias.adicionar(linha);
        }

        @Override
        public String toString() {
            // Formato: palavra [linha1, linha2, ...]
            return palavra + " " + ocorrencias.toString();
        }
    }

    // --- CLASSE NO_LISTA ---
    // Nó básico para a Lista Encadeada
    static class NoLista<T> {
        T elemento;
        NoLista<T> proximo;

        public NoLista(T elemento) {
            this.elemento = elemento;
            this.proximo = null;
        }

        public T getElemento() {
            return elemento;
        }

        public NoLista<T> getProximo() {
            return proximo;
        }

        public void setProximo(NoLista<T> proximo) {
            this.proximo = proximo;
        }
    }

    // --- CLASSE LISTA_ENCADEADA ---
    // Implementação de uma Lista Encadeada genérica
    static class ListaEncadeada<T> {
        private NoLista<T> primeiro;
        private int tamanho;

        public ListaEncadeada() {
            this.primeiro = null;
            this.tamanho = 0;
        }

        public void adicionar(T elemento) {
            NoLista<T> novoNo = new NoLista<>(elemento);
            if (primeiro == null) {
                primeiro = novoNo;
            } else {
                NoLista<T> atual = primeiro;
                while (atual.getProximo() != null) {
                    atual = atual.getProximo();
                }
                atual.setProximo(novoNo);
            }
            tamanho++;
        }

        public boolean estaVazia() {
            return tamanho == 0;
        }

        public int getTamanho() {
            return tamanho;
        }

        public NoLista<T> getPrimeiro() {
            return primeiro;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            NoLista<T> atual = primeiro;
            while (atual != null) {
                sb.append(atual.getElemento());
                if (atual.getProximo() != null) {
                    sb.append(", ");
                }
                atual = atual.proximo;
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // --- CLASSE NO_ABB ---
    // Nó para a Árvore Binária de Busca
    static class NoABB {
        Palavra palavra;
        NoABB esquerda;
        NoABB direita;

        public NoABB(Palavra palavra) {
            this.palavra = palavra;
            this.esquerda = null;
            this.direita = null;
        }

        public Palavra getPalavra() {
            return palavra;
        }

        public void setPalavra(Palavra palavra) {
            this.palavra = palavra;
        }

        public NoABB getEsquerda() {
            return esquerda;
        }

        public void setEsquerda(NoABB esquerda) {
            this.esquerda = esquerda;
        }

        public NoABB getDireita() {
            return direita;
        }

        public void setDireita(NoABB direita) {
            this.direita = direita;
        }
    }

    // --- CLASSE ARVORE_BINARIA_DE_BUSCA (ABB) ---
    // Implementação da Árvore Binária de Busca
    static class ArvoreBinariaDeBusca {
        private NoABB raiz;

        public ArvoreBinariaDeBusca() {
            this.raiz = null;
        }

        public void inserir(Palavra novaPalavra) {
            raiz = inserirRecursivo(raiz, novaPalavra);
        }

        private NoABB inserirRecursivo(NoABB no, Palavra novaPalavra) {
            if (no == null) {
                return new NoABB(novaPalavra);
            }

            // Compara as palavras ignorando maiúsculas/minúsculas
            int comparacao = novaPalavra.getPalavra().compareToIgnoreCase(no.getPalavra().getPalavra());

            if (comparacao < 0) { // novaPalavra vem antes de no.palavra
                no.setEsquerda(inserirRecursivo(no.getEsquerda(), novaPalavra));
            } else if (comparacao > 0) { // novaPalavra vem depois de no.palavra
                no.setDireita(inserirRecursivo(no.getDireita(), novaPalavra));
            } else {
                // A palavra já existe na ABB, apenas adicione a nova ocorrência
                no.getPalavra().adicionarOcorrencia(novaPalavra.getOcorrencias().getPrimeiro().getElemento());
            }
            return no;
        }

        public Palavra buscar(String palavraBuscada) {
            return buscarRecursivo(raiz, palavraBuscada);
        }

        private Palavra buscarRecursivo(NoABB no, String palavraBuscada) {
            if (no == null) {
                return null; // Palavra não encontrada
            }

            int comparacao = palavraBuscada.compareToIgnoreCase(no.getPalavra().getPalavra());

            if (comparacao < 0) {
                return buscarRecursivo(no.getEsquerda(), palavraBuscada);
            } else if (comparacao > 0) {
                return buscarRecursivo(no.getDireita(), palavraBuscada);
            } else {
                return no.getPalavra(); // Palavra encontrada
            }
        }

        // Percorre a ABB em ordem (in-order traversal) e adiciona ao StringBuilder
        // para manter a ordem alfabética
        public void emOrdem(NoABB no, StringBuilder sb) {
            if (no != null) {
                emOrdem(no.getEsquerda(), sb);
                sb.append(no.getPalavra().toString()).append("\n");
                emOrdem(no.getDireita(), sb);
            }
        }        public NoABB getRaiz() {
            return raiz;
        }
    }
    
    // --- CLASSE TABELA_HASH ---
    // Implementação da Tabela Hash onde cada compartimento contém uma ABB
    static class TabelaHash {
        private static final int TAMANHO_TABELA = 26; // Para as 26 letras do alfabeto (a-z)
        private final ArvoreBinariaDeBusca[] tabela;

        public TabelaHash() {
            tabela = new ArvoreBinariaDeBusca[TAMANHO_TABELA];
            // Inicializa cada compartimento da hash com uma nova ABB
            for (int i = 0; i < TAMANHO_TABELA; i++) {
                tabela[i] = new ArvoreBinariaDeBusca();
            }
        }

        // Função de Hash simples: mapeia a primeira letra da palavra para um índice (0-25)
        private int funcaoHash(String palavra) {
            if (palavra == null || palavra.isEmpty()) {
                return -1; // Caso de palavra inválida
            }
            char primeiraLetra = Character.toLowerCase(palavra.charAt(0));
            if (primeiraLetra >= 'a' && primeiraLetra <= 'z') {
                return primeiraLetra - 'a'; // 'a' -> 0, 'b' -> 1, ..., 'z' -> 25
            }
            return -1; // Para caracteres não alfabéticos ou palavras que não começam com letras
        }

        public void inserir(Palavra palavra) {
            int indice = funcaoHash(palavra.getPalavra());
            if (indice != -1) {
                tabela[indice].inserir(palavra);
            }
        }

        public Palavra buscar(String palavraBuscada) {
            int indice = funcaoHash(palavraBuscada);
            if (indice != -1) {
                return tabela[indice].buscar(palavraBuscada);
            }
            return null; // Palavra não encontrada
        }

        // Gera o índice remissivo completo percorrendo todas as ABBs em ordem
        public String gerarIndiceCompletoOrdenado() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < TAMANHO_TABELA; i++) {
                // Chama o percurso em ordem para cada ABB
                tabela[i].emOrdem(tabela[i].getRaiz(), sb);
            }
            return sb.toString();
        }
    }

    // --- MÉTODO MAIN (CLASSE PRINCIPAL) ---
    public static void main(String[] args) {
        TabelaHash indicePrincipal = new TabelaHash();
        // Usamos HashSet para armazenar as palavras-chave para uma busca O(1)
        HashSet<String> palavrasChave = new HashSet<>();

        // 1. Ler arquivo de palavras-chave
        String arquivoPalavrasChave = "palavras_chave.txt"; // Certifique-se de que este arquivo exista
        System.out.println("Lendo palavras-chave do arquivo: " + arquivoPalavrasChave);
        try (BufferedReader br = new BufferedReader(new FileReader(arquivoPalavrasChave))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                palavrasChave.add(linha.trim().toLowerCase()); // Converte para minúsculas e remove espaços em branco
            }
            System.out.println("Palavras-chave carregadas: " + palavrasChave.size());
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de palavras-chave. Verifique se o arquivo existe e tem permissão de leitura.");
            System.err.println("Erro: " + e.getMessage());
            return; // Encerra o programa se não conseguir ler as palavras-chave
        }

        // 2. Ler arquivo de texto e processar as palavras
        String arquivoTexto = "texto.txt"; // Certifique-se de que este arquivo exista
        System.out.println("Processando texto do arquivo: " + arquivoTexto);
        int numeroLinha = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(arquivoTexto))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                numeroLinha++;
                // Limpa a linha: remove caracteres não alfabéticos e divide em palavras
                // A expressão regular permite letras com acentos (português)
                String[] palavrasNaLinha = linha.replaceAll("[^a-zA-ZáàãâéêíóôõúüçÁÀÃÂÉÊÍÓÔÕÚÜÇ ]", " ").toLowerCase().split("\\s+");

                for (String p : palavrasNaLinha) {
                    if (!p.isEmpty()) { // Garante que não estamos processando strings vazias
                        Palavra palavraExistente = indicePrincipal.buscar(p);
                        if (palavraExistente != null) {
                            // Se a palavra já existe na estrutura, apenas adiciona a nova ocorrência
                            palavraExistente.adicionarOcorrencia(numeroLinha);
                        } else {
                            // Se a palavra não existe, cria uma nova Palavra e a insere na Tabela Hash
                            indicePrincipal.inserir(new Palavra(p, numeroLinha));
                        }
                    }
                }
            }
            System.out.println("Texto processado. Total de linhas: " + numeroLinha);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo de texto. Verifique se o arquivo existe e tem permissão de leitura.");
            System.err.println("Erro: " + e.getMessage());
            return; // Encerra o programa se não conseguir ler o texto
        }

        // 3. Gerar o índice remissivo final, filtrando apenas pelas palavras-chave
        System.out.println("Gerando índice remissivo...");
        StringBuilder indiceFinal = new StringBuilder();
        // Obtém todas as palavras processadas da Tabela Hash, já em ordem alfabética
        String indiceBruto = indicePrincipal.gerarIndiceCompletoOrdenado();
        String[] linhasIndice = indiceBruto.split("\n");

        for (String linhaIndice : linhasIndice) {
            if (linhaIndice.isEmpty()) {
                continue;
            }
            // Extrai a palavra (a parte antes do primeiro espaço)
            String palavraDoIndice = linhaIndice.split(" ")[0];
            // Verifica se a palavra é uma das palavras-chave
            if (palavrasChave.contains(palavraDoIndice)) {
                indiceFinal.append(linhaIndice).append("\n");
            }
        }

        // 4. Salvar o índice remissivo em um arquivo de saída
        String arquivoSaida = "indice_remissivo.txt";
        System.out.println("Salvando índice remissivo em: " + arquivoSaida);
        try (FileWriter fw = new FileWriter(arquivoSaida)) {
            fw.write(indiceFinal.toString());
            System.out.println("Índice remissivo gerado e salvo com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo de saída.");
            System.err.println("Erro: " + e.getMessage());
        }
    }
}