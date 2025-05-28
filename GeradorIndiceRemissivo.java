import java.io.*;
import java.util.*;

public class GeradorIndiceRemissivo {
    // Implementação da lista encadeada para ocorrências
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
    static class NoABB {
        Palavra palavra;
        NoABB esquerda, direita;
        public NoABB(Palavra palavra) {
            this.palavra = palavra;
            this.esquerda = this.direita = null;
        }
    }
    static class Hash {
        private static final int TAMANHO = 26;
        private final NoABB[] tabela = new NoABB[TAMANHO];
        private int hash(String palavra) {
            if (palavra == null || palavra.isEmpty()) return -1;
            char c = Character.toLowerCase(palavra.charAt(0));
            return (c >= 'a' && c <= 'z') ? c - 'a' : -1;
        }
        public void inserir(String palavra, int linha) {
            int indice = hash(palavra);
            if (indice != -1) {
                tabela[indice] = inserirNaArvore(tabela[indice], palavra, linha);
            }
        }
        private NoABB inserirNaArvore(NoABB no, String palavra, int linha) {
            if (no == null) {
                return new NoABB(new Palavra(palavra, linha));
            }
            int comparacao = palavra.compareToIgnoreCase(no.palavra.getPalavra());
            if (comparacao < 0) {
                no.esquerda = inserirNaArvore(no.esquerda, palavra, linha);
            } else if (comparacao > 0) {
                no.direita = inserirNaArvore(no.direita, palavra, linha);
            } else {
                no.palavra.adicionarOcorrencia(linha);
            }
            return no;
        }
        public Palavra buscar(String palavra) {
            int indice = hash(palavra);
            if (indice == -1) return null;
            return buscarNaArvore(tabela[indice], palavra);
        }
        private Palavra buscarNaArvore(NoABB no, String palavra) {
            if (no == null) return null;
            int comparacao = palavra.compareToIgnoreCase(no.palavra.getPalavra());
            if (comparacao < 0) {
                return buscarNaArvore(no.esquerda, palavra);
            } else if (comparacao > 0) {
                return buscarNaArvore(no.direita, palavra);
            } else {
                return no.palavra;
            }
        }
        public StringBuilder gerarIndice() {
            StringBuilder sb = new StringBuilder(1024);
            for (int i = 0; i < TAMANHO; i++) {
                percorrerEmOrdem(tabela[i], sb);
            }
            return sb;
        }
        private void percorrerEmOrdem(NoABB no, StringBuilder sb) {
            if (no == null) return;
            percorrerEmOrdem(no.esquerda, sb);
            sb.append(no.palavra.toString()).append('\n');
            percorrerEmOrdem(no.direita, sb);
        }
    }
    public static void main(String[] args) {
        Hash indice = new Hash();
        Set<String> palavrasChave = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("palavras_chave.txt"), 8192)) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    palavrasChave.add(linha.trim().toLowerCase());
                }
            }
            System.out.println("Palavras-chave carregadas: " + palavrasChave.size());
        } catch (IOException e) {
            System.err.println("Erro ao ler palavras-chave: " + e.getMessage());
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader("texto.txt"), 8192)) {
            String linha;
            int numLinha = 0;
            while ((linha = br.readLine()) != null) {
                numLinha++;
                for (String palavra : linha.replaceAll("[^a-zA-ZáàãâéêíóôõúüçÁÀÃÂÉÊÍÓÔÕÚÜÇ ]", " ")
                        .toLowerCase().split("\\s+")) {
                    if (!palavra.isEmpty()) {
                        Palavra p = indice.buscar(palavra);
                        if (p != null) {
                            p.adicionarOcorrencia(numLinha);
                        } else {
                            indice.inserir(palavra, numLinha);
                        }
                    }
                }
            }
            System.out.println("Texto processado. Total de linhas: " + numLinha);
        } catch (IOException e) {
            System.err.println("Erro ao processar texto: " + e.getMessage());
            return;
        }
        StringBuilder resultado = new StringBuilder(1024);
        String[] linhas = indice.gerarIndice().toString().split("\n");
        for (String linha : linhas) {
            if (!linha.isEmpty()) {
                String palavraAtual = linha.split(" ")[0];
                if (palavrasChave.contains(palavraAtual)) {
                    resultado.append(linha).append('\n');
                }
            }
        }
        try (FileWriter fw = new FileWriter("indice_remissivo.txt")) {
            fw.write(resultado.toString());
            System.out.println("Índice remissivo gerado com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao salvar índice: " + e.getMessage());
        }
    }
}