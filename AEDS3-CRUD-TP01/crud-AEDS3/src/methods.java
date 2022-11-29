import java.util.Scanner;

public class methods {
    public methods(){};

    public static void criaContaFunc(Scanner scanner){
        Conta conta = new Conta();
        System.out.println("Digite seu nome: ");
        conta.nomePessoa = scanner.nextLine();
        System.out.println("Digite seu email: ");
        conta.email = scanner.nextLine();
        System.out.println("Digite seu nome de usuario:");
        conta.nomeUsuario = scanner.nextLine();
        System.out.println("Digite sua senha:");
        conta.senha = scanner.nextLine();
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

        System.out.println("Digite a conta de origem: ");
        idConta= scanner.nextInt();

        Conta conta = Crud.readById(idConta);

        System.out.println("\nInformações da Conta: ");
        System.out.println("IdConta: " + conta.idConta);
        System.out.println("Nome: " + conta.nomePessoa);
        System.out.println("Email: " + conta.email);
        System.out.println("Usuario: " + conta.nomeUsuario);
        System.out.println("Senha: " + conta.senha);
        System.out.println("CPF: " + conta.cpf);
        System.out.println("Cidade: " + conta.cidade);
        System.out.println("Numero de Transferencia: " + conta.transferenciasRealizadas);
        System.out.println("Saldo: " + conta.saldoConta);

    }

}
