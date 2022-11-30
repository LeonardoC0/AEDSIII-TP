import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class lzw {

    public static Scanner sc = new Scanner(System.in);

    public static void imprimeArquivo (RandomAccessFile arq, long comeco) { // imprime as ids de um arquivo
		int ultimaId;
		int tamRegAtual;
		long pos0;
		int idAtual;
		
		try {
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = -1;
			System.out.print("| ");
			while(idAtual != ultimaId) { // varre o arquivo e imprime as ids
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				if(arq.readChar() != '*') {
					idAtual = arq.readInt();
					System.out.print(idAtual + ", ");
				} else {
					System.out.print("*, ");
				}
				arq.seek(pos0);
				arq.skipBytes(tamRegAtual);
				System.out.print(tamRegAtual + "B | "); // teste
			}
			System.out.println("");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
    
        public static void comprimeLZW(RandomAccessFile arq, long comeco) {
            byte byteAtual;
            ArrayList<byte[]> dicionario = new ArrayList<byte[]>();
            byte[] itemAtual;
            byte[] I;
            ArrayList<Byte> sequenciaCodificada = new ArrayList<Byte>();
            
            try {
                arq.seek(comeco);
                
                for(int i=0; i<256; i++) {
                    itemAtual = new byte[1];
                    itemAtual[0] = (byte) i;
                    dicionario.add(itemAtual);
                }
                
                
                I = new byte[0];
                boolean acabou = false;
                boolean primeiraMetade = true;
                while(!acabou) {
                    
                    byteAtual = arq.readByte();

                    itemAtual = new byte[I.length + 1];
                    for(int i=0; i<I.length; i++) {
                        itemAtual[i] = I[i];
                    }
                    itemAtual[I.length] = byteAtual;
                    boolean contem = false;
                    for(int i=0; i<dicionario.size(); i++) {
                        if(dicionario.get(i).length == itemAtual.length) {
                            boolean encontrouTodos = true;
                            for (int j=0; j<itemAtual.length; j++) {
                                if(itemAtual[j] != dicionario.get(i)[j]) {
                                    encontrouTodos = false;
                                }
                            }
                            if(encontrouTodos) {
                                contem = true;
                            }
                        }
                    }

                    if(contem) {

                        I = new byte[itemAtual.length];
                        for(int i=0; i<itemAtual.length; i++) {
                            I[i] = itemAtual[i];
                        }
                    } 

                    else {

                        int posicaoEncontrada = -1;
                        for(int i=0; i<dicionario.size(); i++) {
                            if(dicionario.get(i).length == I.length) {
                                boolean encontrouTodos = true;
                                for (int j=0; j<I.length; j++) {
                                    if(I[j] != dicionario.get(i)[j]) {
                                        encontrouTodos = false;
                                    }
                                }
                                if(encontrouTodos) {
                                    posicaoEncontrada = i;
                                }
                            }
                        }
                        if(primeiraMetade) { 
                            byte byte1 = (byte) ((posicaoEncontrada >> 4) & 0xFF);
                            byte byte2 = (byte) ((posicaoEncontrada << 4) & 0xF0);
                            sequenciaCodificada.add(byte1);
                            sequenciaCodificada.add(byte2);
                            primeiraMetade = false; 
                        } else {
                            byte byte1 = (byte) (((posicaoEncontrada >> 8) & 0xF) | sequenciaCodificada.get(sequenciaCodificada.size() - 1));
                            byte byte2 = (byte) (posicaoEncontrada & 0xFF);
                            sequenciaCodificada.remove(sequenciaCodificada.size() - 1);
                            sequenciaCodificada.add(byte1);
                            sequenciaCodificada.add(byte2);
                            primeiraMetade = true;
                        }

                        dicionario.add(itemAtual);
                        
                        I = new byte[1];
                        I[0] = byteAtual;
                    }
              
                    if(arq.getFilePointer() == arq.length() - 1) {
                        
                        int modAtual = I[0];
                        ArrayList<Byte> sequenciaAtual = new ArrayList<Byte>();
                        for(int i=0; i<12; i++) {
                            sequenciaAtual.add((byte) (modAtual & 1));
                            modAtual = modAtual >> 1;
                        }
                        acabou = true;
                        
                        //salva no arquivo
                        RandomAccessFile arqComprimido = new RandomAccessFile("contasLZW.db", "rw");
                        arqComprimido.seek(comeco);
                        arqComprimido.setLength(0);
                        for(Byte p : sequenciaCodificada) {
                            arqComprimido.writeByte(p);
                        }
                        System.out.println("\nArquivo inicial: " + arq.length() + " bytes");
                        System.out.println("Arquivo final: " + arqComprimido.length() + " bytes");
                        System.out.println("Compressão: " + (((float) arqComprimido.length() / arq.length()) * 100) + "% do tamanho");
                        System.out.println("\nAperte enter para continuar.");
                        sc.nextLine();
                        arqComprimido.close();
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        
        
        public static void descomprimeLZW(RandomAccessFile arq, long comeco) {
            try {
                RandomAccessFile arqComprimido = new RandomAccessFile("crud-AEDS3/dados/contasLZW.db", "rw");
                if(arqComprimido.length() == 0) {
                    System.out.println("\nArquivo vazio.\n\nAperte enter para continuar.\n");
                    sc.nextLine();
                    arqComprimido.close();
                    return;
                }
                
                ArrayList<Integer> sequenciaCodificada = new ArrayList<Integer>();
                boolean primeiraMetade = true;
                long pos0 = arqComprimido.getFilePointer();
                for(int i=0; arqComprimido.getFilePointer() < arqComprimido.length()-1; i++) {
                    if(primeiraMetade) {
                        int aux = arqComprimido.readUnsignedByte();
                        pos0 = arqComprimido.getFilePointer();
                        aux = aux << 4;
                        aux = aux | (((int) arqComprimido.readUnsignedByte() >> 4) & 15);
                        sequenciaCodificada.add(aux);
                        primeiraMetade = false;
                        arqComprimido.seek(pos0);
                    } else {
                        int aux;
                        aux = arqComprimido.readUnsignedByte() & 15;
                        aux = aux << 8;
                        int x = arqComprimido.readUnsignedByte();
                        aux = aux | x ;
                        sequenciaCodificada.add(aux);
                        primeiraMetade = true;
                    }
                }
                
                ArrayList<byte[]> dicionario = new ArrayList<byte[]>();
                ArrayList<Byte> sequenciaSaida = new ArrayList<Byte>();
                int cW, pW;
                byte[] stringC, stringP;
                byte[] itemAtual;
                byte c;
                byte[] p;
                for(int i=0; i<256; i++) {
                    itemAtual = new byte[1];
                    itemAtual[0] = (byte) i;
                    dicionario.add(itemAtual);
                }
                
                cW = sequenciaCodificada.get(0);
            
                stringC = dicionario.get(cW);
                for(int i=0; i<stringC.length; i++) {
                    sequenciaSaida.add(stringC[i]);
                }
                
                boolean acabou = false;
                if(sequenciaCodificada.size() == 1) {
                    acabou = true;
                }
                int indiceSeqCod = 1;
                while(!acabou) {
                    pW = cW;
                    
                    cW = sequenciaCodificada.get(indiceSeqCod);
                    indiceSeqCod++;
                    
                    boolean existe = false;
                    if(cW < dicionario.size()) {
                        existe = true;
                        stringC = dicionario.get(cW);
                    } else {
                        existe = false;
                    }

                    if(existe) {
                        
                        for(int i=0; i<stringC.length; i++) {
                            sequenciaSaida.add(stringC[i]);
                        }
                        
                        stringP = dicionario.get(pW);
                        p = stringP;
                        
                        c = stringC[0];
                        
                        byte[] stringParaAdicionar = new byte[p.length + 1];
                        for(int i=0; i<p.length; i++) {
                            stringParaAdicionar[i] = p[i];
                        }
                        stringParaAdicionar[p.length] = c;
                        dicionario.add(stringParaAdicionar);
                    }
                    
                    else {
                        
                        stringP = dicionario.get(pW);
                        p = stringP;
                        
                        c = stringC[0];
                        
                        byte[] stringParaAdicionar = new byte[p.length + 1];
                        for(int i=0; i<p.length; i++) {
                            stringParaAdicionar[i] = p[i];
                        }
                        stringParaAdicionar[p.length] = c;
                        dicionario.add(stringParaAdicionar);
                        for(int i=0; i<stringParaAdicionar.length; i++) {
                            sequenciaSaida.add(stringParaAdicionar[i]);
                        }
                    }
                    
                    if(indiceSeqCod == sequenciaCodificada.size()) {
                        acabou = true;
                    }
                }
                
                arq.seek(comeco);
                arq.setLength(0);
                for(Byte byteAtual : sequenciaSaida) {
                    arq.writeByte(byteAtual);
                }
                
                System.out.println("\nArquivo decodificado:");
                imprimeArquivo(arq, comeco);
                System.out.println("\nAperte enter para continuar.");
                sc.nextLine();
                
                
                arqComprimido.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
}