import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.TreeMap;

class Node {
    Node left, right;
    double value;
    String character;

    public Node(double value, String character) {
        this.value = value;
        this.character = character;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        character = left.character + right.character;
        if (left.value < right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }
}


public class huffman {
    static final boolean readFromFile = false;
    static final boolean newTextBasedOnOldOne = false;
    static RandomAccessFile raf,teste;

    static PriorityQueue<Node> nodes = new PriorityQueue<>((o1, o2) -> (o1.value < o2.value) ? -1 : 1);
    static TreeMap<Character, String> codes = new TreeMap<>();
    static String text = "";
    static String encoded = "";
    static String decoded = "";
    static int ASCII[] = new int[12558];

        public static boolean compressaoHuffman(Scanner scanner,RandomAccessFile arq) throws IOException {
            arq.seek(0);
            while(arq.getFilePointer() < arq.length() -1){
                text += arq.readChar();
            }

                ASCII = new int[40000];

                nodes.clear();
                codes.clear();
                encoded = "";
                decoded = "";
                calcularIntervalos(nodes);
                construirArvore(nodes);
                gerarCodigos(nodes.peek(), "");

                raf = new RandomAccessFile("crud-AEDS3/dados/contasHuffman.db", "rw");
                comprimirHuffman(raf);
                descomprimirHuffman(arq);
                
                return false;

        }


        private static void descomprimirHuffman(RandomAccessFile arq) throws IOException {
            decoded = "";
            Node node = nodes.peek();
            for (int i = 0; i < encoded.length(); ) {
                Node tmpNode = node;
                while (tmpNode.left != null && tmpNode.right != null && i < encoded.length()) {
                    if (encoded.charAt(i) == '1')
                        tmpNode = tmpNode.right;
                    else tmpNode = tmpNode.left;
                    i++;
                }
                if (tmpNode != null)
                    if (tmpNode.character.length() == 1)
                        decoded += tmpNode.character;
                    else
                        System.out.println("Input not Valid");

            }
            arq.seek(0);
            for(int i = 0; i < decoded.length() ;i++)    arq.writeChar(decoded.charAt(i));
        }

        public static void comprimirHuffman(RandomAccessFile raf) throws IOException {
            encoded = "";
            for (int i = 0; i < text.length(); i++)
                encoded += codes.get(text.charAt(i));
            raf.writeUTF(encoded);
        }

        private static void construirArvore(PriorityQueue<Node> vector) {
            while (vector.size() > 1)
                vector.add(new Node(vector.poll(), vector.poll()));
        }

        private static void calcularIntervalos(PriorityQueue<Node> vector) {

            for (int i = 0; i < text.length(); i++){
                ASCII[text.charAt(i)]++;
            }
                

            for (int i = 0; i < ASCII.length; i++)
                if (ASCII[i] > 0) {
                    vector.add(new Node(ASCII[i] / (text.length() * 1.0), ((char) i) + ""));
                }
        }

        private static void gerarCodigos(Node node, String s) {
            if (node != null) {
                if (node.right != null)
                    gerarCodigos(node.right, s + "1");

                if (node.left != null)
                    gerarCodigos(node.left, s + "0");

                if (node.left == null && node.right == null)
                    codes.put(node.character.charAt(0), s);
            }
        }

    }

