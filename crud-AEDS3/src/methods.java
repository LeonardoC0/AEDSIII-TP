import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class methods {
    public static Scanner sc = new Scanner(System.in);
	static int numOp;

    public methods(){};

    public static void criaContaFunc(Scanner scanner){
		String senhacrypto;
        Conta conta = new Conta();
        System.out.println("Digite seu nome: ");
        conta.nomePessoa = scanner.nextLine();
        System.out.println("Digite seu email: ");
        conta.email = scanner.nextLine();
        System.out.println("Digite seu nome de usuario:");
        conta.nomeUsuario = scanner.nextLine();
        System.out.println("Digite sua senha:");
		senhacrypto = scanner.nextLine();
        try {
			conta.senha = OneTimePad.encryptionSenha(senhacrypto);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Digite seu CPF");
        conta.cpf = scanner.nextLine();
        System.out.println("Digite sua cidade:");
        conta.cidade = scanner.nextLine();
        System.out.println("Digite o saldo da conta:");
        conta.saldoConta = Float.parseFloat(scanner.nextLine());
        conta.transferenciasRealizadas = 0;
        Crud.writeAccount(conta);

    }

    public static void contaPorId (Scanner scanner){
        int idConta;
		String senhaDecrypto;

        System.out.println("Digite a conta de origem: ");
        idConta= scanner.nextInt();

        Conta conta = Crud.readById(idConta);

        System.out.println("\nInformações da Conta: ");
        System.out.println("IdConta: " + conta.idConta);
        System.out.println("Nome: " + conta.nomePessoa);
        System.out.println("Email: " + conta.email);
        System.out.println("Usuario: " + conta.nomeUsuario);
		senhaDecrypto = conta.senha;
		try {
			senhaDecrypto = OneTimePad.decryptionSenha(conta.senha);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Senha: " + senhaDecrypto);
        System.out.println("CPF: " + conta.cpf);
        System.out.println("Cidade: " + conta.cidade);
        System.out.println("Numero de Transferencia: " + conta.transferenciasRealizadas);
        System.out.println("Saldo: " + conta.saldoConta);

    }

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

    public static void escreveRegistro (RandomAccessFile arq, long pos0, Conta conta) { // escreve uma conta na posicao dada (escreve tambem o tamanho, nao grava ultimaId)
		try {
			arq.seek(pos0);
			byte[] ba = conta.toByteArray();
			arq.writeInt(ba.length);
			arq.write(ba);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return;
	}

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

    public static void copiaArquivo (RandomAccessFile arqOrigem, long comeco, RandomAccessFile arqDestino) { // copia um arquivo no lugar de outro arquivo
		long pos0, pos1;
		Conta contaTemp;
		int tamRegAtual;
		int ultimaId;
		int idAtual;
		
		try {
			arqOrigem.seek(comeco);
			arqDestino.seek(comeco);
			
			// le qual eh a ultima id
			ultimaId = arqOrigem.readInt();
			
			if(ultimaId > 0) {
				// escreve a ultima id
				arqDestino.writeInt(ultimaId);
				
				do {

					// le o registro atual
					tamRegAtual = arqOrigem.readInt();
					pos0 = arqOrigem.getFilePointer();
					contaTemp = leRegistro (arqOrigem, comeco, pos0);
					idAtual = contaTemp.getIdConta();
					//System.out.println(contaTemp.toString()); // teste
					
					
				} while (idAtual != ultimaId);
				
			} else {
				arqDestino.writeInt(-1);
				return;
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	


    public static void intercalacaoBalanceada (RandomAccessFile arq, long comeco) {
		try {
			System.out.println("\n=== INTERCALAÇÃO BALANCEADA COMUM ===\n");
			
			int m, n; // m registros, n caminhos
			int ultimaId;
			int idAtual = 0;
			int tamRegAtual;
			long pos0, pos1, posBucketNovo;
			Conta contatemp;
			int arquivoFinal = 0;
			
			System.out.println("Por favor, informe...\nNúmero de registros que cabem na memória:");
			m = sc.nextInt();
			sc.nextLine();
			System.out.println("Número de caminhos:");
			n = sc.nextInt();
			sc.nextLine();
			ArrayList<RandomAccessFile> arqTemp = new ArrayList<RandomAccessFile>();
			List<Conta> memoria = new ArrayList<Conta>(m);
			int [] ultimaId2 = new int [2*n];
			int[] ultimoSalvo = new int[2*n];
			
			
			arq.seek(comeco);
			ultimaId = arq.readInt();
			System.out.println("\nArquivo antes da ordenação:");
			/*while(idAtual != ultimaId) { // imprime a ordem do arquivo antes da ordenacao
				tamRegAtual = arq.readInt();
				pos0 = arq.getFilePointer();
				if(arq.readChar() != '*') {
					idAtual = arq.readInt();
					System.out.print(idAtual + " ");
				}
				arq.seek(pos0);
				arq.skipBytes(tamRegAtual);
			}
			System.out.println("");*/
			imprimeArquivo(arq, comeco);
			
			for(int i=0; i<2*n; i++) { // inicia os RandomAccesFiles dos arquivos temporarios
				arqTemp.add(new RandomAccessFile("crud-AEDS3/dados/listaInvertida/arqTemp" + i + ".db", "rw"));
				arqTemp.get(i).writeInt(-1); // escreve -1 como sendo a ultima id
				ultimaId2[i] = -1;
			}
			
			arq.seek(comeco);
			ultimaId = arq.readInt();
			idAtual = 0;
			
			for(int i=0; idAtual != ultimaId; i++){ // faz a distribuicao
				// carrega a memoria com os dados
				while(memoria.size()<m && idAtual != ultimaId) { // carrega os m registros na memoria
					tamRegAtual = arq.readInt();
					pos1 = arq.getFilePointer();
					if(arq.readChar()!='*') {
						arq.seek(pos1);
						contatemp = leRegistro(arq, comeco, pos1);
						memoria.add(contatemp);
						idAtual = contatemp.getIdConta();
					} else {
						arq.seek(pos1);
						arq.skipBytes(tamRegAtual);
					}
				}
				
				//for(Conta k : memoria) { // teste
				//	System.out.print(k.getIdConta());
				//}
				
				memoria.sort(Comparator.comparing(Conta::getIdConta)); // ordena a memoria
				
				//for(Conta k : memoria) { // teste
				//	System.out.print(k.getIdConta());
				//}
				//System.out.println(memoria.size()); // teste
				
				if(arqTemp.get(i%n).length() == 0) { // se o arquivo for vazio, escreve -1 como a ultima id
					arqTemp.get(i%n).seek(comeco);
					arqTemp.get(i%n).writeInt(-1);
					posBucketNovo = arqTemp.get(i%n).getFilePointer(); // salva a posicao atual
					//System.out.println("arquivo vazio"); // teste
				} else {
					posBucketNovo = arqTemp.get(i%n).getFilePointer(); // salva a posicao atual
					arqTemp.get(i%n).seek(comeco); // navega ate o comeco do arquivo temporario para gravar qual foi a ultima id
					arqTemp.get(i%n).writeInt(memoria.get(memoria.size()-1).getIdConta()); // salva qual eh a ultima id na memoria
					arqTemp.get(i%n).seek(posBucketNovo); // navega ate a posicao do comeco do bloco atual
				}
				for(Conta contaTemp : memoria) { // grava os registros da memoria no arquivo temporario
					//System.out.println("escreveu registro " + contaTemp.getIdConta()); // teste
					arqTemp.get(i%n).seek(comeco);
					arqTemp.get(i%n).writeInt(contaTemp.getIdConta()); // escreve a ultima id
					ultimaId2[i%n] = contaTemp.getIdConta();
					arqTemp.get(i%n).seek(posBucketNovo);
					escreveRegistro(arqTemp.get(i%n), posBucketNovo, contaTemp);
					posBucketNovo = arqTemp.get(i%n).getFilePointer();
				}
				
				memoria.clear(); // limpa a memoria
				
				//System.out.println("Arquivo " + i%n + ":"); // teste
				//System.out.println("ultimaid " + ultimaId); // teste
				//imprimeArquivo(arqTemp.get(i%n), comeco); // teste
			}
			
			//System.out.println("INTERCALAÇÃO"); // teste
			
			// do {
				// do {
					// enquanto bloco nao acabou
						// para cada arquivo temporario do lado atual
							// se for a primeira passada ou for o que saiuDaFita
								// se a posicao do bloco atual for < tamanho do bloco ordenado
									// se nao tiver acabado o arquivo
										// le o registro atual
										// aumenta a posBlocoAtual
									// se tiver acabado o arquivo
										// salva o blocoAcabou
								// se a posicao do bloco atual for >= tamanho do bloco ordenado
									// salva o blocoAcabou
						// checa o menor na memoria
							// se o registro for null pula ele
						// salva o saiuDaFita
						// salva o registro no arquivo de saida
						// salva o ultimoSalvo
						// remove o registro da memoria
						// checa se todos os blocos acabaram
					// volta as posBlocoAtual para 0
					// reseta os blocos
					// troca o arquivo de saida 
					// checa se todos os registros foram lidos
				// } repete enquanto todos ultimoSalvo forem diferentes do ultimaId2 para os n arquivos atuais
				// dobra o tamBlocoOrdenado
				// troca os arquivos atuais
				// troca a fitaDeSaida
					// limpa os novos arquivos de saida
			// } repete enquanto tiver mais de um arquivo de saida com dados
			
			int[] posBlocoAtual = new int [n]; // tamanho do bloco
			int tamBlocoOrdenado = m;
			int saiuDaFita = 0;
			boolean[] blocoAcabou = new boolean[n];
			Conta[] memoria2 = new Conta[n];
			int fitaAtual;
			int ordem = 0;
			long[] pos = new long[n*2];
			int menor;
			int indiceMenor;
			int fitaDeSaida = n;
			boolean todosAcabaram = false;
			boolean todosRegistrosLidos;
			int naoOrdem;
			boolean temMaisDeUmComDados = true;
			int arqComDados = n;
			
			// zera o ultimoSalvo, posBlocoAtual e pos e volta o ponteiro para o comeco dos arquivos temporarios
			for(int i=0; i<n*2; i++) {
				ultimoSalvo[i] = -1;
				arqTemp.get(i).seek(comeco);
				ultimaId2[i] = arqTemp.get(i).readInt(); // salva a ultima id 
				pos[i] = arqTemp.get(i).getFilePointer();
			}
			for(int i=0; i<n; i++) {
				posBlocoAtual[i] = 0;
			}
			
			// do {
			do {
				
				// reseta os ponteiros para o comeco do arquivo
				for(int i=0; i<n*2; i++) {
					arqTemp.get(i).seek(comeco);
					arqTemp.get(i).readInt();
					pos[i] = arqTemp.get(i).getFilePointer();
				}
				
				// do {
				do {
					
					// enquanto bloco nao acabou
					while(!todosAcabaram) {
						
						// para cada arquivo temporario do lado atual
						for(int i=0; i<n; i++) {
							
							// se for a primeira passada ou for o que saiuDaFita
							fitaAtual = (ordem*n) + i;
							if(posBlocoAtual[fitaAtual%n] == 0 || fitaAtual == saiuDaFita) {
								
								// se a posicao do bloco atual for < tamanho do bloco ordenado
								if(posBlocoAtual[fitaAtual%n] < tamBlocoOrdenado) {
									
									// se nao tiver acabado o arquivo
									if(ultimoSalvo[fitaAtual] != ultimaId2[fitaAtual]) {
										
										// le o registro atual
										//System.out.print("tentou ler registro na fita " + fitaAtual + " na posição " + pos[fitaAtual] + "\nfita " + fitaAtual + ": "); // teste
										//imprimeArquivo(arqTemp.get(fitaAtual), comeco); // teste
										//System.out.println("ultimoSalvo[" + fitaAtual + "] = " + ultimoSalvo[fitaAtual] + " ultimaId2[" + fitaAtual + "] = " + ultimaId2[fitaAtual] + " posBlocoAtual = " + posBlocoAtual[fitaAtual%n]); // teste
										arqTemp.get(fitaAtual).seek(pos[fitaAtual]);
										tamRegAtual = arqTemp.get(fitaAtual).readInt();
										memoria2[i] = leRegistro(arqTemp.get(fitaAtual), comeco, pos[fitaAtual]);
										pos[fitaAtual] = arqTemp.get(fitaAtual).getFilePointer();
										// ultimoSalvo[fitaAtual] = memoria2[i].getIdConta();
										//System.out.println("leu registro " + memoria2[i].getIdConta() + " posBlocoAtual[" + fitaAtual%n + "] calculado = " + (posBlocoAtual[fitaAtual%n] + 1)); // teste
										
										// aumenta a posBlocoAtual
										posBlocoAtual[fitaAtual%n]++;
										//System.out.println("depois da leitura posBlocoAtual[" + fitaAtual%n + "] = " + posBlocoAtual[fitaAtual%n]); // teste
									}
									// se tiver acabado o arquivo
									else {
										
										// salva o blocoAcabou
										blocoAcabou[fitaAtual%n] = true;
									}
								}
								// se a posicao do bloco atual for >= tamanho do bloco ordenado
								else {
									
									// salva o blocoAcabou
									blocoAcabou[fitaAtual%n] = true;
								}
							}
						}
						
						//for(int i=0; i<n; i++) // teste
						//	if(memoria2[i] != null) // teste
						//		System.out.print(memoria2[i].getIdConta() + " "); // teste
						//System.out.println(""); // teste
						
						
						/*if(memoria2[0] != null) {
							menor = memoria2[0].getIdConta();
							indiceMenor = 0;
						} else { 
							
							// se o registro de indice 0 for null busca o proximo registro not null
							menor = 2147483647;
							indiceMenor = 2147483647;
							boolean achouProximo = false;
							for(int i=0; i<n && !achouProximo; i++) {
								if(memoria2[i] != null) {
									menor = memoria2[i].getIdConta();
									indiceMenor = i;
									achouProximo = true;
								} else {
									menor = 2147483647;
									indiceMenor = 2147483647;
								}
							}
						}*/
						
						// checa o menor na memoria
							// se o registro for null pula ele
						menor = 2147483647;
						indiceMenor = 2147483647;
						for(int i=0; i<n; i++) {
							if(memoria2[i] != null) {
								if(memoria2[i].getIdConta() < menor) {
									menor = memoria2[i].getIdConta();
									indiceMenor = i;
								}
							}
						}
						
						// se achou um registro, salva ele
						if(indiceMenor != 2147483647) { 
							
							// salva o saiuDaFita
							saiuDaFita = indiceMenor + (ordem*n);
							int fitaQueFoiLida = (ordem*n) + indiceMenor; 
							
							//System.out.println("Tentou escrever registro " + menor + " de indice " + indiceMenor + " no arquivo " + fitaDeSaida); // teste
							// salva o registro no arquivo de saida
							arqTemp.get(fitaDeSaida).seek(comeco);
							arqTemp.get(fitaDeSaida).writeInt(menor);
							escreveRegistro(arqTemp.get(fitaDeSaida), pos[fitaDeSaida], memoria2[indiceMenor]);
							pos[fitaDeSaida] = arqTemp.get(fitaDeSaida).getFilePointer();
							//pos[saiuDaFita] = arqTemp.get(saiuDaFita).getFilePointer();
							
							// salva o ultimoSalvo
							ultimaId2[fitaDeSaida] = memoria2[indiceMenor].getIdConta();
							ultimoSalvo[fitaQueFoiLida] = memoria2[indiceMenor].getIdConta();
							//System.out.println("escreveu registro " + menor + " de indice " + indiceMenor + " no arquivo " + fitaDeSaida + ", ultimoSalvo[" + fitaQueFoiLida + "] = " + ultimoSalvo[fitaQueFoiLida]); // teste
							
							// remove o registro da memoria
							memoria2[indiceMenor] = null;
						} else {
							//System.out.println("não escreveu nenhum registro"); // teste
						}
						
						// checa se todos os blocos acabaram
						todosAcabaram = true;
						for(int i=0; i<n; i++) {
							if(!blocoAcabou[i]) {
								todosAcabaram = false;
							}
						}
						
						//System.out.println("fita de saida " + fitaDeSaida + ":"); // teste
						//imprimeArquivo(arqTemp.get(fitaDeSaida), comeco); // teste
						
					}
					
					// volta as posBlocoAtual para 0
					for(int i=0; i<n; i++) {
						posBlocoAtual[i] = 0;
						blocoAcabou[i] = false;
						//System.out.println("zerou posBloco["+i+"]"); // teste
					}
					
					// reseta os blocos
					todosAcabaram = false;
					
					// troca o arquivo de saida
					//System.out.print("fita de saida antes: " + fitaDeSaida); // teste
					if (ordem == 0) {
						naoOrdem = 1;
					} else {
						naoOrdem = 0;
					}
					fitaDeSaida = (naoOrdem*n) + ((fitaDeSaida+1)%n);
					//System.out.println(" fita de saida depois: " + fitaDeSaida); // teste
					
					// checa se todos os registros foram lidos
					todosRegistrosLidos = true;
					for(int i=0; i<n; i++) {
						if(ultimoSalvo[(ordem*n) + i] != ultimaId2[(ordem*n) + i]) {
							todosRegistrosLidos = false;
						}
						//System.out.println("final do while todosRegistrosLidos ultimoSalvo[" + ((ordem*n) + i) + "] = " + ultimoSalvo[(ordem*n) + i] + " ultimaId2[" + ((ordem*n) + i) + "] = " + ultimaId2[(ordem*n) + i]); // teste
						//sc.nextLine();
					}
					
				// } repete enquanto todos ultimoSalvo forem diferentes do ultimaId2 para os n arquivos atuais
				} while (!todosRegistrosLidos);
				
				// dobra o tamBlocoOrdenado
				tamBlocoOrdenado *= 2;
				
				// troca os arquivos atuais
				if(ordem == 0) {
					ordem = 1;
					naoOrdem = 0;
				} else {
					ordem = 0;
					naoOrdem = 1;
				}
				
				// troca a fitaDeSaida
				//System.out.print("troca grupo de fitas. fita de saida antes: " + fitaDeSaida); // teste
				fitaDeSaida = naoOrdem*n;
				//System.out.println(" fita de saida depois: " + fitaDeSaida); // teste
				
				// limpa os novos arquivos de saida
				for(int i=0; i<n; i++) {
					int arquivoAtual = (naoOrdem*n) + i; 
					arqTemp.get(arquivoAtual).setLength(0);
					arqTemp.get(arquivoAtual).writeInt(-1);
					pos[arquivoAtual] = arqTemp.get(arquivoAtual).getFilePointer();
					//System.out.println("limpou o ultimoSalvo do arquivo " + ((ordem*n) + i)); // teste
					ultimoSalvo[(ordem*n) + i] = -1; // ultimo salvo do arquivo de entrada
					ultimaId2[arquivoAtual] = -1;
				}
				
				// checa se tem mais de um com dados
				temMaisDeUmComDados = false;
				int numComDados = 0;
				for(int i=0; i<n; i++) {
					int arquivoAtual = (ordem*n) + i;
					
					//System.out.println("arquivo " + arquivoAtual + ":"); // teste
					//imprimeArquivo(arqTemp.get(arquivoAtual), comeco); // teste
					//System.out.println("length = " + arqTemp.get(arquivoAtual).length()); // teste
					if(arqTemp.get(arquivoAtual).length() > 4) {
						numComDados++;
						arqComDados = arquivoAtual;
						//System.out.println("arquivo " + (arquivoAtual) + " tem dados"); // teste
						arquivoFinal = arquivoAtual;
					}
					// sc.nextLine(); // teste
					
					// reseta os ponteiros para o comeco do arquivo
					arqTemp.get(arquivoAtual).seek(comeco);
					arqTemp.get(arquivoAtual).readInt();
					pos[arquivoAtual] = arqTemp.get(arquivoAtual).getFilePointer();
				}
				if(numComDados > 1) {
					temMaisDeUmComDados = true;
				} 
				
			// } repete enquanto tiver mais de um arquivo de saida com dados
			} while (temMaisDeUmComDados);
			//System.out.println("arquivo final com dados: "); // teste
			//imprimeArquivo(arqTemp.get(arqComDados), comeco); // teste
			
			// escreve sobre o arquivo de dados
			copiaArquivo(arqTemp.get(arquivoFinal), comeco, arq);
			
			// atualiza o arquivo hash
			RandomAccessFile arqHash = new RandomAccessFile("crud-AEDS3/dados/hash.db", "rw");
			hash.criaHash(arqHash, comeco);
			
			System.out.println("\nArquivo após a ordenação:");
			imprimeArquivo(arq, comeco);
			System.out.println("\nAperte enter para continuar.");
			sc.nextLine();
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void buscarPadrao (RandomAccessFile arq, long comeco) {
		int idAtual = -1;
		ArrayList<Integer> contasEncontradas = new ArrayList<Integer>();
		int ultimaId;
		
		try {
			arq.seek(comeco);
			ultimaId = arq.readInt();
			
			// pergunta para o usuario qual sera o texto a ser procurado
			System.out.println("\nDigite o texto que deseja buscar: (máximo 64 caracteres)");
			String padrao = sc.nextLine();
			
			// para cada registro no arquivo
			while(idAtual != ultimaId) {
				boolean achou = false;
				numOp = 0;
				
				// le o registro
				int tamReg = arq.readInt(); // tamanho do registro
				long pos0 = arq.getFilePointer();
				char lapide = arq.readChar();
				arq.seek(pos0);
				Conta conta = leRegistro(arq, comeco, pos0);
				if(lapide != '*') {
					idAtual = conta.getIdConta();
					
					
					// checa se o padrao procurado existe no nome
					if(shiftAnd(conta.getNomePessoa(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe no nome de usuario
					if(shiftAnd(conta.getNomeUsuario(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe no email
						if(shiftAnd(conta.getEmail(), padrao)) {
							achou = true;
						}
					
					
					// checa se o padrao procurado existe no cpf
					if(shiftAnd(conta.getCpf(), padrao)) {
						achou = true;
					}
					
					// checa se o padrao procurado existe na cidade
					if(shiftAnd(conta.getCidade(), padrao)) {
						achou = true;
					}
					
					// se achou, adiciona ao array de contas encontradas
					if(achou) {
						contasEncontradas.add(conta.getIdConta());
					}
				}
				
				// pula para o proximo registro
				arq.seek(pos0);
				arq.skipBytes(tamReg);
			}
			
			// imprime as contas encontradas
			System.out.println("\nOperações realizadas: " + numOp);
			if(contasEncontradas.size() > 0) {
				System.out.println("\nTexto encontrado nas contas:");
				for(Integer p : contasEncontradas) {
					System.out.print(p);
					if(p != contasEncontradas.get(contasEncontradas.size() -1)) {
						System.out.print(", ");
					} else {
						System.out.println("\n\nAperte enter para continuar.");
						sc.nextLine();
					}
				}
			} else {
				System.out.println("\nTexto não encontrado no arquivo.");
				System.out.println("\nAperte enter para continuar.");
				sc.nextLine();
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	
	public static boolean shiftAnd(String texto, String padrao) { // retorna true se o padrao foi encontrado e false se nao foi
		boolean encontrou = false;
		
		// se o padrao cabe no texto
		if(texto.length() >= padrao.length()) {
			
			// carrega a lista de caracteres para fazer as mascaras
			String caracteres = "";
			for(int i=0; i<padrao.length(); i++) {
				boolean jaTem = false;
				for(int j=0; j<caracteres.length(); j++) {
					if(caracteres.charAt(j) == padrao.charAt(i)) {
						jaTem = true;
					}
				}
				if(!jaTem) {
					caracteres += padrao.charAt(i);
				}
			}
			
			// carrega as mascaras
			long[] mascara = new long[caracteres.length() + 1];
			for(int i=0; i<mascara.length-1; i++) {
				mascara[i] = 0;
				for(int j=0; j<padrao.length(); j++) {
					if(caracteres.charAt(i) == padrao.charAt(j)) {
						mascara[i] = mascara[i] | ((long) 1 << (63-j));
					}
				}
			}
			mascara[caracteres.length()] = 0;
			
			// faz o shift-and
			long resultadoAnterior = (long) 1 << 63;
			long resultadoAtual;
			for(int i=0; i<texto.length(); i++) {
				char caracterAtual = texto.charAt(i);
				int indiceCharAtual = caracteres.length();
				for(int j=0; j<caracteres.length(); j++) {
					if(caracterAtual == caracteres.charAt(j)) {
						indiceCharAtual = j;
					}
				}
				
				// faz o and
				resultadoAtual = resultadoAnterior & mascara[indiceCharAtual];
				
				// faz o shift
				resultadoAnterior = (resultadoAtual >> 1) | ((long) 1 << 63);
				
				// verifica se encontrou
				if((resultadoAtual & ((long) 1 << (63 - (padrao.length() - 1)))) == ((long) 1 << (63 - (padrao.length() - 1)))) {
					encontrou = true;
				}
				
				numOp++;
			}
			
			return encontrou;
		} else {
			return false;
		}
	}

}
