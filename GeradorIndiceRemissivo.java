import java.io.*;
import java.util.*;

public class GeradorIndiceRemissivo {
    static class NoLista {
        int linha;
        NoLista prox;
        NoLista(int linha) {
            this.linha = linha;
            this.prox = null;
        }
    }

    static class ListaEncadeada {
        private NoLista inicio;
        private NoLista fim;
        public ListaEncadeada() {
            this.inicio = null;
            this.fim = null;
        }
        public void inserir(int linha) {
            NoLista novo = new NoLista(linha);
            if (inicio == null) {
                inicio = novo;
                fim = novo;
            } else {
                fim.prox = novo;
                fim = novo;
            }
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            NoLista atual = inicio;
            sb.append("[");
            while (atual != null) {
                sb.append(atual.linha);
                if (atual.prox != null) sb.append(", ");
                atual = atual.prox;
            }
            sb.append("]");
            return sb.toString();
        }
    }

    static class Palavra {
        private final String palavra;
        private final ListaEncadeada ocorrencias = new ListaEncadeada();
        public Palavra(String palavra, int linha) {
            this.palavra = palavra;
            this.ocorrencias.inserir(linha);
        }
        public String getPalavra() { return palavra; }
        public void adicionarOcorrencia(int linha) { this.ocorrencias.inserir(linha); }
        @Override
        public String toString() { return palavra + " " + ocorrencias.toString(); }
    }
    // Árvore Binária de Busca (ABB)
    static class ArvoreBinariaDeBusca {
        static class NoABB {
            Palavra palavra;
            NoABB esquerda, direita;
            public NoABB(Palavra palavra) {
                this.palavra = palavra;
                this.esquerda = null;
                this.direita = null;
            }
        }
        private NoABB raiz;
        public ArvoreBinariaDeBusca() {
            this.raiz = null;
        }
        public void inserir(String palavra, int linha) {
            raiz = inserirNaArvore(raiz, palavra, linha);
        }
        private NoABB inserirNaArvore(NoABB no, String palavra, int linha) {
            if (no == null) {
                return new NoABB(new Palavra(palavra, linha));
            }
            int cmp = palavra.compareToIgnoreCase(no.palavra.getPalavra());
            if (cmp < 0) {
                no.esquerda = inserirNaArvore(no.esquerda, palavra, linha);
            } else if (cmp > 0) {
                no.direita = inserirNaArvore(no.direita, palavra, linha);
            } else {
                no.palavra.adicionarOcorrencia(linha);
            }
            return no;
        }
        public Palavra buscar(String palavra) {
            return buscarNaArvore(raiz, palavra);
        }
        private Palavra buscarNaArvore(NoABB no, String palavra) {
            if (no == null) return null;
            int cmp = palavra.compareToIgnoreCase(no.palavra.getPalavra());
            if (cmp < 0) {
                return buscarNaArvore(no.esquerda, palavra);
            } else if (cmp > 0) {
                return buscarNaArvore(no.direita, palavra);
            } else {
                return no.palavra;
            }
        }
        // Adapta percurso em ordem para array
        public void percorrerEmOrdem(StringBuilder sb, String[] palavrasChave) {
            percorrerEmOrdem(raiz, sb, palavrasChave);
        }
        private void percorrerEmOrdem(NoABB no, StringBuilder sb, String[] palavrasChave) {
            if (no == null) return;
            percorrerEmOrdem(no.esquerda, sb, palavrasChave);
            String palavraAtual = no.palavra.getPalavra();
            for (String chave : palavrasChave) {
                if (palavraAtual.equals(chave)) {
                    sb.append(no.palavra.toString()).append('\n');
                    break;
                }
            }
            percorrerEmOrdem(no.direita, sb, palavrasChave);
        }
    }
    static class Hash {
        private static final int TAMANHO = 26;
        private final ArvoreBinariaDeBusca[] tabela = new ArvoreBinariaDeBusca[TAMANHO];
        private int hash(String palavra) {
            if (palavra == null || palavra.isEmpty()) return -1;
            char c = Character.toLowerCase(palavra.charAt(0));
            return (c >= 'a' && c <= 'z') ? c - 'a' : -1;
        }
        public void inserir(String palavra, int linha) {
            int indice = hash(palavra);
            if (indice != -1) {
                if (tabela[indice] == null) tabela[indice] = new ArvoreBinariaDeBusca();
                tabela[indice].inserir(palavra, linha);
            }
        }
        public Palavra buscar(String palavra) {
            int indice = hash(palavra);
            if (indice == -1 || tabela[indice] == null) return null;
            return tabela[indice].buscar(palavra);
        }
        // Adapta método gerarIndice para aceitar array de palavras-chave
        public StringBuilder gerarIndice(String[] palavrasChave) {
            StringBuilder sb = new StringBuilder(1024);
            for (int i = 0; i < TAMANHO; i++) {
                if (tabela[i] != null) tabela[i].percorrerEmOrdem(sb, palavrasChave);
            }
            return sb;
        }
    }
    public static void main(String[] args) {
        Hash indice = new Hash();
        // Troca HashSet por array de palavras-chave
        String[] palavrasChave;
        int totalPalavrasChave = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("palavras_chave.txt"), 8192)) {
            List<String> listaTemp = new ArrayList<>();
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    listaTemp.add(linha.trim().toLowerCase());
                }
            }
            palavrasChave = listaTemp.toArray(new String[0]);
            totalPalavrasChave = palavrasChave.length;
            System.out.println("Palavras-chave carregadas: " + totalPalavrasChave);
        } catch (IOException e) {
            System.err.println("Erro ao ler palavras-chave: " + e.getMessage());
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader("texto.txt"), 8192)) {
            String linha;
            int numLinha = 0;
            while ((linha = br.readLine()) != null) {
                numLinha++;
                String linhaLimpa = linha.toLowerCase().replaceAll("[^a-záàãâéêíóôõúüç -]", " ");
                Set<String> palavrasUnicasLinha = new HashSet<>();
                for (String palavra : linhaLimpa.split("\\s+")) {
                    if (!palavra.isEmpty()) {
                        String palavraBase = palavra;
                        if (palavraBase.endsWith("es") && palavraBase.length() > 2) {
                            palavraBase = palavraBase.substring(0, palavraBase.length() - 2);
                        } else if (palavraBase.endsWith("s") && palavraBase.length() > 1) {
                            palavraBase = palavraBase.substring(0, palavraBase.length() - 1);
                        }
                        palavrasUnicasLinha.add(palavraBase);
                    }
                }
                for (String palavraBase : palavrasUnicasLinha) {
                    Palavra p = indice.buscar(palavraBase);
                    if (p != null) {
                        p.adicionarOcorrencia(numLinha);
                    } else {
                        indice.inserir(palavraBase, numLinha);
                    }
                }
            }
            System.out.println("Texto processado. Total de linhas: " + numLinha);
        } catch (IOException e) {
            System.err.println("Erro ao processar texto: " + e.getMessage());
            return;
        }
        // Corrige chamada para gerarIndice e remove variáveis não utilizadas
        StringBuilder resultado = indice.gerarIndice(palavrasChave);
        try (FileWriter fw = new FileWriter("indice_remissivo.txt")) {
            fw.write(resultado.toString());
            System.out.println("Índice remissivo gerado com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao salvar índice: " + e.getMessage());
        }
    }
}