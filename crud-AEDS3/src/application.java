import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

public class application {

    public static String nomeArquivo = "crud-AEDS3/dados/conta.db";

    public static void main(String[] args) throws Exception {
            Scanner scanner = new Scanner(System.in);
            Crud crud = new Crud();
            for (int i = 0; i < 30; i++) {
                Crud.writeAccount(Crud.createAccount());
            }
            Crud.listAccouts();
            
             System.out.println(Crud.listAccouts());
             int opcao = -1;
             while (opcao!=0) {
                 menu();
                 opcao = Integer.parseInt(scanner.nextLine());  
             }

    }

    public static void menu() {
        Scanner sc = new Scanner(System.in);
        Scanner ps = new Scanner(System.in);
        int resp = 0, id = 0, r = 0;

        long comeco;

        try {

            RandomAccessFile arq = new RandomAccessFile("crud-AEDS3/dados/conta.db", "rw"); 
            comeco = arq.getFilePointer();
            if(arq.length()==0) { 
                arq.writeInt(-1);
                arq.seek(comeco); // volta pro inicio
            }


            ArrayList<Conta> account = new ArrayList<Conta>();

            hash.criaHash(arq, comeco); // cria o arquivo de hash para evitar erros que existiriam sem esses arquivos

            System.out.println("\nMENU:");
            System.out.println("1- Criar conta");
            System.out.println("2- Realizar uma transferencia");
            System.out.println("3- Ler um registro por ID");
            System.out.println("4- Atualizar um registro");
            System.out.println("5- Deletar um registro");
            System.out.println("6- Cria Hash");
            System.out.println("7- Busca Hash");
            System.out.println("8- Lista Invertida");
            System.out.println("9- Comprime LZW");
            System.out.println("10- Comprime Huffman");
            System.out.println("11- Intercalacao Balanceada");
            System.out.println("0- Sair");

            System.out.print("Digite a opcao desejada: ");
            resp = sc.nextInt();

            switch (resp) {
                case 1:
                    System.out.println("\n\nOpcao escolhida: \n\t1- Criar conta\n");

                    methods.criaContaFunc(ps);

                    System.out.println("\nSua conta foi cadastrada com sucesso!");
                    break;

                case 2:
                    float valor = 0;
                    int idConta1, idConta2;

                    System.out.println("\n\nOpcao escolhida: \n\t2- Realizar uma transferencia\n");
                    
                    System.out.println("Digite a conta de origem: ");
                    idConta1= sc.nextInt();

                    System.out.println("Digite a conta de destino: ");
                    idConta2= sc.nextInt();

                    System.out.println("Digite o valor da transferencia: ");
                    valor = sc.nextFloat();

                    Crud.transferencia(Crud.readById(idConta1), Crud.readById(idConta2), valor);

                    System.out.println("\nTransferencia realizada com sucesso!");
                    break;
                case 3:
                    System.out.println("\n\nOpcao escolhida: \n\t3- Ler um registro por ID\n");

                    methods.contaPorId(ps);

                    System.out.println("\nArquivo de registros lido com sucesso!");
                    break;

                case 4:
                    System.out.println("\n\nOpcao escolhida: \n\t4- Atualizar registro");
                    int idConta;
                    System.out.println("Digite o id da Conta: ");
                    idConta= sc.nextInt();

                    Crud.update(Crud.readById(idConta));


                    System.out.println("\nSua conta foi atualizada com sucesso!");
                    break;

                case 5:
                    System.out.println("\n\nOpcao escolhida: \n\t5- Deletar registro");

                    System.out.println("Digite o id da Conta: ");
                    idConta= sc.nextInt();

                    Crud.delete(idConta);
                    
                    System.out.println("\nA conta foi deletada com sucesso!");
                    break;

                case 6:
                    System.out.println("\n\nOpcao escolhida: \n\t6- Cria Hash");
                    hash.criaHash(arq, comeco);
                    break;

                case 7:
                    System.out.println("\n\nOpcao escolhida: \n\t7- Busca Hash");
                    hash.buscaHash(arq, comeco);
                    break;

                case 9:
                    System.out.println("\n\nOpcao escolhida: \n\t9- Comprimir LZW");
                    lzw.comprimeLZW(arq, comeco);
                    break;

                case 10:
                    System.out.println("\n\nOpcao escolhida: \n\t11- Comprimir Huffman");
                    huffman.compressaoHuffman(sc, arq);
                    break;
                case 11:
                    System.out.println("\n\nOpcao escolhida: \n\t11- Comprimir Huffman");
                    methods.intercalacaoBalanceada(arq, comeco);
                    break;
                default:
                    System.out.print("\n\nOpcao invalida!");
                    break;
            }

        }catch(IOException e) {
	    	System.out.println(e.getMessage());

        }

    }
}

