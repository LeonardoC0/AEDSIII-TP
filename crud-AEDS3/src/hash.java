import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class hash {

	public static Scanner sc = new Scanner(System.in);

	public static Conta leRegistro (RandomAccessFile arq, long comeco, long pos0) { // le um registro e retorna esse registro como objeto (nao le o tamanho do registro)
		String nomePessoa;
		String[] email;
		String nomeUsuario;
		String senha;
		String cpf;
		String cidade;
		float saldoConta;
		int numEmails;
		int tamString;
		int transferenciasRealizadas;
		int id;
		
		try {
			arq.readChar();
			id = arq.readInt();
			tamString = arq.readInt();
			nomePessoa = arq.readUTF();
			numEmails = arq.readInt();
			email = new String[numEmails];
			for(int i=0; i<numEmails; i++) { // le todos os emails
				tamString = arq.readInt();
				email[i] = arq.readUTF();
			}
			tamString = arq.readInt();
			nomeUsuario = arq.readUTF();
			tamString = arq.readInt();
			senha = arq.readUTF();
			cpf = arq.readUTF();
			tamString = arq.readInt();
			cidade = arq.readUTF();
			transferenciasRealizadas = arq.readInt();
			saldoConta = arq.readFloat();
			
			Conta conta = new Conta(); // cria um objeto para ser retornado
			return conta;
		}  catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return new Conta();
	}

	public static long getEndDir (RandomAccessFile arqDir, long comeco, int idDesejada) { // varre o arquivo de diretorio buscando o registro de id selecionado e retorna o endereco do bucket correspondente
		long endDir = -1;
		try {
			arqDir.seek(comeco);
			int profGlobal = arqDir.readInt();
			double tamDir = Math.pow(2, profGlobal);
			// para cada valor no diretorio
			for(int i=0; i<tamDir; i++) { 
				// checa se eh o bucket da id desejada
				if(i == idDesejada % tamDir) {	
					// salva o endereco do bucket
					endDir = arqDir.readLong();
				} else {
					// pula para o proximo
					arqDir.readLong();
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return endDir;
	}
	

    public static void criaHash(RandomAccessFile arq, long comeco) { // cria o arquivo de hash inicial
		// abre o arquivo de indice hash
		// limpa o arquivo de indice hash
		// abre o arquivo de diretorio
		// limpa o arquivo de diretorio
		// inicializa os arquivos de indice e diretorio com p=1 e buckets vazios
		// para cada registro no arquivo
			// le o registro
			// ve no diretorio em qual bucket vai cair
			// acessa o bucket
			// se o bucket ja estiver cheio
				// se a profundidade local for menor que a profundidade global
					// aumenta a profundidade local
					// cria um bucket novo
					// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1)) 
					// rebalanceia o bucket atual com o seu segundo %
					// insere o registro no bucket equivalente
				// se a profundidade local for igual a profundidade global
					// aumenta a profundidade local
					// aumenta a profundidade global
					// dobra o tamanho do diretorio
					// copia os ponteiros antigos do diretorio para os campos novos
					// cria um bucket novo
					// troca o ponteiro do segundo registro de % atual para o bucket novo
					// rebalanceia o bucket atual com o seu segundo %
					// insere o registro no bucket equivalente
			// se o bucket nao estiver cheio
				// insere o registro no bucket
		// end while
		
		try {
			
			int ultimaId;
			int idAtual;
			Conta contaTemp;
			int tamRegAtual;
			long pos0, pos1, posBucketNovo;
			long endDir;
			int profGlobal;
			int profLocal;
			int numRegs;
			char lapide;
			int ultimoBucket;
			double tamDir;
						
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			
			// limpa o arquivo de indice hash
			arqHash.setLength(0);
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("crud-AEDS3/dados/diretorio.db", "rw");
			
			// limpa o arquivo de diretorio
			arqDir.setLength(0);
			
			// inicializa os arquivos de indice e diretorio com p=1 e buckets vazios
			arqDir.seek(comeco);
			arqDir.writeInt(1); // profundidade global
			profGlobal = 1;
			arqHash.seek(comeco);
			arqHash.writeInt(1); // numero ultimo bucket
			for(int i=0; i<2; i++) {
				arqDir.writeLong(arqHash.getFilePointer()); // escreve o endereco do bucket correspondente
				arqHash.writeInt(1); // profundidade local
				arqHash.writeInt(0); // numero de elementos
				for(int j=0; j<4; j++) {
					arqHash.writeInt(-1); // chave
					arqHash.writeLong(-1); // endereco
				}
			}
						
			
			// para cada registro no arquivo
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = -1;
			while (idAtual != ultimaId) {
				
				// le o registro
				pos1 = arq.getFilePointer(); // endereco antes do tamanho do registro, eh o que sera gravado no diretorio
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				lapide = arq.readChar();
				arq.seek(pos0);
				contaTemp = leRegistro(arq, comeco, pos0);
				if(lapide != '*') {
					idAtual = contaTemp.getIdConta();
					
					// ve no diretorio em qual bucket vai cair
					endDir = getEndDir(arqDir, comeco, idAtual);
					
					// acessa o bucket
					arqHash.seek(endDir);
					profLocal = arqHash.readInt();
					numRegs = arqHash.readInt();
					
					// se o bucket ja estiver cheio
					if(numRegs == 4) {
						
						// se a profundidade local for menor que a profundidade global
						if(profLocal < profGlobal) {
							
							// aumenta a profundidade local
							profLocal += 1;
							arqHash.seek(endDir);
							arqHash.writeInt(profLocal);
							
							// cria um bucket novo
							arqHash.seek(comeco);
							ultimoBucket = arqHash.readInt();
							for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
								arqHash.skipBytes(56);
							}
							posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
							arqHash.writeInt(profLocal);
							arqHash.writeInt(0); // numero de registros
							for(int j=0; j<4; j++) { // escreve valores -1
								arqHash.writeInt(-1); // chave
								arqHash.writeLong(-1); // endereco
							}
							
							// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1))
							arqDir.seek(comeco);
							profGlobal = arqDir.readInt(); // profundidade global
							tamDir = Math.pow(2, profGlobal);
							for(int i=0; i<tamDir; i++) {
								if(((i >> profLocal-1) % 2) == 1 && (i % Math.pow(2, (profGlobal-1))) == (idAtual % Math.pow(2, (profGlobal-1)))) { // se a id atual for parte da 2a metade das ids que pertencem ao grupo que contem ponteiros para o mesmo bucket 
									arqDir.writeLong(posBucketNovo); // grava a posicao do novo bucket
								}
								else { // senao, pula para o proximo
									arqDir.skipBytes(8);
								}
							}
							
							// rebalanceia o bucket atual com o seu segundo %
								
								// le os registros
							int[] chave = new int [5];
							long[] endereco = new long [5];
							arqHash.seek(endDir);
							arqHash.readInt(); // pula a profundidade
							arqHash.readInt(); // pula o numero de elementos (eh 4)
							for(int i=0; i<4; i++) {
								chave[i] = arqHash.readInt();
								endereco[i] = arqHash.readLong();
							}
								// define em qual posicao vai ficar
							int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
							for(int i=0; i<4; i++) {
								if(idAtual < chave[i]) {
									novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
									i = 4; // break
								} else if(idAtual == chave[i]) {
									System.out.println("Erro: ID duplicada");
									return;
								}
							}
							
								// insere o registro e remaneja os existentes
							for(int i=3; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							
							// insere os registros nos buckets equivalentes
							long posPrimeiro, posSegundo;
							arqHash.seek(endDir); // vai para a posicao do primeiro bucket
							arqHash.readInt(); // pula a profundidade
							posPrimeiro = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
							arqHash.readInt(); // pula a profundidade
							posSegundo = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							int numRegs2;
							for(int i=0; i<5; i++) {
								if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
									arqHash.seek(posPrimeiro);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posPrimeiro);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
									
								} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
									arqHash.seek(posSegundo);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posSegundo);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								}
							}
							
							// aumenta o ultimoBucket
							ultimoBucket += 1;
							arqHash.seek(comeco);
							arqHash.writeInt(ultimoBucket);
						}
						
						// se a profundidade local for igual a profundidade global
						else {
							
							// aumenta a profundidade local
							profLocal += 1;
							arqHash.seek(endDir);
							arqHash.writeInt(profLocal);
							
							// aumenta a profundidade global
							profGlobal += 1;
							arqDir.seek(comeco);
							arqDir.writeInt(profGlobal);
							
							// dobra o tamanho do diretorio
							tamDir = Math.pow(2, profGlobal);
							
							// copia os ponteiros antigos do diretorio para os campos novos
							long posMetade1 = arqDir.getFilePointer();
							arqDir.skipBytes((int) ((tamDir/2) * 8));
							long posMetade2 = arqDir.getFilePointer();
							long endAtual;
							for(int i=0; i<tamDir/2; i++) { // varre o arquivo de diretorio copiando os enderecos para a segunda metade
								arqDir.seek(posMetade1);
								endAtual = arqDir.readLong();
								posMetade1 = arqDir.getFilePointer();
								arqDir.seek(posMetade2);
								arqDir.writeLong(endAtual);
								posMetade2 = arqDir.getFilePointer();
							}
							
							// cria um bucket novo
							arqHash.seek(comeco);
							ultimoBucket = arqHash.readInt();
							for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
								arqHash.skipBytes(56);
							}
							posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
							arqHash.writeInt(profLocal);
							arqHash.writeInt(0); // numero de registros
							for(int j=0; j<4; j++) { // escreve valores -1
								arqHash.writeInt(-1); // chave
								arqHash.writeLong(-1); // endereco
							}
							
							// aumenta o ultimoBucket
							ultimoBucket += 1;
							arqHash.seek(comeco);
							arqHash.writeInt(ultimoBucket);
							
							// troca o ponteiro do segundo registro de % atual para o bucket novo
							arqDir.seek(comeco);
							arqDir.readInt(); // pula a profundidade global
							arqDir.skipBytes((int) ((idAtual % Math.pow(2, profGlobal-1)) + Math.pow(2, profGlobal-1)) * 8); // pula para o segundo registro de % atual
							arqDir.writeLong(posBucketNovo);
							
							// rebalanceia o bucket atual com o seu segundo %
							
								// le os registros
							int[] chave = new int [5];
							long[] endereco = new long [5];
							arqHash.seek(endDir);
							arqHash.readInt(); // pula a profundidade
							arqHash.readInt(); // pula o numero de elementos (eh 4)
							for(int i=0; i<4; i++) {
								chave[i] = arqHash.readInt();
								endereco[i] = arqHash.readLong();
							}
								// define em qual posicao vai ficar
							int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
							for(int i=0; i<4; i++) {
								if(idAtual < chave[i]) {
									novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
									i = 4; // break
								} else if(idAtual == chave[i]) {
									System.out.println("Erro: ID duplicada");
									return;
								}
							}
							
								// insere o registro e remaneja os existentes
							for(int i=3; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							
							// insere os registros nos buckets equivalentes
							long posPrimeiro, posSegundo;
							arqHash.seek(endDir); // vai para a posicao do primeiro bucket
							arqHash.readInt(); // pula a profundidade
							posPrimeiro = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
							arqHash.readInt(); // pula a profundidade
							posSegundo = arqHash.getFilePointer();
							arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
							int numRegs2;
							for(int i=0; i<5; i++) {
								if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
									arqHash.seek(posPrimeiro);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posPrimeiro);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
									arqHash.seek(posSegundo);
									numRegs2 = arqHash.readInt();
									arqHash.seek(posSegundo);
									arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
									arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
									arqHash.writeInt(chave[i]); // escreve a chave
									arqHash.writeLong(endereco[i]); // escreve o endereco
								}
							}
						}
					}
					
					// se o bucket nao estiver cheio
					else {
						
						// insere o registro no bucket
						
							// le os registros
						int[] chave = new int [4];
						long[] endereco = new long [4];
						for(int i=0; i<numRegs; i++) {
							chave[i] = arqHash.readInt();
							endereco[i] = arqHash.readLong();
						}
						
							// define em qual posicao vai ficar
						int novaPosicao = numRegs; // se for maior que todos a posicao eh a ultima
						for(int i=0; i<numRegs; i++) {
							if(idAtual < chave[i]) {
								novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
								i = numRegs; // break
							} else if(idAtual == chave[i]) {
								System.out.println("Erro: ID duplicada");
								return;
							}
						}
						
							// insere o registro e remaneja os existentes
						if(numRegs > 0) {
							for(int i=numRegs-1; i>=novaPosicao; i--) {
								chave[i+1] = chave[i];
								endereco[i+1] = endereco[i];
							}
							chave[novaPosicao] = idAtual;
							endereco[novaPosicao] = pos1;
							numRegs += 1;
						} else {
							chave[0] = idAtual;
							endereco[0] = pos1;
							numRegs = 1;
						}
						
							// aumenta o numero de elementos no bucket
						arqHash.seek(endDir);
						arqHash.readInt(); // profundidade local
						arqHash.writeInt(numRegs);
						
							// grava o bucket novo
						for(int i=0; i<numRegs; i++) {
							arqHash.writeInt(chave[i]);
							arqHash.writeLong(endereco[i]);
						}
					}
				}
			// end while
			}
				
			//imprimeArqDir(arqDir, comeco);
			//imprimeArqHash(arqHash, comeco);
			//System.out.println("\nArquivo inicial de hash criado.\n\nAperte enter para continuar.");
			//sc.nextLine();
			arqHash.close();
			arqDir.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void adicionaHash(long comeco, Conta conta, long enderecoArq) {
		// ve no diretorio em qual bucket vai cair
		// acessa o bucket
		// se o bucket ja estiver cheio
			// se a profundidade local for menor que a profundidade global
				// aumenta a profundidade local
				// cria um bucket novo
				// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1)) 
				// rebalanceia o bucket atual com o seu segundo %
				// insere o registro no bucket equivalente
			// se a profundidade local for igual a profundidade global
				// aumenta a profundidade local
				// aumenta a profundidade global
				// dobra o tamanho do diretorio
				// copia os ponteiros antigos do diretorio para os campos novos
				// cria um bucket novo
				// troca o ponteiro do segundo registro de % atual para o bucket novo
				// rebalanceia o bucket atual com o seu segundo %
				// insere o registro no bucket equivalente
		// se o bucket nao estiver cheio
			// insere o registro no bucket
		
		int ultimaId;
		int idAtual;
		Conta contaTemp;
		int tamRegAtual;
		long pos0, pos1, posBucketNovo;
		long endDir;
		int profGlobal;
		int profLocal;
		int numRegs;
		char lapide;
		int ultimoBucket;
		double tamDir;
		
		try {
			
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("crud-AEDS3/dados/diretorio.db", "rw");
			
			// salva a profundidade global
			arqHash.seek(comeco);
			profGlobal = arqHash.readInt();
						
			idAtual = conta.getIdConta();
			pos1 = enderecoArq;
			
			// ve no diretorio em qual bucket vai cair
			endDir = getEndDir(arqDir, comeco, idAtual);
			
			// acessa o bucket
			arqHash.seek(endDir);
			profLocal = arqHash.readInt();
			numRegs = arqHash.readInt();
			
			// se o bucket ja estiver cheio
			if(numRegs == 4) {
				
				// se a profundidade local for menor que a profundidade global
				if(profLocal < profGlobal) {
					
					// aumenta a profundidade local
					profLocal += 1;
					arqHash.seek(endDir);
					arqHash.writeInt(profLocal);
					
					// cria um bucket novo
					arqHash.seek(comeco);
					ultimoBucket = arqHash.readInt();
					for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
						arqHash.skipBytes(56);
					}
					posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
					arqHash.writeInt(profLocal);
					arqHash.writeInt(0); // numero de registros
					for(int j=0; j<4; j++) { // escreve valores -1
						arqHash.writeInt(-1); // chave
						arqHash.writeLong(-1); // endereco
					}
					
					// troca o ponteiro da segunda metade de % atual para o bucket novo ((numBucket >> Pl-1)%2)==1(segunda metade) && (bits da direita iguais)(numBucket%2^(p-1))==(idAtual%2^(p-1))
					arqDir.seek(comeco);
					profGlobal = arqDir.readInt(); // profundidade global
					tamDir = Math.pow(2, profGlobal);
					for(int i=0; i<tamDir; i++) {
						if(((i >> profLocal-1) % 2) == 1 && (i % Math.pow(2, (profGlobal-1))) == (idAtual % Math.pow(2, (profGlobal-1)))) { // se a id atual for parte da 2a metade das ids que pertencem ao grupo que contem ponteiros para o mesmo bucket 
							arqDir.writeLong(posBucketNovo); // grava a posicao do novo bucket
						}
						else { // senao, pula para o proximo
							arqDir.skipBytes(8);
						}
					}
					
					// rebalanceia o bucket atual com o seu segundo %
						
						// le os registros
					int[] chave = new int [5];
					long[] endereco = new long [5];
					arqHash.seek(endDir);
					arqHash.readInt(); // pula a profundidade
					arqHash.readInt(); // pula o numero de elementos (eh 4)
					for(int i=0; i<4; i++) {
						chave[i] = arqHash.readInt();
						endereco[i] = arqHash.readLong();
					}
						// define em qual posicao vai ficar
					int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
					for(int i=0; i<4; i++) {
						if(idAtual < chave[i]) {
							novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
							i = 4; // break
						} else if(idAtual == chave[i]) {
							System.out.println("Erro: ID duplicada");
							return;
						}
					}
					
						// insere o registro e remaneja os existentes
					for(int i=3; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					
					// insere os registros nos buckets equivalentes
					long posPrimeiro, posSegundo;
					arqHash.seek(endDir); // vai para a posicao do primeiro bucket
					arqHash.readInt(); // pula a profundidade
					posPrimeiro = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
					arqHash.readInt(); // pula a profundidade
					posSegundo = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					int numRegs2;
					for(int i=0; i<5; i++) {
						if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
							arqHash.seek(posPrimeiro);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posPrimeiro);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
							
						} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
							arqHash.seek(posSegundo);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posSegundo);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						}
					}
					
					// aumenta o ultimoBucket
					ultimoBucket += 1;
					arqHash.seek(comeco);
					arqHash.writeInt(ultimoBucket);
				}
				
				// se a profundidade local for igual a profundidade global
				else {
					
					// aumenta a profundidade local
					profLocal += 1;
					arqHash.seek(endDir);
					arqHash.writeInt(profLocal);
					
					// aumenta a profundidade global
					profGlobal += 1;
					arqDir.seek(comeco);
					arqDir.writeInt(profGlobal);
					
					// dobra o tamanho do diretorio
					tamDir = Math.pow(2, profGlobal);
					
					// copia os ponteiros antigos do diretorio para os campos novos
					long posMetade1 = arqDir.getFilePointer();
					arqDir.skipBytes((int) ((tamDir/2) * 8));
					long posMetade2 = arqDir.getFilePointer();
					long endAtual;
					for(int i=0; i<tamDir/2; i++) { // varre o arquivo de diretorio copiando os enderecos para a segunda metade
						arqDir.seek(posMetade1);
						endAtual = arqDir.readLong();
						posMetade1 = arqDir.getFilePointer();
						arqDir.seek(posMetade2);
						arqDir.writeLong(endAtual);
						posMetade2 = arqDir.getFilePointer();
					}
					
					// cria um bucket novo
					arqHash.seek(comeco);
					ultimoBucket = arqHash.readInt();
					for(int i=0; i<=ultimoBucket; i++) { // pula todos os buckets existentes
						arqHash.skipBytes(56);
					}
					posBucketNovo = arqHash.getFilePointer(); // endereco do bucket novo
					arqHash.writeInt(profLocal);
					arqHash.writeInt(0); // numero de registros
					for(int j=0; j<4; j++) { // escreve valores -1
						arqHash.writeInt(-1); // chave
						arqHash.writeLong(-1); // endereco
					}
					
					// aumenta o ultimoBucket
					ultimoBucket += 1;
					arqHash.seek(comeco);
					arqHash.writeInt(ultimoBucket);
					
					// troca o ponteiro do segundo registro de % atual para o bucket novo
					arqDir.seek(comeco);
					arqDir.readInt(); // pula a profundidade global
					arqDir.skipBytes((int) ((idAtual % Math.pow(2, profGlobal-1)) + Math.pow(2, profGlobal-1)) * 8); // pula para o segundo registro de % atual
					arqDir.writeLong(posBucketNovo);
					
					// rebalanceia o bucket atual com o seu segundo %
					
						// le os registros
					int[] chave = new int [5];
					long[] endereco = new long [5];
					arqHash.seek(endDir);
					arqHash.readInt(); // pula a profundidade
					arqHash.readInt(); // pula o numero de elementos (eh 4)
					for(int i=0; i<4; i++) {
						chave[i] = arqHash.readInt();
						endereco[i] = arqHash.readLong();
					}
						// define em qual posicao vai ficar
					int novaPosicao = 4; // se for maior que todos a posicao eh a ultima
					for(int i=0; i<4; i++) {
						if(idAtual < chave[i]) {
							novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
							i = 4; // break
						} else if(idAtual == chave[i]) {
							System.out.println("Erro: ID duplicada");
							return;
						}
					}
					
						// insere o registro e remaneja os existentes
					for(int i=3; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					
					// insere os registros nos buckets equivalentes
					long posPrimeiro, posSegundo;
					arqHash.seek(endDir); // vai para a posicao do primeiro bucket
					arqHash.readInt(); // pula a profundidade
					posPrimeiro = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					arqHash.seek(posBucketNovo); // vai para a posicao do segundo bucket
					arqHash.readInt(); // pula a profundidade
					posSegundo = arqHash.getFilePointer();
					arqHash.writeInt(0); // numero de elementos vai para zero para ser aumentado depois
					int numRegs2;
					for(int i=0; i<5; i++) {
						if(chave[i] % Math.pow(2, profLocal-1) == chave[i] % Math.pow(2, profLocal)) { // se a chave pertencer ao primeiro bucket, adiciona a chave ao primeiro bucket e aumenta o numero de elementos
							arqHash.seek(posPrimeiro);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posPrimeiro);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						} else { // se a chave pertencer ao segundo bucket, adiciona a chave ao segundo bucket e aumenta o numero de elementos
							arqHash.seek(posSegundo);
							numRegs2 = arqHash.readInt();
							arqHash.seek(posSegundo);
							arqHash.writeInt(numRegs2 + 1); // aumenta o numero de elementos
							arqHash.skipBytes(12 * numRegs2); // pula os registros que ja foram inseridos
							arqHash.writeInt(chave[i]); // escreve a chave
							arqHash.writeLong(endereco[i]); // escreve o endereco
						}
					}
				}
			}
			
			// se o bucket nao estiver cheio
			else {
				
				// insere o registro no bucket
				
					// le os registros
				int[] chave = new int [4];
				long[] endereco = new long [4];
				for(int i=0; i<numRegs; i++) {
					chave[i] = arqHash.readInt();
					endereco[i] = arqHash.readLong();
				}
				
					// define em qual posicao vai ficar
				int novaPosicao = numRegs; // se for maior que todos a posicao eh a ultima
				for(int i=0; i<numRegs; i++) {
					if(idAtual < chave[i]) {
						novaPosicao = i; // se for menor que a chave[i] entao achou em qual posicao devera ficar
						i = numRegs; // break
					} else if(idAtual == chave[i]) {
						System.out.println("Erro: ID duplicada");
						return;
					}
				}
				
					// insere o registro e remaneja os existentes
				if(numRegs > 0) {
					for(int i=numRegs-1; i>=novaPosicao; i--) {
						chave[i+1] = chave[i];
						endereco[i+1] = endereco[i];
					}
					chave[novaPosicao] = idAtual;
					endereco[novaPosicao] = pos1;
					numRegs += 1;
				} else {
					chave[0] = idAtual;
					endereco[0] = pos1;
					numRegs = 1;
				}
				
					// aumenta o numero de elementos no bucket
				arqHash.seek(endDir);
				arqHash.readInt(); // profundidade local
				arqHash.writeInt(numRegs);
				
					// grava o bucket novo
				for(int i=0; i<numRegs; i++) {
					arqHash.writeInt(chave[i]);
					arqHash.writeLong(endereco[i]);
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}
	
	
	public static void imprimeArqHash(RandomAccessFile arq, long comeco) {
		try {
			arq.seek(comeco);
			int numBuckets = arq.readInt()+1;
			int n;
			System.out.println("Numero de buckets: " + numBuckets);
			for(int i=0; i<numBuckets; i++) {
				System.out.print("Bucket " + i + " p local: " + arq.readInt() + " n: ");
				n = arq.readInt();
				System.out.println(n);
				for(int j=0; j<4; j++) {
					System.out.println(" c " + arq.readInt() + " e " + arq.readLong());
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}
	
	
	public static void imprimeArqDir(RandomAccessFile arq, long comeco) {
		try {
			arq.seek(comeco);
			int prof = arq.readInt();
			int n = (int) Math.pow(2, prof);
			System.out.println("Profundidade global: " + prof);
			for(int i=0; i<n; i++) {
				System.out.println(" bucket c " + i + " e " + arq.readLong());
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	public static void deletaHash(long comeco, int idConta) { // deleta um registro do arquivo de hash, id recebida como parametro
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// remove o registro desejado
			// reescreve o bucket sem o registro
			
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("crud-AEDS3/dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// remove o registro desejado
			for(int i=posReg; i<n-1; i++) {
				chave[i] = chave[i+1];
				endereco[i] = endereco[i+1];
			}
			
			// reescreve o bucket sem o registro
			arqHash.seek(endBucket);
			arqHash.readInt(); // pula a profundidade local
			arqHash.writeInt(n-1); // escreve o novo numero de registros
			for(int i=0; i<n-1; i++) {
				arqHash.writeInt(chave[i]); // escreve a chave
				arqHash.writeLong(endereco[i]); // escreve o endereco
			}
			arqHash.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void alteraHash(long comeco, int idConta, long novoEnd) { // altera o endereco do registro no arquivo hash, recebe a id e o endereco novo como parametro
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// altera o registro desejado
			// reescreve o bucket com o novo endereco
			
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("crud-AEDS3/dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// altera o registro desejado
			endereco[posReg] = novoEnd;

			// reescreve o bucket com o novo endereco
			arqHash.seek(endBucket);
			arqHash.readInt(); // pula a profundidade local
			arqHash.readInt(); // pula o numero de elementos
			for(int i=0; i<n; i++) {
				arqHash.writeInt(chave[i]); // escreve a chave
				arqHash.writeLong(endereco[i]); // escreve o endereco
			}
			arqHash.close();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
	
	
	public static void buscaHash(RandomAccessFile arq, long comeco) {
		try {
			// abre os arquivos
			// encontra o endereco do bucket no diretorio
			// navega ate o bucket
			// le o bucket
			// encontra o registro desejado
			// vai ate o endereco encontrado no arquivo de contas
			// le o registro
			// imprime o registro
			
			
			// pede a id desejada para o usuario
			System.out.println("\n=== BUSCAR UMA CONTA POR HASH ===\n");
			System.out.println("Digite a ID da conta que quer exibir:");
			int idConta = sc.nextInt();
			sc.nextLine();
			
			// abre os arquivos
			// abre o arquivo de indice hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			
			// abre o arquivo de diretorio
			RandomAccessFile arqDir = new RandomAccessFile("crud-AEDS3/dados/diretorio.db", "rw");
			
			// encontra o endereco do bucket no diretorio
			int profGlobal;
			arqDir.seek(comeco);
			profGlobal = arqDir.readInt();
			int tamDir = (int) Math.pow(2, profGlobal);
			arqDir.skipBytes((idConta % tamDir) * 8);
			long endBucket = arqDir.readLong();
			arqDir.close();
			
			// navega ate o bucket
			arqHash.seek(endBucket);
			
			// le o bucket
			arqHash.readInt(); // profundidade local
			int n = arqHash.readInt(); // numero de elementos
			if(n == 0) {
				System.out.println("Erro: ID não encontrada. Bucket vazio");
				arqHash.close();
				return;
			}
			int[] chave = new int[n];
			long[] endereco = new long[n];
			for(int i=0; i<n; i++) {
				chave[i] = arqHash.readInt(); // chave
				endereco[i] = arqHash.readLong(); // endereco
			}
			
			// encontra o registro desejado
			int posReg = -1;
			for(int i=0; i<n; i++) {
				if(chave[i] == idConta) {
					posReg = i;
				}
			}
			if(posReg == -1) {
				System.out.println("Erro: ID não encontrada");
				arqHash.close();
				return;
			}
			
			// vai ate o endereco encontrado no arquivo de contas
			arq.seek(endereco[posReg]);
			long pos0 = arq.getFilePointer();
			
			// le o registro
			arq.readInt(); // pula o tamanho do registro
			Conta conta = leRegistro(arq, comeco, pos0);
			
			// imprime o registro
			System.out.println("\nEndereço do registro: " + pos0);
			System.out.println(conta.toString());
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
						
			arqHash.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		return;
	}
}